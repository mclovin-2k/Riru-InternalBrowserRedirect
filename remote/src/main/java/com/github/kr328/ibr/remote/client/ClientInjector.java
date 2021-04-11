package com.github.kr328.ibr.remote.client;

import android.os.Build;
import android.os.IBinder;

import com.github.kr328.ibr.remote.proxy.ServiceManagerProxy;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

public class ClientInjector {
    public static void inject() throws ReflectiveOperationException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.setHiddenApiExemptions(
                    "Landroid/app/IActivityTaskManager",
                    "Landroid/app/IActivityManager",
                    "Landroid/app/IApplicationThread"
            );
        }
        final ClientActivityManagerProxy proxy = new ClientActivityManagerProxy();

        ServiceManagerProxy.install(new ServiceManagerProxy.Callback() {
            @Override
            public IBinder getService(String name, IBinder service) {
                return proxy.proxy(name, service);
            }
        });
    }
}
