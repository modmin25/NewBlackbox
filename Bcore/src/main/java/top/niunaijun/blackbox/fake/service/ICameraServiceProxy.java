package top.niunaijun.blackbox.fake.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

import java.lang.reflect.Method;

import black.android.os.BRServiceManager;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.fake.hook.BinderInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;
import top.niunaijun.blackbox.utils.Reflector;
import top.niunaijun.blackbox.utils.Slog;

public class ICameraServiceProxy extends BinderInvocationStub {
    public static final String TAG = "CameraServiceProxy";

    public static final int CAMERA_MODE_OFF = 0;
    public static final int CAMERA_MODE_DISABLE = 1;
    public static final int CAMERA_MODE_LOCAL = 2;
    public static final int CAMERA_MODE_NETWORK = 3;

    private static final String PREFS_NAME = "virtual_camera_prefs";
    private static final String KEY_CAMERA_MODE = "camera_mode";

    public ICameraServiceProxy() {
        super(BRServiceManager.get().getService("media.camera"));
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

    @Override
    protected Object getWho() {
        IBinder binder = BRServiceManager.get().getService("media.camera");
        if (binder == null) {
            Slog.e(TAG, "Failed to get media.camera binder");
            return null;
        }
        try {
            Object iface = null;
            try {
                iface = Reflector.on("android.hardware.ICameraService$Stub").call("asInterface", binder);
            } catch (Exception e1) {
                Slog.d(TAG, "Trying alternative path: " + e1.getMessage());
                try {
                    Class<?> stubClass = Class.forName("android.hardware.ICameraService$Stub");
                    Method asInterfaceMethod = stubClass.getMethod("asInterface", IBinder.class);
                    iface = asInterfaceMethod.invoke(null, binder);
                } catch (Exception e3) {
                    Slog.e(TAG, "All paths failed for ICameraService", e3);
                    return null;
                }
            }
            if (iface != null) {
                Slog.d(TAG, "Successfully obtained ICameraService interface");
                return (IInterface) iface;
            }
        } catch (Exception e) {
            Slog.e(TAG, "Failed to get ICameraService interface", e);
        }
        return null;
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("media.camera");
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @Override
    public boolean transact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        int mode = getCameraMode();
        if (mode == CAMERA_MODE_OFF) {
            return super.transact(code, data, reply, flags);
        }

        Slog.d(TAG, "Camera service transaction code=" + code + ", mode=" + mode);

        if (mode == CAMERA_MODE_DISABLE) {
            Slog.d(TAG, "Camera disabled, blocking transaction code=" + code);
            if (reply != null) {
                reply.writeNoException();
                reply.writeInt(0);
            }
            return true;
        }

        return super.transact(code, data, reply, flags);
    }

    @ProxyMethod("getNumberOfCameras")
    public static class GetNumberOfCameras extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            int mode = getCameraMode();
            if (mode == CAMERA_MODE_DISABLE) {
                Slog.d(TAG, "getNumberOfCameras() returning 0 (disabled)");
                return 0;
            }
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("connectDevice")
    public static class ConnectDevice extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            int mode = getCameraMode();
            Slog.d(TAG, "connectDevice() intercepted, mode=" + mode);
            if (mode == CAMERA_MODE_DISABLE) {
                return null;
            }
            return method.invoke(who, args);
        }
    }
}
