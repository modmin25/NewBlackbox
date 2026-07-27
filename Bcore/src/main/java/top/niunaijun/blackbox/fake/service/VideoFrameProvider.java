package top.niunaijun.blackbox.fake.service;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Decodes video files and provides NV21 frames (Camera1 API preview callback format).
 * Uses MediaExtractor to read video and MediaCodec to decode to YUV_420_888,
 * then converts to NV21 byte arrays.
 */
public class VideoFrameProvider {

    private static final String TAG = "VideoFrameProvider";
    private static final long TIMEOUT_US = 10_000;
    private static final int DEFAULT_FPS = 15;

    public interface FrameCallback {
        void onFrameAvailable(byte[] frame, int width, int height);
    }

    private final Context mContext;
    private volatile FrameCallback mCallback;
    private volatile int mFps = DEFAULT_FPS;
    private final AtomicBoolean mLoopMode = new AtomicBoolean(false);
    private final AtomicBoolean mRunning = new AtomicBoolean(false);
    private final AtomicBoolean mReleased = new AtomicBoolean(false);

    private final Object mLock = new Object();
    private MediaExtractor mExtractor;
    private MediaCodec mDecoder;
    private MediaFormat mVideoFormat;
    private int mVideoWidth;
    private int mVideoHeight;

    private HandlerThread mDecodeThread;
    private Handler mDecodeHandler;

    private Uri mUri;
    private String mFilePath;

    public VideoFrameProvider(Context context) {
        mContext = context.getApplicationContext();
    }

    public void setCallback(FrameCallback callback) {
        mCallback = callback;
    }

    public void setFps(int fps) {
        if (fps < 1) {
            throw new IllegalArgumentException("FPS must be >= 1");
        }
        mFps = fps;
    }

    public int getFps() {
        return mFps;
    }

    public void setLoopMode(boolean loop) {
        mLoopMode.set(loop);
    }

    public boolean isLoopMode() {
        return mLoopMode.get();
    }

    public boolean isRunning() {
        return mRunning.get();
    }

    public int getVideoWidth() {
        return mVideoWidth;
    }

    public int getVideoHeight() {
        return mVideoHeight;
    }

    public void setDataSource(String filePath) {
        if (mRunning.get()) {
            throw new IllegalStateException("Cannot set data source while running");
        }
        mFilePath = filePath;
        mUri = null;
    }

    public void setDataSource(Uri uri) {
        if (mRunning.get()) {
            throw new IllegalStateException("Cannot set data source while running");
        }
        mUri = uri;
        mFilePath = null;
    }

    public void start() {
        if (mReleased.get()) {
            throw new IllegalStateException("Provider has been released");
        }
        if (mRunning.getAndSet(true)) {
            return;
        }

        mDecodeThread = new HandlerThread("VideoFrameDecoder");
        mDecodeThread.start();
        mDecodeHandler = new Handler(mDecodeThread.getLooper());
        mDecodeHandler.post(this::doStart);
    }

    private void doStart() {
        try {
            synchronized (mLock) {
                if (!openExtractor()) {
                    mRunning.set(false);
                    return;
                }
                if (!setupDecoder()) {
                    closeExtractor();
                    mRunning.set(false);
                    return;
                }
            }
            decodeLoop();
        } catch (Exception e) {
            Log.e(TAG, "Error during decoding", e);
            cleanup();
            mRunning.set(false);
        }
    }

    private boolean openExtractor() {
        try {
            mExtractor = new MediaExtractor();
            if (mFilePath != null) {
                mExtractor.setDataSource(mFilePath);
            } else if (mUri != null) {
                mExtractor.setDataSource(mContext, mUri, null);
            } else {
                Log.e(TAG, "No data source set");
                return false;
            }

            for (int i = 0; i < mExtractor.getTrackCount(); i++) {
                MediaFormat format = mExtractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime != null && mime.startsWith("video/")) {
                    mVideoFormat = format;
                    mVideoWidth = format.getInteger(MediaFormat.KEY_WIDTH);
                    mVideoHeight = format.getInteger(MediaFormat.KEY_HEIGHT);
                    mExtractor.selectTrack(i);
                    Log.d(TAG, "Video track: " + mVideoWidth + "x" + mVideoHeight + " " + mime);
                    return true;
                }
            }

            Log.e(TAG, "No video track found");
            return false;
        } catch (IOException e) {
            Log.e(TAG, "Failed to open data source", e);
            return false;
        }
    }

    private boolean setupDecoder() {
        try {
            String mime = mVideoFormat.getString(MediaFormat.KEY_MIME);
            if (mime == null) {
                Log.e(TAG, "MIME type is null");
                return false;
            }

            mVideoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);

            MediaCodecInfo codecInfo = findCodecForType(mime);
            if (codecInfo != null) {
                mDecoder = MediaCodec.createByCodecName(codecInfo.getName());
            } else {
                mDecoder = MediaCodec.createDecoderByType(mime);
            }

            mDecoder.configure(mVideoFormat, null, null, 0);
            mDecoder.start();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Failed to setup decoder", e);
            return false;
        }
    }

    private MediaCodecInfo findCodecForType(String mime) {
        MediaCodecList codecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
        for (MediaCodecInfo info : codecList.getCodecInfos()) {
            if (info.isEncoder()) {
                continue;
            }
            for (String type : info.getSupportedTypes()) {
                if (type.equalsIgnoreCase(mime)) {
                    return info;
                }
            }
        }
        return null;
    }

    private void decodeLoop() {
        boolean inputDone = false;
        boolean outputDone = false;
        long frameIntervalUs = 1_000_000L / mFps;
        long lastFrameTimeUs = 0;

        while (!outputDone && mRunning.get() && !mReleased.get()) {
            if (!inputDone) {
                inputDone = feedInput();
            }

            FrameResult result = drainOutput();
            if (result == null) {
                continue;
            }

            if (result.isEndOfStream) {
                outputDone = true;
            }

            if (result.nv21 != null) {
                long now = System.nanoTime() / 1000;
                if (lastFrameTimeUs > 0) {
                    long elapsed = now - lastFrameTimeUs;
                    long sleepUs = frameIntervalUs - elapsed;
                    if (sleepUs > 0 && sleepUs < frameIntervalUs) {
                        try {
                            Thread.sleep(sleepUs / 1000, (int) ((sleepUs % 1000) * 1000));
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
                lastFrameTimeUs = System.nanoTime() / 1000;

                FrameCallback callback = mCallback;
                if (callback != null) {
                    callback.onFrameAvailable(result.nv21, result.width, result.height);
                }
            }
        }

        if (mLoopMode.get() && mRunning.get() && !mReleased.get()) {
            restart();
        } else {
            cleanup();
            mRunning.set(false);
        }
    }

    private boolean feedInput() {
        synchronized (mLock) {
            if (mExtractor == null || mDecoder == null) {
                return true;
            }

            int inputIndex = mDecoder.dequeueInputBuffer(TIMEOUT_US);
            if (inputIndex < 0) {
                return false;
            }

            ByteBuffer inputBuffer = mDecoder.getInputBuffer(inputIndex);
            if (inputBuffer == null) {
                return false;
            }

            int sampleSize = mExtractor.readSampleData(inputBuffer, 0);
            if (sampleSize < 0) {
                mDecoder.queueInputBuffer(inputIndex, 0, 0, 0,
                        MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                return true;
            }

            long presentationTimeUs = mExtractor.getSampleTime();
            mDecoder.queueInputBuffer(inputIndex, 0, sampleSize, presentationTimeUs, 0);
            mExtractor.advance();
            return false;
        }
    }

    private static class FrameResult {
        final byte[] nv21;
        final int width;
        final int height;
        final boolean isEndOfStream;

        FrameResult(byte[] nv21, int width, int height, boolean isEndOfStream) {
            this.nv21 = nv21;
            this.width = width;
            this.height = height;
            this.isEndOfStream = isEndOfStream;
        }
    }

    private FrameResult drainOutput() {
        synchronized (mLock) {
            if (mDecoder == null) {
                return null;
            }

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputIndex = mDecoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_US);

            if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat newFormat = mDecoder.getOutputFormat();
                Log.d(TAG, "Output format changed: " + newFormat);
                return null;
            }

            if (outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER || outputIndex < 0) {
                return null;
            }

            try {
                ByteBuffer outputBuffer = mDecoder.getOutputBuffer(outputIndex);
                boolean isEndOfStream = (bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0;

                if (isEndOfStream || outputBuffer == null || bufferInfo.size <= 0) {
                    if (isEndOfStream) {
                        return new FrameResult(null, mVideoWidth, mVideoHeight, true);
                    }
                    return null;
                }

                MediaFormat outputFormat = mDecoder.getOutputFormat();
                int stride = outputFormat.containsKey(MediaFormat.KEY_STRIDE)
                        ? outputFormat.getInteger(MediaFormat.KEY_STRIDE) : mVideoWidth;
                int sliceHeight = outputFormat.containsKey(MediaFormat.KEY_SLICE_HEIGHT)
                        ? outputFormat.getInteger(MediaFormat.KEY_SLICE_HEIGHT) : mVideoHeight;
                int colorFormat = outputFormat.containsKey(MediaFormat.KEY_COLOR_FORMAT)
                        ? outputFormat.getInteger(MediaFormat.KEY_COLOR_FORMAT)
                        : MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible;

                byte[] nv21 = convertToNv21(outputBuffer, mVideoWidth, mVideoHeight,
                        stride, sliceHeight, colorFormat);

                return new FrameResult(nv21, mVideoWidth, mVideoHeight, false);
            } finally {
                mDecoder.releaseOutputBuffer(outputIndex, false);
            }
        }
    }

    private byte[] convertToNv21(ByteBuffer buffer, int width, int height,
                                 int stride, int sliceHeight, int colorFormat) {
        int ySize = width * height;
        byte[] nv21 = new byte[ySize + (ySize / 2)];

        buffer.rewind();

        switch (colorFormat) {
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
                convertNv12ToNv21(buffer, nv21, width, height, stride, sliceHeight);
                break;
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
                convertI420ToNv21(buffer, nv21, width, height, stride, sliceHeight);
                break;
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible:
            default:
                convertFlexibleToNv21(buffer, nv21, width, height, stride, sliceHeight);
                break;
        }

        return nv21;
    }

    private void convertFlexibleToNv21(ByteBuffer buffer, byte[] nv21,
                                       int width, int height, int stride, int sliceHeight) {
        int ySize = width * height;
        int frameSize = stride * sliceHeight;

        buffer.position(0);
        buffer.get(nv21, 0, Math.min(ySize, buffer.remaining()));

        int uvLimit = Math.min(frameSize + ySize, buffer.capacity());
        int uvAvailable = uvLimit - frameSize;
        if (uvAvailable <= 0) {
            return;
        }

        byte[] uvTemp = new byte[Math.min(ySize / 2, uvAvailable)];
        buffer.position(frameSize);
        buffer.get(uvTemp, 0, uvTemp.length);

        int dstOffset = ySize;
        for (int i = 0; i + 1 < uvTemp.length && dstOffset + 1 < nv21.length; i += 2) {
            nv21[dstOffset] = uvTemp[i + 1];
            nv21[dstOffset + 1] = uvTemp[i];
            dstOffset += 2;
        }
    }

    private void convertNv12ToNv21(ByteBuffer buffer, byte[] nv21,
                                   int width, int height, int stride, int sliceHeight) {
        int ySize = width * height;
        int frameSize = stride * sliceHeight;

        buffer.position(0);
        buffer.get(nv21, 0, Math.min(ySize, buffer.remaining()));

        int uvLimit = Math.min(frameSize + ySize, buffer.capacity());
        int uvAvailable = uvLimit - frameSize;
        if (uvAvailable <= 0) {
            return;
        }

        byte[] uvTemp = new byte[Math.min(ySize / 2, uvAvailable)];
        buffer.position(frameSize);
        buffer.get(uvTemp, 0, uvTemp.length);

        int dstOffset = ySize;
        for (int i = 0; i + 1 < uvTemp.length && dstOffset + 1 < nv21.length; i += 2) {
            nv21[dstOffset] = uvTemp[i + 1];
            nv21[dstOffset + 1] = uvTemp[i];
            dstOffset += 2;
        }
    }

    private void convertI420ToNv21(ByteBuffer buffer, byte[] nv21,
                                   int width, int height, int stride, int sliceHeight) {
        int ySize = width * height;
        int uvSize = ySize / 4;
        int frameSize = stride * sliceHeight;

        buffer.position(0);
        buffer.get(nv21, 0, Math.min(ySize, buffer.remaining()));

        byte[] uPlane = new byte[uvSize];
        byte[] vPlane = new byte[uvSize];

        int uStart = Math.min(frameSize, buffer.capacity());
        if (uStart + uvSize * 2 <= buffer.capacity()) {
            buffer.position(uStart);
            buffer.get(uPlane, 0, uvSize);
            buffer.get(vPlane, 0, uvSize);
        }

        int dstOffset = ySize;
        for (int i = 0; i < uvSize && dstOffset + 1 < nv21.length; i++) {
            nv21[dstOffset] = vPlane[i];
            nv21[dstOffset + 1] = uPlane[i];
            dstOffset += 2;
        }
    }

    private void restart() {
        Log.d(TAG, "Restarting video (loop mode)");
        synchronized (mLock) {
            closeDecoder();
            closeExtractor();
        }
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        synchronized (mLock) {
            if (!openExtractor()) {
                mRunning.set(false);
                return;
            }
            if (!setupDecoder()) {
                closeExtractor();
                mRunning.set(false);
                return;
            }
        }
        decodeLoop();
    }

    public void stop() {
        if (!mRunning.getAndSet(false)) {
            return;
        }
        cleanup();
        if (mDecodeThread != null) {
            mDecodeThread.quitSafely();
            try {
                mDecodeThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            mDecodeThread = null;
            mDecodeHandler = null;
        }
    }

    public void release() {
        if (mReleased.getAndSet(true)) {
            return;
        }
        mRunning.set(false);
        cleanup();
        if (mDecodeThread != null) {
            mDecodeThread.quitSafely();
            try {
                mDecodeThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            mDecodeThread = null;
            mDecodeHandler = null;
        }
        mCallback = null;
    }

    private void cleanup() {
        synchronized (mLock) {
            closeDecoder();
            closeExtractor();
        }
    }

    private void closeDecoder() {
        if (mDecoder != null) {
            try {
                mDecoder.stop();
            } catch (Exception e) {
                Log.w(TAG, "Error stopping decoder", e);
            }
            try {
                mDecoder.release();
            } catch (Exception e) {
                Log.w(TAG, "Error releasing decoder", e);
            }
            mDecoder = null;
        }
    }

    private void closeExtractor() {
        if (mExtractor != null) {
            try {
                mExtractor.release();
            } catch (Exception e) {
                Log.w(TAG, "Error releasing extractor", e);
            }
            mExtractor = null;
        }
    }
}
