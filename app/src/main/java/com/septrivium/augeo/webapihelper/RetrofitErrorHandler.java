package com.septrivium.augeo.webapihelper;

import retrofit.ErrorHandler;
import retrofit.RetrofitError;

/**
 * Created by harold on 7/18/2015.
 */
public class RetrofitErrorHandler implements ErrorHandler {

    @Override
    public Throwable handleError(RetrofitError cause) {

        return new RetrofitNetworkException(cause.getMessage());
    }

}