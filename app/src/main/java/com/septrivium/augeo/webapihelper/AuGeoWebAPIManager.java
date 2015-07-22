package com.septrivium.augeo.webapihelper;

import retrofit.RestAdapter;

/**
 * Created by harold on 7/16/2015.
 */
public class AuGeoWebAPIManager {
    //String data = GET("http://portal.septrivium.com/api.php?EIN=" + deviceID);

    private RestAdapter mRestAdapter;
    private AuGeoWebService mWebService;

    private static AuGeoWebAPIManager sInstance;

    private AuGeoWebAPIManager() {
        if(mRestAdapter == null || mWebService == null) {
            mRestAdapter = new RestAdapter.Builder()
                    .setEndpoint("http://portal.septrivium.com")
                    .setErrorHandler(new RetrofitErrorHandler())
                    .build();

            mRestAdapter.setLogLevel(RestAdapter.LogLevel.FULL);

            mWebService = mRestAdapter.create(AuGeoWebService.class);

        }
    }

    public static AuGeoWebAPIManager getInstance() {
        if(sInstance == null) {
            sInstance = new AuGeoWebAPIManager();
        }
        return sInstance;
    }

    public AuGeoWebService getWebService() {
        return mWebService;
    }

    //For debugging:       String responseInString = new String(((TypedByteArray) response.getBody()).getBytes());
}
