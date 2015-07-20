package com.septrivium.augeo.vpnhelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by harold on 7/15/2015.
 */
public class OpenVpnConfigManager {

    private static final String KEY_BATTERY_MODE = "vpn_prefs_lifecycle";
    private static final String KEY_VPN_USERNAME = "vpn_username";
    private static final String KEY_VPN_PASSWORD = "vpn_password";
    private final SharedPreferences mPrefs;
    private static OpenVpnConfigManager sInstance;

    private OpenVpnConfigManager(Context context) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void init(Context context) {
        if (sInstance == null) {
            sInstance = new OpenVpnConfigManager(context);
        }
    }

    public static OpenVpnConfigManager getInstance() {
        if (sInstance == null) throw new IllegalStateException("Call OpenVpnConfigManager.init(context) first.");

        return sInstance;
    }


    public boolean isInBatterySavingMode() {
        return mPrefs.getBoolean(KEY_BATTERY_MODE, false);
    }

    public void saveVpnPassword(String password) {
        mPrefs.edit().putString(KEY_VPN_PASSWORD, password).apply();
    }

    public String getVpnPassword() {
        return mPrefs.getString(KEY_VPN_PASSWORD, "");
    }

    public void saveVpnUsername(String username) {
        mPrefs.edit().putString(KEY_VPN_USERNAME, username).apply();
    }

    public String getVpnUsername() {
        return mPrefs.getString(KEY_VPN_USERNAME, "");
    }

    public boolean hasVpnAuthCredentials() {
        return (!getVpnPassword().isEmpty() && !getVpnUsername().isEmpty());

    }


}
