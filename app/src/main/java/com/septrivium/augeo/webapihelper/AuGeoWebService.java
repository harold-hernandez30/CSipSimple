package com.septrivium.augeo.webapihelper;

import com.septrivium.augeo.webresponse.AuGeoDeviceResponse;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by harold on 7/16/2015.
 */
public interface AuGeoWebService {

    @GET("/api.php")
    void requestDeviceProfile(@Query("EIN") String ein, Callback<AuGeoDeviceResponse> response); //async

    @GET("/api.php")
    AuGeoDeviceResponse requestDeviceProfile(@Query("EIN") String ein);

    @GET("/api.php")
    Observable<AuGeoDeviceResponse> requestDeviceProfileObservable(@Query("EIN") String ein);
}
