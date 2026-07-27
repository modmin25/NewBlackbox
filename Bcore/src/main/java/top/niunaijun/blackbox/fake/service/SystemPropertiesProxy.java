package top.niunaijun.blackbox.fake.service;

import java.lang.reflect.Method;

import top.niunaijun.blackbox.fake.hook.ClassInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;
import top.niunaijun.blackbox.app.BActivityThread;
import top.niunaijun.blackbox.utils.Slog;

public class SystemPropertiesProxy extends ClassInvocationStub {
    public static final String TAG = "SystemPropertiesProxy";

    private static Class<?> sSysPropsClass;

    public SystemPropertiesProxy() {
        super();
    }

    @Override
    protected Object getWho() {
        try {
            sSysPropsClass = Class.forName("android.os.SystemProperties");
            return sSysPropsClass;
        } catch (Throwable e) {
            Slog.e(TAG, "Failed to find SystemProperties class: " + e.getMessage());
            return null;
        }
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @ProxyMethod("get")
    public static class Get extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (args == null || args.length < 1) {
                return method.invoke(who, args);
            }

            String key = args[0] instanceof String ? (String) args[0] : "";
            Object result = method.invoke(who, args);

            try {
                String pkgName = BActivityThread.getAppPackageName();
                if (pkgName == null) return result;

                switch (key) {
                    case "ro.product.model":
                        return SpoofProvider.getSpoofedModel(pkgName, result);
                    case "ro.product.manufacturer":
                        return SpoofProvider.getSpoofedManufacturer(pkgName, result);
                    case "ro.product.brand":
                        return SpoofProvider.getSpoofedBrand(pkgName, result);
                    case "ro.product.device":
                        return SpoofProvider.getSpoofedDevice(pkgName, result);
                    case "ro.product.name":
                        return SpoofProvider.getSpoofedProduct(pkgName, result);
                    case "ro.product.board":
                        return SpoofProvider.getSpoofedBoard(pkgName, result);
                    case "ro.product.bootloader":
                        return SpoofProvider.getSpoofedBootloader(pkgName, result);
                    case "ro.hardware":
                        return SpoofProvider.getSpoofedHardware(pkgName, result);
                    case "ro.build.display.id":
                        return SpoofProvider.getSpoofedDisplay(pkgName, result);
                    case "ro.build.fingerprint":
                        return SpoofProvider.getSpoofedFingerprint(pkgName, result);
                    case "ro.build.host":
                        return SpoofProvider.getSpoofedHost(pkgName, result);
                    case "ro.build.type":
                        return SpoofProvider.getSpoofedType(pkgName, result);
                    case "ro.build.tags":
                        return SpoofProvider.getSpoofedTags(pkgName, result);
                    case "ro.build.user":
                        return SpoofProvider.getSpoofedUser(pkgName, result);
                    case "ro.serialno":
                        return SpoofProvider.getSpoofedSerial(pkgName, result);
                    case "ro.com.google.clientidbase":
                    case "ro.com.google.clientidbase.ms":
                    case "ro.com.google.clientidbase.gmm":
                    case "ro.com.google.clientidbase.cm":
                        Slog.d(TAG, "GMS client ID requested for: " + pkgName);
                        return result;
                }
            } catch (Throwable e) {
                Slog.d(TAG, "Hook error for key " + key + ": " + e.getMessage());
            }

            return result;
        }
    }
}
