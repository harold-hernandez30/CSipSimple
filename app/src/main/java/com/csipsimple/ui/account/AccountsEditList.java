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
import android.net.ConnectivityManager;
import android.net.VpnService;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.septrivium.augeo.connection.ConnectionReciever;
import com.septrivium.augeo.helper.AppFlowCallback;
import com.septrivium.augeo.helper.AuGeoAppFlowManager;
import com.septrivium.augeo.helper.AuGeoServiceFlowManager;
import com.septrivium.augeo.webresponse.DeviceProfile;
import com.csipsimple.R;
import com.csipsimple.api.SipProfile;
import com.csipsimple.utils.Compatibility;

import de.blinkt.openvpn.VpnProfile;

public class AccountsEditList extends SherlockFragmentActivity implements  AppFlowCallback {

    private static final int ANDROID_CONFIRM_DIALOG = 101;
    private Handler handler = new Handler();
    private BroadcastReceiver connectionReceiver;
    private String deviceID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        deviceID = telephonyManager.getDeviceId();
        AuGeoServiceFlowManager.getInstance().registerAppFlowCallbackListener(this);
        connectionReceiver = new ConnectionReciever();

        Intent intent = VpnService.prepare(this);
        if (intent != null) {
            startActivityForResult(intent, ANDROID_CONFIRM_DIALOG);

            android.util.Log.d("VPN_SERVICE_PREPARE", "AccountsEditList:onCreate()");
        } else {
            AuGeoServiceFlowManager.getInstance().startServices(this, deviceID);
        }
        setContentView(R.layout.accounts_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }

    private void launchVpnPermissionDialog() {
        Intent intent = VpnService.prepare(this);
        if (intent != null) {
            startActivityForResult(intent, ANDROID_CONFIRM_DIALOG);

            android.util.Log.d("VPN_SERVICE_PREPARE", "AccountsEditList:launchVpnPermissionDialog()");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(connectionReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(connectionReceiver);
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
    public void onVpnAuthCredentialsRecieved(VpnProfile sipProfile) {

    }

    @Override
    public void onDeviceProfileReceived(DeviceProfile deviceProfile) {

    }

    @Override
    public void onDeviceProfileRetreiveFailed() {

    }

    @Override
    public void onVpnConnected() {
        android.util.Log.d("ACCOUNT_LIST_FRAG", "=========================");
    }

    @Override
    public void onSipAccountSavedToDatabase(SipProfile sipProfile) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ANDROID_CONFIRM_DIALOG:
                    AuGeoServiceFlowManager.getInstance().startServices(this, deviceID);
                    break;
            }
        } else if (resultCode == RESULT_CANCELED) {
            switch (requestCode) {
                case ANDROID_CONFIRM_DIALOG:
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.warning)
                            .setMessage(getString(R.string.app_name) + " requires VPN service. Please click 'OK' on the 'Allow connection' dialog to use this service.")
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    launchVpnPermissionDialog();
                                }
                            })
//                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
////                                    SipHome.home.fetchData();
//                                }
//                            })
                            .show();
                    break;

            }
        }
    }
}
