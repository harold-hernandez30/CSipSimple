package com.septrivium.augeo.webresponse;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by harold on 7/15/2015.
 */
public class DeviceProfile {

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

    private List<SpeedDialButton> buttons;

    @SerializedName("site_id")
    private String siteId;


    @SerializedName("vpn_server")
    private String vpnServerDomain;

    @SerializedName("vpn_port")
    private String vpnServerPort;

    public String getSipUpsername() {
        return sipUpsername;
    }

    public List<SpeedDialButton> getButtons() {
        return buttons;
    }

    public void setButtons(List<SpeedDialButton> buttons) {
        this.buttons = buttons;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getVpnServerDomain() {
        return vpnServerDomain;
    }

    public void setVpnServerDomain(String vpnServerDomain) {
        this.vpnServerDomain = vpnServerDomain;
    }

    public String getVpnServerPort() {
        return vpnServerPort;
    }

    public void setVpnServerPort(String vpnServerPort) {
        this.vpnServerPort = vpnServerPort;
    }

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
