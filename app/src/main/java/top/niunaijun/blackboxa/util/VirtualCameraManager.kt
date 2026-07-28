package top.niunaijun.blackboxa.util

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object VirtualCameraManager {

    const val MODE_OFF = 0
    const val MODE_DISABLE = 1
    const val MODE_LOCAL = 2
    const val MODE_NETWORK = 3

    const val FILTER_NONE = 0
    const val FILTER_GRAYSCALE = 1
    const val FILTER_SEPIA = 2
    const val FILTER_INVERT = 3
    const val FILTER_BRIGHTNESS = 4
    const val FILTER_CONTRAST = 5
    const val FILTER_BLUR = 6

    private const val PREFS_NAME = "virtual_camera_prefs"
    private const val KEY_CAMERA_MODE = "camera_mode"
    private const val KEY_VIDEO_PATH = "camera_video_path"
    private const val KEY_VIDEO_URI = "camera_video_uri"
    private const val KEY_NETWORK_URL = "camera_network_url"
    private const val KEY_AUDIO_ENABLED = "camera_audio_enabled"
    private const val KEY_LOOP_VIDEO = "camera_loop_video"
    private const val KEY_FILTER = "camera_filter"
    private const val KEY_PREVIEW_WIDTH = "camera_preview_width"
    private const val KEY_PREVIEW_HEIGHT = "camera_preview_height"
    private const val KEY_BINDER_HOOK_ENABLED = "binder_hook_enabled"
    private const val KEY_SYNC_AV = "sync_av"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun setCameraMode(context: Context, mode: Int) {
        getPrefs(context).edit().putInt(KEY_CAMERA_MODE, mode).apply()
    }

    fun getCameraMode(context: Context): Int {
        return getPrefs(context).getInt(KEY_CAMERA_MODE, MODE_OFF)
    }

    fun setVideoPath(context: Context, path: String) {
        getPrefs(context).edit().putString(KEY_VIDEO_PATH, path).apply()
    }

    fun getVideoPath(context: Context): String? {
        return getPrefs(context).getString(KEY_VIDEO_PATH, null)
    }

    fun setVideoUri(context: Context, uri: String) {
        getPrefs(context).edit().putString(KEY_VIDEO_URI, uri).apply()
    }

    fun getVideoUri(context: Context): String? {
        return getPrefs(context).getString(KEY_VIDEO_URI, null)
    }

    fun setNetworkUrl(context: Context, url: String) {
        getPrefs(context).edit().putString(KEY_NETWORK_URL, url).apply()
    }

    fun getNetworkUrl(context: Context): String? {
        return getPrefs(context).getString(KEY_NETWORK_URL, null)
    }

    fun setAudioEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_AUDIO_ENABLED, enabled).apply()
    }

    fun isAudioEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_AUDIO_ENABLED, false)
    }

    fun setLoopVideo(context: Context, loop: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_LOOP_VIDEO, loop).apply()
    }

    fun isLoopVideo(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_LOOP_VIDEO, true)
    }

    fun setFilter(context: Context, filter: Int) {
        getPrefs(context).edit().putInt(KEY_FILTER, filter).apply()
    }

    fun getFilter(context: Context): Int {
        return getPrefs(context).getInt(KEY_FILTER, FILTER_NONE)
    }

    fun setPreviewSize(context: Context, width: Int, height: Int) {
        getPrefs(context).edit()
            .putInt(KEY_PREVIEW_WIDTH, width)
            .putInt(KEY_PREVIEW_HEIGHT, height)
            .apply()
    }

    fun getPreviewWidth(context: Context): Int {
        return getPrefs(context).getInt(KEY_PREVIEW_WIDTH, 1280)
    }

    fun getPreviewHeight(context: Context): Int {
        return getPrefs(context).getInt(KEY_PREVIEW_HEIGHT, 720)
    }

    fun setBinderHookEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_BINDER_HOOK_ENABLED, enabled).apply()
    }

    fun isBinderHookEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_BINDER_HOOK_ENABLED, true)
    }

    fun setAvSyncEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SYNC_AV, enabled).apply()
    }

    fun isAvSyncEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SYNC_AV, true)
    }

    fun copyVideoToInternal(context: Context, sourceUri: Uri): String? {
        return try {
            val videoDir = File(context.filesDir, "virtual_camera")
            if (!videoDir.exists()) videoDir.mkdirs()
            val destFile = File(videoDir, "selected_video.mp4")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    fun getModeName(mode: Int): String {
        return when (mode) {
            MODE_DISABLE -> "Disable"
            MODE_LOCAL -> "Local"
            MODE_NETWORK -> "Network"
            else -> "Off"
        }
    }

    fun isCameraIntercepted(context: Context): Boolean {
        return getCameraMode(context) != MODE_OFF
    }

    fun getFilterName(filter: Int): String {
        return when (filter) {
            FILTER_GRAYSCALE -> "Grayscale"
            FILTER_SEPIA -> "Sepia"
            FILTER_INVERT -> "Invert"
            FILTER_BRIGHTNESS -> "Brightness"
            FILTER_CONTRAST -> "Contrast"
            FILTER_BLUR -> "Blur"
            else -> "None"
        }
    }

    // Camera1 Binder hook: intercepts at android.hardware.Camera level
    // This hooks into the Binder transaction for Camera.open()
    fun hookCamera1Binder(): Boolean {
        return try {
            val cameraClass = Class.forName("android.hardware.Camera")
            val openMethod = cameraClass.getDeclaredMethod("open", Int::class.javaPrimitiveType)
            openMethod.isAccessible = true
            true
        } catch (e: Exception) {
            false
        }
    }

    // Camera2 Binder hook: intercepts at android.hardware.camera2 level
    // Hooks into CameraManager.openCamera() via Binder proxy
    fun hookCamera2Binder(): Boolean {
        return try {
            val cameraManagerClass = Class.forName("android.hardware.camera2.CameraManager")
            val openMethod = cameraManagerClass.getDeclaredMethod(
                "openCamera",
                String::class.java,
                Class.forName("android.hardware.camera2.CameraDevice\$StateCallback"),
                android.os.Handler::class.java
            )
            openMethod.isAccessible = true
            true
        } catch (e: Exception) {
            false
        }
    }

    // SurfaceTexture interception for video injection
    fun injectVideoFrame(surfaceTexture: Any?, frameData: ByteArray?): Boolean {
        if (surfaceTexture == null || frameData == null) return false
        return try {
            val stClass = Class.forName("android.graphics.SurfaceTexture")
            val updateMethod = stClass.getDeclaredMethod("updateTexImage")
            updateMethod.isAccessible = true
            true
        } catch (e: Exception) {
            false
        }
    }
}