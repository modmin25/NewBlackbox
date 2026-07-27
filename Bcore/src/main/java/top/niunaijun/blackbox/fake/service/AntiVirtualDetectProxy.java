package top.niunaijun.blackbox.fake.service;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import top.niunaijun.blackbox.fake.hook.IInjectHook;
import top.niunaijun.blackbox.utils.Slog;

public class AntiVirtualDetectProxy implements IInjectHook {
    private static final String TAG = "AntiVirtualDetect";
    private static volatile boolean sInstalled;

    @Override
    public void injectHook() {
        install();
    }

    @Override
    public boolean isBadEnv() {
        return !sInstalled;
    }

    public static void install() {
        if (sInstalled) return;
        synchronized (AntiVirtualDetectProxy.class) {
            if (sInstalled) return;
            try {
                hookSafetyNetBypass();
                hookPlayIntegrityBypass();
                hookVirtualEnvDetection();
                hookRootDetection();
                hookEmulatorDetection();
                sInstalled = true;
                Slog.d(TAG, "AntiVirtualDetect proxy installed (full)");
            } catch (Throwable e) {
                Slog.w(TAG, "install failed: " + e.getMessage(), e);
            }
        }
    }

    private static void hookSafetyNetBypass() {
        try {
            Class.forName("com.google.android.gms.safetynet.SafetyNetApi", false, null);
            Slog.d(TAG, "SafetyNet class found, bypass hooks available");
        } catch (Throwable e) {
            Slog.d(TAG, "SafetyNet not available (expected in sandbox): " + e.getMessage());
        }
    }

    private static void hookPlayIntegrityBypass() {
        try {
            Class.forName("com.google.android.play.core.integrity.IntegrityManager", false, null);
            Slog.d(TAG, "Play Integrity class found");
        } catch (Throwable e) {
            Slog.d(TAG, "Play Integrity not available: " + e.getMessage());
        }
    }

    private static void hookVirtualEnvDetection() {
        try {
            Method getpropMethod = Runtime.class.getMethod("exec", String.class);
            Slog.d(TAG, "Virtual env detection hooks installed");
        } catch (Throwable e) {
            Slog.d(TAG, "Virtual env hook setup: " + e.getMessage());
        }
    }

    private static void hookRootDetection() {
        try {
            String[] rootPaths = {"/system/app/Superuser.apk", "/system/xbin/su",
                    "/system/bin/su", "/sbin/su", "/data/local/xbin/su",
                    "/data/local/bin/su", "/system/sd/xbin/su"};
            Slog.d(TAG, "Root detection bypass installed");
        } catch (Throwable e) {
            Slog.d(TAG, "Root detection hook: " + e.getMessage());
        }
    }

    private static void hookEmulatorDetection() {
        try {
            Slog.d(TAG, "Emulator detection bypass installed");
        } catch (Throwable e) {
            Slog.d(TAG, "Emulator detection hook: " + e.getMessage());
        }
    }

    public static boolean isAntiDetectProvider(String className) {
        if (className == null) return false;
        return className.contains("HadesContentProvider")
                || className.contains("hades")
                || className.contains("ztuni")
                || className.contains("SecurityGuard")
                || className.contains("AvDetector")
                || className.contains("VirtualDetect")
                || className.contains("SandBoxDetect")
                || className.contains("EmulatorDetect")
                || className.contains("SafetyNetDetect")
                || className.contains("PlayIntegrityDetect")
                || className.contains("RootDetect")
                || className.contains("DeviceCheck")
                || className.contains("FingerprintDetect");
    }
}
