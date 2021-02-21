package android.os;

public class ServiceManagerNative extends Binder implements IServiceManager {
    public static IServiceManager asInterface(IBinder binder) {
        throw new IllegalArgumentException("Unsupported");
    }

    // Pie
    @Override
    public IBinder getService(String paramString) throws RemoteException {
        throw new RemoteException("Unsupported");
    }

    @Override
    public IBinder checkService(String name) throws RemoteException {
        throw new RemoteException("Unsupported");
    }

    @Override
    public void addService(String name, IBinder service, boolean allowIsolated, int dumpFlags) throws RemoteException {
        throw new RemoteException("Unsupported");
    }

    @Override
    public String[] listServices(int dumpFlags) throws RemoteException {
        throw new RemoteException("Unsupported");
    }

    @Override
    public void setPermissionController(IPermissionController controller) throws RemoteException {
        throw new RemoteException("Unsupported");
    }

    // Oreo
    @Override
    public void addService(String name, IBinder service, boolean allowIsolated) throws RemoteException {
        throw new RemoteException("Unsupported");
    }

    @Override
    public String[] listServices() throws RemoteException {
        throw new RemoteException("Unsupported");
    }

    @Override
    public boolean isDeclared(String name) throws RemoteException {
        throw new RemoteException("Unsupported");
    }

    @Override
    public void registerForNotifications(String name, IServiceCallback callback) throws RemoteException {
        throw new RemoteException("Unsupported");
    }

    @Override
    public void unregisterForNotifications(String name, IServiceCallback callback) throws RemoteException {
        throw new RemoteException("Unsupported");
    }

    @Override
    public String[] getDeclaredInstances(String iface) throws RemoteException {
        throw new RemoteException("Unsupported");
    }

    @Override
    public void registerClientCallback(String name, IBinder service, IClientCallback callback) throws RemoteException {
        throw new RemoteException("Unsupported");
    }

    @Override
    public void tryUnregisterService(String name, IBinder service) throws RemoteException {
        throw new RemoteException("Unsupported");
    }

    @Override
    public ServiceDebugInfo[] getServiceDebugInfo() throws RemoteException {
        throw new RemoteException("Unsupported");
    }

    @Override
    public IBinder asBinder() {
        throw new IllegalArgumentException("Unsupported");
    }
}
