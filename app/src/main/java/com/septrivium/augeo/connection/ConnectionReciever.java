package com.septrivium.augeo.connection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.VpnService;
import android.telephony.TelephonyManager;

import com.septrivium.augeo.helper.AuGeoServiceFlowManager;

public class ConnectionReciever extends BroadcastReceiver {

    //Do we need an instance of AuGeoAppFlowManager?

    /**
     * This class should handle trying to re-start the open vpn service.
     * Classes will create an instance of this and pass the AuGeoAppFlowManager
     *
     * Should be able to try to re-start the app flow when connected to Internet
     *
     * Try to handle case where already connected to the Internet and lost connection
     * - Not sure yet how the vpn handles this
     * -- How to restart the vpn service?
     * -- Should be able to update the isVpnConnected variable
     *
     */
    public ConnectionReciever() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if(isConnected) {

            if((VpnService.prepare(context) == null)){

                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                final String deviceID = telephonyManager.getDeviceId();
                AuGeoServiceFlowManager.getInstance().startServices(context, deviceID);
//                android.util.Log.d("APP_FLOW_MANAGER", "start from: " + Log.getStackTraceString(new Throwable()));
            }
        } else { // DISCONNECTED
            // Update value of VPN isConnected.
                AuGeoServiceFlowManager.getInstance().disconnectVpn();
        }
    }
}
