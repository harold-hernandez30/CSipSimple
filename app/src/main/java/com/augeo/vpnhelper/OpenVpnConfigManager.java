package com.augeo.vpnhelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by harold on 7/15/2015.
 */
public class OpenVpnConfigManager {

    private static final String KEY_BATTERY_MODE = "vpn_prefs_lifecycle";
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

}
