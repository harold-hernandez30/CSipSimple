/**
 * Copyright (C) 2010-2012 Regis Montoya (aka r3gis - www.r3gis.fr)
 * This file is part of CSipSimple.
 * <p/>
 * CSipSimple is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * If you own a pjsip commercial license you can also redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public License
 * as an android library.
 * <p/>
 * CSipSimple is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with CSipSimple.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.csipsimple.ui.account;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.VpnService;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.augeo.siphelper.sipprofilehelper.SipProfileBuilder;
import com.augeo.siphelper.sipprofilehelper.SipProfileDatabaseHelper;
import com.augeo.ui.SipHome;
import com.augeo.vpnhelper.OpenVpnConfigManager;
import com.augeo.webapihelper.AuGeoWebAPIManager;
import com.augeo.webresponse.AuGeoDeviceResponse;
import com.augeo.webresponse.DeviceProfile;
import com.csipsimple.R;
import com.augeo.vpnhelper.ConfigConverter;
import com.augeo.vpnhelper.OpenVpnHelper;
import com.csipsimple.api.SipProfile;
import com.csipsimple.service.SipService;
import com.csipsimple.utils.Compatibility;
import com.csipsimple.utils.PreferencesWrapper;

import java.io.IOException;

import de.blinkt.openvpn.LaunchVPN;
import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.api.ConfirmDialog;
import de.blinkt.openvpn.core.ConfigParser;
import de.blinkt.openvpn.core.ProfileManager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AccountsEditList extends SherlockFragmentActivity implements OpenVpnHelper.StatusListener {

    private static final int CONFIRM_DIALOG = 100;
    private static final int ANDROID_CONFIRM_DIALOG = 101;
    private VpnProfile mVpnProfile;
    private BroadcastReceiver mConfirmDialogReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() == OpenVpnHelper.ACTION_BROADCAST_VPN_PERMISSION_OK) {
                if (mVpnProfile != null) {
                    startVPN(mVpnProfile);
                } else {
                    new ConfigProfileTask(true).execute();
                }
            }
        }
    };
    private SipProfile account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        launchVpnPermissionDialog();
        setContentView(R.layout.accounts_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        OpenVpnHelper.getInstance().registerStatusListener(AccountsEditList.this);


        startVpnSipRegisterProcess();

    }

    private void launchVpnPermissionDialog() {
        Intent intent = VpnService.prepare(this);
        if(intent != null) {
            startActivityForResult(intent, ANDROID_CONFIRM_DIALOG);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mConfirmDialogReceiver, new IntentFilter(OpenVpnHelper.ACTION_BROADCAST_VPN_PERMISSION_OK));

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mConfirmDialogReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == Compatibility.getHomeMenuId()) {
            finish();
            return true;
        }
        return false;
    }

    @Override
    public void onStatusChanged(String message) {

        if (message.contains(OpenVpnHelper.CONNECTED_SUCESS_STATUS)) {
            Intent intent = new Intent();
            intent.setAction(OpenVpnHelper.ACTION_BROADCAST_VPN_CONNECTED);
            sendBroadcast(intent);
        }
    }

    @Override
    public void onVpnServiceConnected(Intent intent, int requestCode) {

        switch (requestCode) {
            case OpenVpnHelper.ANDROID_REQUEST_PERMISSION:
                startActivity(intent);
                break;
            case OpenVpnHelper.ICS_OPENVPN_PERMISSION:
                startActivityForResult(intent, requestCode); //request for that permission
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case ANDROID_CONFIRM_DIALOG:
                    startVpnSipRegisterProcess();
                    break;

                case CONFIRM_DIALOG:
                    if (mVpnProfile != null) {
                        startVPN(mVpnProfile);
                    } else {
                        new ConfigProfileTask(true).execute();
                    }
                    break;
            }
        } else if (resultCode == RESULT_CANCELED) {
            switch (requestCode) {
                case ANDROID_CONFIRM_DIALOG:
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.warning)
                            .setMessage(getString(R.string.app_name) + " requires VPN. Please ")
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    launchVpnPermissionDialog();
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
//                                    SipHome.home.fetchData();
                                }
                            })
                            .show();
                    break;

            }
        }
    }

    private void startVpnSipRegisterProcess() {
        if (OpenVpnConfigManager.getInstance().hasVpnAuthCredentials() && !OpenVpnHelper.getInstance().isBound()) {
            new ConfigProfileTask(true).execute();
        }  else {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String deviceID = telephonyManager.getDeviceId();
            AuGeoWebAPIManager.getInstance().getWebService().requestDeviceProfile(deviceID, new Callback<AuGeoDeviceResponse>() {
                @Override
                public void success(AuGeoDeviceResponse auGeoDeviceResponse, Response response) {
                    Log.d("AuGeoWebAPIManager", "Account Edit LIst auGeoDeviceResponse: " + auGeoDeviceResponse);
                    DeviceProfile deviceProfile = auGeoDeviceResponse.getResponse().get(0);

                    OpenVpnConfigManager.getInstance().saveVpnUsername(deviceProfile.getVpnUsername());
                    OpenVpnConfigManager.getInstance().saveVpnPassword(deviceProfile.getVpnPassword());

                    account = SipProfileBuilder.generateFromDeviceProfile(deviceProfile);
                    new ConfigProfileTask(true).execute();



                }

                @Override
                public void failure(RetrofitError error) {
                    Log.d("AuGeoWebAPIManager", "error: " + error.getMessage());
                }
            });
        }
    }

    private class ConfigProfileTask extends AsyncTask<Void, Void, VpnProfile> {

        private boolean shouldLaunchVpn;

        public ConfigProfileTask(boolean shouldLaunchVpn) {

            this.shouldLaunchVpn = shouldLaunchVpn;
        }

        @Override
        protected VpnProfile doInBackground(Void... voids) {
            ConfigConverter configConverter = new ConfigConverter(AccountsEditList.this);
            try {
                return configConverter.doImportFromAsset("augeo_android.ovpn");
            } catch (IOException e) {
                e.printStackTrace();
                android.util.Log.d("OpenVPNMessage", e.getMessage());
                return null;
            } catch (ConfigParser.ConfigParseError configParseError) {
                configParseError.printStackTrace();
                android.util.Log.d("OpenVPNMessage", configParseError.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(VpnProfile vpnProfile) {
            super.onPostExecute(vpnProfile);
            mVpnProfile = vpnProfile;
            try {
                OpenVpnHelper.getInstance().init(AccountsEditList.this, AccountsEditList.this);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            ProfileManager.getInstance(AccountsEditList.this).saveProfile(AccountsEditList.this, vpnProfile);

            if (shouldLaunchVpn) {
                startVPN(vpnProfile);
            }
        }
    }

    private void startVPN(VpnProfile profile) {
        Intent intent = new Intent(this, LaunchVPN.class);
        intent.putExtra(LaunchVPN.EXTRA_KEY, profile.getUUID().toString());
        intent.setAction(Intent.ACTION_MAIN);
        startActivity(intent);
    }
}
