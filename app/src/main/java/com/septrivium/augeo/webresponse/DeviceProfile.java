package com.septrivium.augeo.webresponse;

import com.google.gson.annotations.SerializedName;

/**
 * Created by harold on 7/15/2015.
 */
public class DeviceProfile {
/**
 *
 * {"devices":[
 {"device_id":"13",
 "ein":"357441053465113",
 "mac":"","protocol":"sip",
 "server":"172.27.3.15",
 "username":"223","password":"027bbd169197dc36cada5f6e6fa7f640",
 "vpn_username":"richard",
 "vpn_password":"richard"}]}


 {
    "devices":[
        {
        "device_id":"13",
        "ein":"357441053465113",
        "mac":"","protocol":"sip",
        "server":"172.27.3.15",
        "username":"223",
        "password":"027bbd169197dc36cada5f6e6fa7f640"
        }
    ],
    "vpn_username":"richard",
    "vpn_password":"richard"
 }

 {
 "devices":[
        {
         "device_id":"13",
         "ein":"357441053465113",
         "mac":"","protocol":"sip",
         "server":"172.27.3.15",
         "username":"223",
         "password":"027bbd169197dc36cada5f6e6fa7f640",
         "vpn_username":"richard",
         "vpn_password":"richard"
        }
    ]
 }
 */

    @SerializedName("device_id")
    private String deviceId;
    private String ein;
    private String mac;

    @SerializedName("server")
    private String serverIp;

    @SerializedName("username")
    private String sipUpsername;

    @SerializedName("password")
    private String sipPassword;

    @SerializedName("vpn_username")
    private String vpnUsername;

    @SerializedName("vpn_password")
    private String vpnPassword;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getEin() {
        return ein;
    }

    public void setEin(String ein) {
        this.ein = ein;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public String getSipUsername() {
        return sipUpsername;
    }

    public void setSipUpsername(String sipUpsername) {
        this.sipUpsername = sipUpsername;
    }

    public String getSipPassword() {
        return sipPassword;
    }

    public void setSipPassword(String sipPassword) {
        this.sipPassword = sipPassword;
    }

    public String getVpnUsername() {
        return vpnUsername;
    }

    public void setVpnUsername(String vpnUsername) {
        this.vpnUsername = vpnUsername;
    }

    public String getVpnPassword() {
        return vpnPassword;
    }

    public void setVpnPassword(String vpnPassword) {
        this.vpnPassword = vpnPassword;
    }

    @Override
    public String toString() {
        return "DeviceProfile{" +
                "deviceId='" + deviceId + '\'' +
                ", ein='" + ein + '\'' +
                ", mac='" + mac + '\'' +
                ", serverIp='" + serverIp + '\'' +
                ", sipUpsername='" + sipUpsername + '\'' +
                ", sipPassword='" + sipPassword + '\'' +
                ", vpnUsername='" + vpnUsername + '\'' +
                ", vpnPassword='" + vpnPassword + '\'' +
                '}';
    }
}
