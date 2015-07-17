package com.augeo.webapihelper;

import java.io.IOException;
import java.net.SocketTimeoutException;

import retrofit.ErrorHandler;
import retrofit.RetrofitError;
import retrofit.client.Client;
import retrofit.client.Request;
import retrofit.client.Response;

/**
 * Created by harold on 7/18/2015.
 */
public class RetrofitErrorHandler implements ErrorHandler {

    @Override
    public Throwable handleError(RetrofitError cause) {

        return new RetrofitNetworkException(cause.getMessage());
    }

}