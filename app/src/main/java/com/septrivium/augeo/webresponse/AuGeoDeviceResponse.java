package com.septrivium.augeo.webresponse;

import java.util.List;

/**
 * Created by harold on 7/15/2015.
 */
public class AuGeoDeviceResponse {

    private List<DeviceProfile> devices;

    public List<DeviceProfile> getResponse() {
        return devices;
    }

    public void setResponse(List<DeviceProfile> response) {
        this.devices = response;
    }

    @Override
    public String toString() {
        return "AuGeoDeviceResponse{" +
                "devices=" + devices +
                '}';
    }
}
