package com.septrivium.augeo.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.septrivium.augeo.webresponse.DeviceProfile;

/**
 * Created by harold on 7/24/2015.
 */
public class AuGeoPreferenceManager {

    private static final String KEY_DEVICE_PROFILE = "device_profile";
    private static AuGeoPreferenceManager sInstance;
    private final Context context;
    private final SharedPreferences prefs;

    public static void init(Context context) {
        sInstance = new AuGeoPreferenceManager(context);
    }

    private AuGeoPreferenceManager(Context aContext) {
        context = aContext;
        prefs = PreferenceManager.getDefaultSharedPreferences(aContext);

    }

    public static AuGeoPreferenceManager getInstance() {
        if(sInstance == null)
            throw new IllegalStateException("You must call AuGeoPreferenceManger.init(context) first.");
        return sInstance;
    }

    public DeviceProfile getDeviceProfile() {
        String deviceProfileJsonString = prefs.getString(KEY_DEVICE_PROFILE, "");
        if(deviceProfileJsonString.isEmpty()) {
            return null;
        }

        Gson gson = new Gson();
        return gson.fromJson(deviceProfileJsonString, DeviceProfile.class);
    }

    public void saveDeviceProfie(DeviceProfile deviceProfile) {
        prefs.edit().putString(KEY_DEVICE_PROFILE, new Gson().toJson(deviceProfile)).apply();
    }
}
