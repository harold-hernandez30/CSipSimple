package com.augeo.vpnhelper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import java.util.List;

import de.blinkt.openvpn.api.APIVpnProfile;
import de.blinkt.openvpn.api.ExternalAppDatabase;
import de.blinkt.openvpn.api.IOpenVPNAPIService;
import de.blinkt.openvpn.api.IOpenVPNStatusCallback;


/**
 * Created by harold on 7/6/2015.
 */
public class OpenVpnHelper implements Handler.Callback {

    private static final int MSG_UPDATE_STATE = 0;
    private static final int MSG_UPDATE_MYIP = 1;
    private static final int START_PROFILE_EMBEDDED = 2;
    private static final int START_PROFILE_BYUUID = 3;
    public static final int ICS_OPENVPN_PERMISSION = 7;
    private static final int PROFILE_ADD_NEW = 8;
    public static final int ANDROID_REQUEST_PERMISSION = 9;
    public static final int HACK_ANDROID_REQUEST_REINSTALL_FIX = 10;

    public static final String ACTION_BROADCAST_VPN_CONNECTED = "action.vpnstatus.connected";
    public static final String ACTION_BROADCAST_VPN_PERMISSION_OK = "action.vpnpermission.ok";
    public static final CharSequence CONNECTED_SUCESS_STATUS = "SUCCESS";

    protected IOpenVPNAPIService mService = null;
    private Handler mHandler;
    private StatusListener mListener;
    private static OpenVpnHelper sInstance;
    private static boolean isInited = false;
    private String mPackageName;


    private IOpenVPNStatusCallback mCallback;


    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection;
    private String mStartUUID = null;


    private boolean isBound = false;
    private boolean isVpnConnected = false;

    public interface StatusListener {
        void onVpnConnected();

        void onVpnServiceConnected(Intent i, int status);

        void onVpnFailed();

    }
    private OpenVpnHelper() {}

    public static OpenVpnHelper getInstance() {
        if(sInstance == null) {
            sInstance = new OpenVpnHelper();
        }

        return sInstance;
    }

    public boolean hasPermission(Context context) {
        return (new ExternalAppDatabase(context).isAllowed(context.getPackageName()));
    }


    public void registerStatusListener(StatusListener listener) {
        mListener = listener;
    }

    public void init(Context context, StatusListener listener) throws RemoteException {
        if(!isInited) {
            mHandler = new Handler(this);
            mPackageName = context.getPackageName();
            Log.d("OpenVPNLog", "init");
            mCallback = generateIOpenVPNStatusCallBack();
            mConnection = buildServiceConnection();
            mListener = listener;
            bindService(context);
        }

    }

    public void init(Context context) throws RemoteException {
        init(context, null);
    }

    private IOpenVPNStatusCallback generateIOpenVPNStatusCallBack() {
        return new IOpenVPNStatusCallback.Stub() {
            /**
             * This is called by the remote service regularly to tell us about
             * new values.  Note that IPC calls are dispatched through a thread
             * pool running in each process, so the code executing here will
             * NOT be running in our main thread like most other things -- so,
             * to update the UI, we need to use a Handler to hop over there.
             */

            @Override
            public void newStatus(String uuid, String state, String message, String level)
                    throws RemoteException {
                Message msg = Message.obtain(mHandler, MSG_UPDATE_STATE, state + "|" + message);
                msg.sendToTarget();

                if(message.contains(CONNECTED_SUCESS_STATUS)) {
                    isVpnConnected = true;
                    android.util.Log.d("VPN_SUCCESS", "message contains success");
                    if(mListener != null) {
                        mListener.onVpnConnected();
                    }
                } else {

                    android.util.Log.d("VPN_SUCCESS", "other messages: " + message);
                }


            }

        };
    }


    private ServiceConnection buildServiceConnection() {
        return new ServiceConnection() {
            public void onServiceConnected(ComponentName className,
                                           IBinder service) {
                // This is called when the connection with the service has been
                // established, giving us the service object we can use to
                // interact with the service.  We are communicating with our
                // service through an IDL interface, so get a client-side
                // representation of that from the raw service object.

                Log.d("OpenVPNLog", "onVpnServiceConnected");
                mService = IOpenVPNAPIService.Stub.asInterface(service);
                isBound = true;
                Intent intent = null;
                try {
                    intent = mService.prepare(mPackageName); //OpenVPN permission
                    Log.d("OPEN_VPN_PERMISSION", "OpenVPN permission, intent: " + intent);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                if(intent != null) {
                    if(mListener != null) {
                        mListener.onVpnServiceConnected(intent, ICS_OPENVPN_PERMISSION);
                    }
                } else {
                    try {
                        // Request permission to use the API
                        Intent i = mService.prepareVPNService(); //Android permission
                        Log.d("OPEN_VPN_PERMISSION", "Android permission. intent: " + i);
                        if(i != null) {
                            if(mListener != null) {
                                mListener.onVpnServiceConnected(i, ANDROID_REQUEST_PERMISSION);
                            }
                        } else {
                            registerToServiceCallback(); //register anyway
                            if(mListener != null) {
                                mListener.onVpnServiceConnected(i, HACK_ANDROID_REQUEST_REINSTALL_FIX);
                            }
                        }

                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }


            }

            public void onServiceDisconnected(ComponentName className) {
                // This is called when the connection with the service has been
                // unexpectedly disconnected -- that is, its process crashed.
                mService = null;
                isBound = false;
                isVpnConnected = false;

            }
        };
    }

    private void bindService(Context context) {

        Intent icsopenvpnService = new Intent(IOpenVPNAPIService.class.getName());
        icsopenvpnService.setPackage(context.getPackageName());

        context.bindService(icsopenvpnService, mConnection, Context.BIND_AUTO_CREATE);
    }

    protected void listVPNs() {

        try {
            List<APIVpnProfile> list = mService.getProfiles();
            String all = "List:";
            for (APIVpnProfile vp : list.subList(0, Math.min(5, list.size()))) {
                all = all + vp.mName + ":" + vp.mUUID + "\n";
            }
//
            if (list.size() > 5)
                all += "\n And some profiles....";
//
            if (list.size() > 0) {
                mStartUUID = list.get(0).mUUID;
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
        }
    }

    public void unbindService(Context context) {
        if(mConnection != null) {
            context.unbindService(mConnection);
            mListener = null;
        }
    }

    public void onStop(Context context) {
        unbindService(context);
    }

    public void connect(Context context) throws RemoteException {
        init(context);
    }

    public void disconnect() {
        try {
            if(mService != null) {
//                unbindService(context);
                mService.disconnect();
                mService = null;
            }
            isVpnConnected = false;
            isBound = false;
            mListener = null;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public void registerToServiceCallback() {
        listVPNs();
        try {
            if(mService != null) {
                mService.registerStatusCallback(mCallback);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
        }
    }


    @Override
    public boolean handleMessage(Message msg) {
        //TODO: add status message listener
        if (msg.what == MSG_UPDATE_STATE) {
            if (mListener != null) {
                String message = (String) msg.obj;
                if(message.toLowerCase().contains("CONNECTED")) {
                    isVpnConnected = true;
                }
            }
        }
        return true;
    }


    public boolean isBound() {
        return isBound;
    }

    public boolean isVpnConnected() {
        return isVpnConnected;
    }
}
