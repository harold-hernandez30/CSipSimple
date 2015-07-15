package com.augeo.webapihelper;

import com.augeo.webresponse.AuGeoDeviceResponse;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by harold on 7/16/2015.
 */
public interface AuGeoWebService {

    @GET("/api.php")
    void requestDeviceProfile(@Query("EIN") String ein, Callback<AuGeoDeviceResponse> response); //async

    @GET("/api.php")
    AuGeoDeviceResponse requestDeviceProfile(@Query("EIN") String ein);
}
