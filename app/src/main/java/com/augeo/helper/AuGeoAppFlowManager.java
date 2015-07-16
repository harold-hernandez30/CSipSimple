package com.augeo.helper;

import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.os.AsyncTask;
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

import java.io.IOException;
import java.lang.ref.WeakReference;

import de.blinkt.openvpn.LaunchVPN;
import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.ConfigParser;
import de.blinkt.openvpn.core.ProfileManager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by harold on 7/16/2015.
 */
public class AuGeoAppFlowManager {

    private AppFlowCallback mListener;
    private Context mContext;
    private Handler mHandler;


    //should this class handle asynchronous waiting? Probably yes.

    public AuGeoAppFlowManager(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
    }

    public void start(){
        //no need to ask for dialog

        new Thread(new Runnable() {
            @Override
            public void run() {

                final DeviceProfile deviceProfile = registerDeviceProfileReceived();

                OpenVpnConfigManager.getInstance().saveVpnUsername(deviceProfile.getVpnUsername());
                OpenVpnConfigManager.getInstance().saveVpnPassword(deviceProfile.getVpnPassword());
                try {
                    final VpnProfile vpnProfile = new ConfigConverter(mContext).doImportFromAsset("augeo_android.ovpn");


                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {


                            try {
                                OpenVpnHelper.getInstance().init(mContext, new OpenVPNStatusListener(deviceProfile));
                                startVPN(vpnProfile);
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

//                new ConfigProfileTask(mContext, sipProfile).execute();
            }
        }).start();
    }


    private DeviceProfile registerDeviceProfileReceived() {
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        final String deviceID = telephonyManager.getDeviceId();
        AuGeoDeviceResponse deviceResponse = AuGeoWebAPIManager.getInstance().getWebService().requestDeviceProfile(deviceID);

        AuGeoWebAPIManager.getInstance().getWebService().requestDeviceProfile(deviceID, new Callback<AuGeoDeviceResponse>() {
            @Override
            public void success(AuGeoDeviceResponse auGeoDeviceResponse, Response response) {

            }

            @Override
            public void failure(RetrofitError error) {

            }
        });

        DeviceProfile deviceProfile = null;
        if (deviceResponse != null && deviceResponse.getResponse() != null && !deviceResponse.getResponse().isEmpty()) {
            deviceProfile = deviceResponse.getResponse().get(0);
            mListener.onDeviceProfileReceived(deviceProfile);
        } else {
            mListener.onDeviceProfileRetreiveFailed();
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

    private class OpenVPNStatusListener implements OpenVpnHelper.StatusListener {

        private DeviceProfile deviceProfile;

        public OpenVPNStatusListener(DeviceProfile deviceProfile) {
            this.deviceProfile = deviceProfile;
        }

        @Override
        public void onVpnConnected() {
            Log.d("APP_FLOW", "Creating profile and register");
            SipProfileDatabaseHelper.createProfileAndRegister(mContext,  SipProfileBuilder.generateFromDeviceProfile(deviceProfile));

        }

        @Override
        public void onVpnServiceConnected(Intent i, int status) {

        }

        @Override
        public void onVpnFailed() {

        }
    }

}