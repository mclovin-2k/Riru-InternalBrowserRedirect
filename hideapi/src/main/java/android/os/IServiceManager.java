package android.os;

public interface IServiceManager extends IInterface {
    // Pie
    IBinder getService(String name) throws RemoteException;

    IBinder checkService(String name) throws RemoteException;

    void addService(String name, IBinder service, boolean allowIsolated, int dumpFlags)
            throws RemoteException;

    String[] listServices(int dumpFlags) throws RemoteException;

    void setPermissionController(IPermissionController controller)
            throws RemoteException;

    // Oreo
    void addService(String name, IBinder service, boolean allowIsolated)
            throws RemoteException;

    String[] listServices() throws RemoteException;

    // R
    boolean isDeclared(String name) throws RemoteException;

    void registerForNotifications(String name, IServiceCallback callback) throws RemoteException;

    void unregisterForNotifications(String name, IServiceCallback callback) throws RemoteException;

    String[] getDeclaredInstances(String iface) throws RemoteException;

    void registerClientCallback(String name, IBinder service, IClientCallback callback) throws RemoteException;

    void tryUnregisterService(String name, IBinder service) throws RemoteException;

    ServiceDebugInfo[] getServiceDebugInfo() throws RemoteException;
}
