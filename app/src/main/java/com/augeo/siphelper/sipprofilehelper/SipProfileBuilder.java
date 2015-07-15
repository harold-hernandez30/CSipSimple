package com.augeo.siphelper.sipprofilehelper;

import android.content.Context;

import com.augeo.vpnhelper.OpenVpnConfigManager;
import com.augeo.webresponse.DeviceProfile;
import com.csipsimple.api.SipProfile;
import com.csipsimple.api.SipUri;
import com.csipsimple.db.DBProvider;

/**
 * Created by harold on 7/16/2015.
 */
public class SipProfileBuilder {

    public static SipProfile generateFromDeviceProfile(DeviceProfile deviceProfile) {
        SipProfile account = new SipProfile();
        account.display_name = deviceProfile.getSipUsername();


        account.acc_id = "<sip:"
                + SipUri.encodeUser(account.display_name) + "@" + deviceProfile.getServerIp() + ">";

        String regUri = "sip:" + deviceProfile.getServerIp();
        account.reg_uri = regUri;
        account.proxies = new String[]{regUri};


        account.realm = "*";
        account.username = deviceProfile.getSipUsername();
        account.data = deviceProfile.getSipPassword();

        account.scheme = SipProfile.CRED_SCHEME_DIGEST;
        account.datatype = SipProfile.CRED_DATA_PLAIN_PASSWD;

        account.reg_timeout = 5 * 1000;
        account.transport = SipProfile.TRANSPORT_TCP;

        return account;
    }
}
