package com.septrivium.augeo.helper;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;

import com.csipsimple.api.SipProfile;
import com.csipsimple.widgets.AccountWidgetProvider;
import com.septrivium.augeo.persistence.AuGeoPreferenceManager;
import com.septrivium.augeo.rxhelper.RetryWithDelay;
import com.septrivium.augeo.siphelper.sipprofilehelper.SipProfileBuilder;
import com.septrivium.augeo.siphelper.sipprofilehelper.SipProfileDatabaseHelper;
import com.septrivium.augeo.ui.SipHome;
import com.septrivium.augeo.vpnhelper.ConfigConverter;
import com.septrivium.augeo.vpnhelper.OpenVpnHelper;
import com.septrivium.augeo.webapihelper.AuGeoWebAPIManager;
import com.septrivium.augeo.webresponse.AuGeoDeviceResponse;
import com.septrivium.augeo.webresponse.DeviceProfile;

import java.io.IOException;

import de.blinkt.openvpn.LaunchVPN;
import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.ConfigParser;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * Created by harold on 7/27/2015.
 */
public class AuGeoServiceFlowManager {

    private AppFlowCallback mListener;
    private OpenVPNStatusListener mOpenVpnStatusListener;
    private ResultData mResultData;
    private static AuGeoServiceFlowManager sInstance;
    private boolean isStarted = false;

    public static AuGeoServiceFlowManager getInstance() {
        if(sInstance == null) {
            sInstance = new AuGeoServiceFlowManager();
        }
        return sInstance;
    }

    private AuGeoServiceFlowManager(){}

    public void startServices(final Context context, final String deviceId) {
        if(isStarted) return;
        isStarted = true;
        requestDeviceProfileObservable(deviceId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<DeviceProfile>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(final DeviceProfile deviceProfile) {
                        processResultData(context, deviceProfile);
                    }
                });

    }

    private void processResultData(final Context context, final DeviceProfile deviceProfile) {
        getResultData(context, deviceProfile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResultData>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ResultData resultData) {
                        startVPN(context, resultData.getVpnProfile());

                        mOpenVpnStatusListener = new OpenVPNStatusListener(context, resultData.getSipProfile(), mListener);

                        try {
                            OpenVpnHelper.getInstance().init(context, mOpenVpnStatusListener);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private Observable<ResultData> getResultData(Context context, DeviceProfile deviceProfile) {
        return Observable.zip(
                createVpnProfile(context, deviceProfile),
                convertToSipProfile(deviceProfile),
                new Func2<VpnProfile, SipProfile, ResultData>() {
                    @Override
                    public ResultData call(VpnProfile vpnProfile, SipProfile sipProfile) {
                        mResultData = new ResultData(vpnProfile, sipProfile);
                        return mResultData;
                    }
                }
        );
    }

    private Observable<DeviceProfile> requestDeviceProfileObservable(String deviceID) {

        return AuGeoWebAPIManager.getInstance().getWebService().requestDeviceProfileObservable(deviceID)
                .map(new Func1<AuGeoDeviceResponse, DeviceProfile>() {
                    @Override
                    public DeviceProfile call(AuGeoDeviceResponse deviceResponse) {

                        DeviceProfile deviceProfile = null;
                        if (deviceResponse != null && deviceResponse.getResponse() != null && !deviceResponse.getResponse().isEmpty()) {
                            deviceProfile = deviceResponse.getResponse().get(0);
                            if (mListener != null) {
                                mListener.onDeviceProfileReceived(deviceProfile);
                            }
                            AuGeoPreferenceManager.getInstance().saveDeviceProfie(deviceProfile);
                        } else {
                            if (mListener != null) {
                                mListener.onDeviceProfileRetreiveFailed();
                            }
                        }

                        return deviceProfile;
                    }
                }).retryWhen(new RetryWithDelay(3));
    }

    private Observable<VpnProfile> createVpnProfile(final Context context, final DeviceProfile deviceProfile) {
        return Observable.create(new Observable.OnSubscribe<VpnProfile>() {
            @Override
            public void call(Subscriber<? super VpnProfile> subscriber) {
                try {
                    subscriber.onNext(new ConfigConverter(context).doImportFromAsset("augeo_android.ovpn", deviceProfile));
                    subscriber.onCompleted();
                } catch (IOException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                } catch (ConfigParser.ConfigParseError configParseError) {
                    configParseError.printStackTrace();
                    subscriber.onError(configParseError);
                }
            }
        });
    }

    private Observable<SipProfile> convertToSipProfile(final DeviceProfile deviceProfile) {
        return Observable.create(new Observable.OnSubscribe<SipProfile>() {
            @Override
            public void call(Subscriber<? super SipProfile> subscriber) {
                subscriber.onNext(SipProfileBuilder.generateFromDeviceProfile(deviceProfile));
                subscriber.onCompleted();
            }
        });
    }

    public void registerAppFlowCallbackListener(AppFlowCallback appFlowCallback) {
        mListener = appFlowCallback;
    }

    public void disconnectVpn() {
        OpenVpnHelper.getInstance().disconnect();
    }

    private class OpenVPNStatusListener implements OpenVpnHelper.StatusListener {

        private Context context;
        private SipProfile sipProfile;
        private AppFlowCallback listener;

        public OpenVPNStatusListener(Context context, SipProfile sipProfile, AppFlowCallback listener) {
            this.context = context;
            this.sipProfile = sipProfile;
            this.listener = listener;
        }

        @Override
        public void onVpnConnected() {
            Log.d("APP_FLOW", "VPN Connected. (should wait 3 seconds before creating profile.)");

            SipProfileDatabaseHelper.createProfileAndRegister(context, sipProfile);
            listener.onSipAccountSavedToDatabase(sipProfile);
            AccountWidgetProvider.updateWidget(context);
        }

        @Override
        public void onVpnServiceConnected(Intent i, int status) {

        }

        @Override
        public void onVpnFailed() {

        }
    }

//    public void updateAllRegistered() {
//        ContentValues cv = new ContentValues();
//        cv.put(SipProfile.FIELD_ACTIVE, true);
//        mContext.getContentResolver().update(ContentUris.withAppendedId(SipProfile.ACCOUNT_ID_URI_BASE, sipAccount.id), cv, null, null);
//    }

    private class ResultData {
        private VpnProfile vpnProfile;
        private SipProfile sipProfile;

        public ResultData(VpnProfile vpnProfile, SipProfile sipProfile) {
            this.vpnProfile = vpnProfile;
            this.sipProfile = sipProfile;
        }

        public SipProfile getSipProfile() {
            return sipProfile;
        }

        public void setSipProfile(SipProfile sipProfile) {
            this.sipProfile = sipProfile;
        }

        public VpnProfile getVpnProfile() {
            return vpnProfile;
        }

        public void setVpnProfile(VpnProfile vpnProfile) {
            this.vpnProfile = vpnProfile;
        }
    }

    private void startVPN(Context context, VpnProfile profile) {
        Intent intent = new Intent(context, LaunchVPN.class);
        intent.putExtra(LaunchVPN.EXTRA_KEY, profile.getUUID().toString());
        intent.setAction(Intent.ACTION_MAIN);
        context.startActivity(intent);

        android.util.Log.d("LAUNCH_VPN", "AuGeoAppFlowManager.startVPN");
    }


    public void updateAllRegistered(Context context) {
        ContentValues cv = new ContentValues();
        cv.put(SipProfile.FIELD_ACTIVE, true);
        if (mResultData != null && mResultData.getSipProfile() != null) {
            context.getContentResolver().update(ContentUris.withAppendedId(SipProfile.ACCOUNT_ID_URI_BASE, mResultData.getSipProfile().id), cv, null, null);
        }
    }
}
