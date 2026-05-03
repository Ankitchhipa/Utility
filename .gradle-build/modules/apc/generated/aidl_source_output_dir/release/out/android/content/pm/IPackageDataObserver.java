/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: /Users/ankitchhipa/Library/Android/sdk/build-tools/35.0.0/aidl -p/Users/ankitchhipa/Library/Android/sdk/platforms/android-36/framework.aidl -o/Users/ankitchhipa/AndroidStudioProjects/Toolkit/.gradle-build/modules/apc/generated/aidl_source_output_dir/release/out -I/Users/ankitchhipa/AndroidStudioProjects/Toolkit/apc/src/main/aidl -I/Users/ankitchhipa/AndroidStudioProjects/Toolkit/apc/src/release/aidl -I/Users/ankitchhipa/.gradle/caches/8.14.3/transforms/03d9c7f4817ec562b688043c658fc9a2/transformed/media-1.0.0/aidl -I/Users/ankitchhipa/.gradle/caches/8.14.3/transforms/a7e2e5697d9c0469edfc48759a08e1d0/transformed/core-1.17.0/aidl -I/Users/ankitchhipa/.gradle/caches/8.14.3/transforms/e3e07d4a01ff289bf2ff59ba9c8d406a/transformed/versionedparcelable-1.1.1/aidl -d/var/folders/yb/s_df3hcj6vgbryqb5wwkd4zh0000gn/T/aidl17707041594346844424.d /Users/ankitchhipa/AndroidStudioProjects/Toolkit/apc/src/main/aidl/android/content/pm/ipackagedataobserver.aidl
 */
package android.content.pm;
/**
 * API for package data change related callbacks from the Package Manager.
 * Some usage scenarios include deletion of cache directory, generate
 * statistics related to code, data, cache usage(TODO)
 * {@hide}
 */
public interface IPackageDataObserver extends android.os.IInterface
{
  /** Default implementation for IPackageDataObserver. */
  public static class Default implements android.content.pm.IPackageDataObserver
  {
    @Override public void onRemoveCompleted(java.lang.String packageName, boolean succeeded) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements android.content.pm.IPackageDataObserver
  {
    /** Construct the stub at attach it to the interface. */
    @SuppressWarnings("this-escape")
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an android.content.pm.IPackageDataObserver interface,
     * generating a proxy if needed.
     */
    public static android.content.pm.IPackageDataObserver asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof android.content.pm.IPackageDataObserver))) {
        return ((android.content.pm.IPackageDataObserver)iin);
      }
      return new android.content.pm.IPackageDataObserver.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      java.lang.String descriptor = DESCRIPTOR;
      if (code >= android.os.IBinder.FIRST_CALL_TRANSACTION && code <= android.os.IBinder.LAST_CALL_TRANSACTION) {
        data.enforceInterface(descriptor);
      }
      if (code == INTERFACE_TRANSACTION) {
        reply.writeString(descriptor);
        return true;
      }
      switch (code)
      {
        case TRANSACTION_onRemoveCompleted:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          boolean _arg1;
          _arg1 = (0!=data.readInt());
          this.onRemoveCompleted(_arg0, _arg1);
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements android.content.pm.IPackageDataObserver
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public java.lang.String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      @Override public void onRemoveCompleted(java.lang.String packageName, boolean succeeded) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(packageName);
          _data.writeInt(((succeeded)?(1):(0)));
          boolean _status = mRemote.transact(Stub.TRANSACTION_onRemoveCompleted, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
    }
    /** @hide */
    public static final java.lang.String DESCRIPTOR = "android.content.pm.IPackageDataObserver";
    static final int TRANSACTION_onRemoveCompleted = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
  }
  public void onRemoveCompleted(java.lang.String packageName, boolean succeeded) throws android.os.RemoteException;
}
