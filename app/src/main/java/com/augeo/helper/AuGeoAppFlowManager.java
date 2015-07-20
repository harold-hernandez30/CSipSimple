package com.augeo.helper;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.augeo.siphelper.sipprofilehelper.SipProfileBuilder;
import com.augeo.siphelper.sipprofilehelper.SipProfileDatabaseHelper;
import com.augeo.vpnhelper.ConfigConverter;
import com.augeo.vpnhelper.OpenVpnConfigManager;
import com.augeo.vpnhelper.OpenVpnHelper;
import com.augeo.webapihelper.AuGeoWebAPIManager;
import com.augeo.webresponse.AuGeoDeviceResponse;
import com.augeo.webresponse.DeviceProfile;
import com.csipsimple.api.SipProfile;
import com.csipsimple.widgets.AccountWidgetProvider;

import java.io.IOException;

import de.blinkt.openvpn.LaunchVPN;
import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.ConfigParser;

/**
 * Created by harold on 7/16/2015.
 */
public class AuGeoAppFlowManager {

    private AppFlowCallback mListener;
    private Context mContext;
    private Handler mHandler;
    private DeviceProfile deviceProfile;
    private VpnProfile vpnProfile;
    private boolean isStarted = false; //started run already

    private class AppFlowRunnable implements Runnable {

        @Override
        public void run() {

            try {
                if(deviceProfile == null) {
                    deviceProfile = registerDeviceProfileReceived();
                }
                if(deviceProfile == null) return;
            } catch (Exception e) {
                e.printStackTrace();
                //TODO: Add specific error message to return to the mListener. Like "Network error."
                return;
            }


            OpenVpnConfigManager.getInstance().saveVpnUsername(deviceProfile.getVpnUsername());
            OpenVpnConfigManager.getInstance().saveVpnPassword(deviceProfile.getVpnPassword());
            try {
                if(vpnProfile == null) {
                    vpnProfile = new ConfigConverter(mContext).doImportFromAsset("augeo_android.ovpn");
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (!OpenVpnHelper.getInstance().isVpnConnected()) {
                                OpenVpnHelper.getInstance().init(mContext, new OpenVPNStatusListener(deviceProfile, mListener));
                                startVPN(vpnProfile);
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ConfigParser.ConfigParseError configParseError) {
                configParseError.printStackTrace();
            }

        }

    }

    //should this class handle asynchronous waiting? Probably yes.

    public AuGeoAppFlowManager(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
    }

    /**
     * start() should only run once. Otherwise, it will spawn multiple threads trying to connect to connect to the vpn
     * multiple times causing the vpn connection to fail.
     *
     */
    public void start() {
        if(!isStarted) {
            isStarted = true;
            new Thread(new AppFlowRunnable()).start();
        }
    }


    private DeviceProfile registerDeviceProfileReceived() throws Exception{
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        final String deviceID = telephonyManager.getDeviceId();

        AuGeoDeviceResponse deviceResponse = AuGeoWebAPIManager.getInstance().getWebService().requestDeviceProfile(deviceID);
        DeviceProfile deviceProfile = null;
        if (deviceResponse != null && deviceResponse.getResponse() != null && !deviceResponse.getResponse().isEmpty()) {
            deviceProfile = deviceResponse.getResponse().get(0);
            if(mListener != null) {
                mListener.onDeviceProfileReceived(deviceProfile);
            }
        } else {
            if(mListener != null) {
                mListener.onDeviceProfileRetreiveFailed();
            }
        }

        return deviceProfile;
    }


    public void registerAppFlowCallbackListener(AppFlowCallback listener) {
        mListener = listener;
    }


    private void startVPN(VpnProfile profile) {
        Intent intent = new Intent(mContext, LaunchVPN.class);
        intent.putExtra(LaunchVPN.EXTRA_KEY, profile.getUUID().toString());
        intent.setAction(Intent.ACTION_MAIN);
        mContext.startActivity(intent);
    }

    public void disconnect() {
        OpenVpnHelper.getInstance().disconnect();
        isStarted = false;
    }

    private class OpenVPNStatusListener implements OpenVpnHelper.StatusListener {

        private DeviceProfile deviceProfile;
        private AppFlowCallback listener;

        public OpenVPNStatusListener(DeviceProfile deviceProfile, AppFlowCallback listener) {
            this.deviceProfile = deviceProfile;
            this.listener = listener;
        }

        @Override
        public void onVpnConnected() {
            Log.d("APP_FLOW", "Creating profile and register");
            listener.onVpnConnected();
            mHandler.post(new Runnable() {
                @Override
                public void run() {

                    SipProfile sipAccount = SipProfileBuilder.generateFromDeviceProfile(deviceProfile);
                    sipAccount.active = true;
                    SipProfileDatabaseHelper.createProfileAndRegister(mContext, sipAccount);
//                    updateAllRegistered(sipAccount);
                    listener.onSipAccountSavedToDatabase(sipAccount);

                    AccountWidgetProvider.updateWidget(mContext);
                }
            });

        }

        @Override
        public void onVpnServiceConnected(Intent i, int status) {

        }

        @Override
        public void onVpnFailed() {

        }
    }

    public void updateAllRegistered(SipProfile sipProfile) {
        ContentValues cv = new ContentValues();
        cv.put(SipProfile.FIELD_ACTIVE, true);
        mContext.getContentResolver().update(ContentUris.withAppendedId(SipProfile.ACCOUNT_ID_URI_BASE, sipProfile.id), cv, null, null);
    }

    public Context getContext() {
        return mContext;
    }
}