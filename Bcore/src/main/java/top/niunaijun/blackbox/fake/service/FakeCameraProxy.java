package top.niunaijun.blackbox.fake.service;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.Method;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.fake.hook.ClassInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;
import top.niunaijun.blackbox.utils.Slog;

public class FakeCameraProxy extends ClassInvocationStub {
    public static final String TAG = "FakeCameraProxy";

    public static final int CAMERA_MODE_OFF = 0;
    public static final int CAMERA_MODE_DISABLE = 1;
    public static final int CAMERA_MODE_LOCAL = 2;
    public static final int CAMERA_MODE_NETWORK = 3;

    private static final String PREFS_NAME = "virtual_camera_prefs";
    private static final String KEY_CAMERA_MODE = "camera_mode";

    public FakeCameraProxy() {
        super();
    }

    @Override
    protected Object getWho() {
        return null;
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    private static int getCameraMode() {
        try {
            Context context = BlackBoxCore.getContext();
            if (context == null) return CAMERA_MODE_OFF;
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            return prefs.getInt(KEY_CAMERA_MODE, CAMERA_MODE_OFF);
        } catch (Exception e) {
            Slog.e(TAG, "Failed to read camera mode: " + e.getMessage());
            return CAMERA_MODE_OFF;
        }
    }

    @ProxyMethod("open")
    public static class CameraOpen extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            int mode = getCameraMode();
            Slog.d(TAG, "Camera.open() intercepted, mode=" + mode);
            if (mode == CAMERA_MODE_OFF) {
                return method.invoke(who, args);
            }
            if (mode == CAMERA_MODE_DISABLE) {
                Slog.d(TAG, "Camera disabled by Virtual Camera setting, returning null");
                return null;
            }
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("openLegacy")
    public static class CameraOpenLegacy extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            int mode = getCameraMode();
            Slog.d(TAG, "Camera.openLegacy() intercepted, mode=" + mode);
            if (mode == CAMERA_MODE_OFF) {
                return method.invoke(who, args);
            }
            if (mode == CAMERA_MODE_DISABLE) {
                return null;
            }
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("setPreviewTexture")
    public static class SetPreviewTexture extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            int mode = getCameraMode();
            if (mode == CAMERA_MODE_OFF) {
                return method.invoke(who, args);
            }
            Slog.d(TAG, "Camera.setPreviewTexture() intercepted, mode=" + mode);
            return null;
        }
    }

    @ProxyMethod("startPreview")
    public static class StartPreview extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            int mode = getCameraMode();
            if (mode == CAMERA_MODE_OFF) {
                return method.invoke(who, args);
            }
            Slog.d(TAG, "Camera.startPreview() intercepted, mode=" + mode);
            return null;
        }
    }

    @ProxyMethod("stopPreview")
    public static class StopPreview extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            int mode = getCameraMode();
            if (mode == CAMERA_MODE_OFF) {
                return method.invoke(who, args);
            }
            Slog.d(TAG, "Camera.stopPreview() intercepted, mode=" + mode);
            return null;
        }
    }

    @ProxyMethod("release")
    public static class CameraRelease extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            int mode = getCameraMode();
            if (mode == CAMERA_MODE_OFF) {
                return method.invoke(who, args);
            }
            Slog.d(TAG, "Camera.release() intercepted, mode=" + mode);
            return null;
        }
    }

    @ProxyMethod("setPreviewCallback")
    public static class SetPreviewCallback extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            int mode = getCameraMode();
            if (mode == CAMERA_MODE_OFF) {
                return method.invoke(who, args);
            }
            Slog.d(TAG, "Camera.setPreviewCallback() intercepted, mode=" + mode);
            return null;
        }
    }

    @ProxyMethod("setPreviewCallbackWithBuffer")
    public static class SetPreviewCallbackWithBuffer extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            int mode = getCameraMode();
            if (mode == CAMERA_MODE_OFF) {
                return method.invoke(who, args);
            }
            Slog.d(TAG, "Camera.setPreviewCallbackWithBuffer() intercepted, mode=" + mode);
            return null;
        }
    }

    @ProxyMethod("unlock")
    public static class CameraUnlock extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            int mode = getCameraMode();
            if (mode == CAMERA_MODE_OFF) {
                return method.invoke(who, args);
            }
            Slog.d(TAG, "Camera.unlock() intercepted, mode=" + mode);
            return null;
        }
    }

    @ProxyMethod("reconnect")
    public static class CameraReconnect extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            int mode = getCameraMode();
            if (mode == CAMERA_MODE_OFF) {
                return method.invoke(who, args);
            }
            Slog.d(TAG, "Camera.reconnect() intercepted, mode=" + mode);
            return null;
        }
    }

    @ProxyMethod("takePicture")
    public static class TakePicture extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            int mode = getCameraMode();
            if (mode == CAMERA_MODE_OFF) {
                return method.invoke(who, args);
            }
            Slog.d(TAG, "Camera.takePicture() intercepted, mode=" + mode);
            return 0;
        }
    }

    @ProxyMethod("getNumberOfCameras")
    public static class GetNumberOfCameras extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            int mode = getCameraMode();
            if (mode == CAMERA_MODE_OFF) {
                return method.invoke(who, args);
            }
            if (mode == CAMERA_MODE_DISABLE) {
                Slog.d(TAG, "Camera.getNumberOfCameras() returning 0 (disabled)");
                return 0;
            }
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("getCameraInfo")
    public static class GetCameraInfo extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            int mode = getCameraMode();
            if (mode == CAMERA_MODE_OFF) {
                return method.invoke(who, args);
            }
            Slog.d(TAG, "Camera.getCameraInfo() intercepted, mode=" + mode);
            return method.invoke(who, args);
        }
    }
}
