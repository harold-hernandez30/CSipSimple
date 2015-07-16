package com.augeo.helper;

import com.augeo.webresponse.DeviceProfile;
import com.csipsimple.api.SipProfile;

import de.blinkt.openvpn.VpnProfile;

/**
 * Created by harold on 7/16/2015.
 */
public interface AppFlowCallback {

    void onVpnAuthCredentialsRecieved(VpnProfile sipProfile);

    void onDeviceProfileReceived(DeviceProfile deviceProfile);

    void onDeviceProfileRetreiveFailed();

    void onVpnConnected();

    void onSipAccountSavedToDatabase(SipProfile sipProfile);

}
