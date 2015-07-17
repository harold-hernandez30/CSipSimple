package com.augeo.helper;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.VpnService;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.augeo.siphelper.sipprofilehelper.SipProfileBuilder;
import com.augeo.siphelper.sipprofilehelper.SipProfileDatabaseHelper;
import com.augeo.vpnhelper.ConfigConverter;
import com.augeo.vpnhelper.OpenVpnConfigManager;
import com.augeo.vpnhelper.OpenVpnHelper;
import com.augeo.webapihelper.AuGeoWebAPIManager;
import com.augeo.webresponse.AuGeoDeviceResponse;
import com.augeo.webresponse.DeviceProfile;
import com.csipsimple.api.SipProfile;
import com.csipsimple.utils.AccountListUtils;
import com.csipsimple.widgets.AccountWidgetProvider;

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

    public void start() {
        //no need to ask for dialog

        new Thread(new Runnable() {
            @Override
            public void run() {

                final DeviceProfile deviceProfile;
                try {
                    deviceProfile = registerDeviceProfileReceived();
                    if(deviceProfile == null) return;
                } catch (Exception e) {
                    e.printStackTrace();
                    //TODO: Add specific error message to return to the mListener. Like "Network error."
                    return;
                }


                OpenVpnConfigManager.getInstance().saveVpnUsername(deviceProfile.getVpnUsername());
                OpenVpnConfigManager.getInstance().saveVpnPassword(deviceProfile.getVpnPassword());
                try {
                    final VpnProfile vpnProfile = new ConfigConverter(mContext).doImportFromAsset("augeo_android.ovpn");
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

//                new ConfigProfileTask(mContext, sipProfile).execute();
            }
        }).start();
    }


    private DeviceProfile registerDeviceProfileReceived() throws Exception{
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        final String deviceID = telephonyManager.getDeviceId();
        //DEBUG FOR EMULATOR ONLY: 357441053465113

        AuGeoDeviceResponse deviceResponse = AuGeoWebAPIManager.getInstance().getWebService().requestDeviceProfile(deviceID);
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
}