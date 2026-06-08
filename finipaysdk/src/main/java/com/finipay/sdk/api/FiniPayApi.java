package com.finipay.sdk.api;

import com.finipay.sdk.api.models.*;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface FiniPayApi {

    @POST("api/payment/create")
    Call<PaymentResponse> createPayment(
            @Header("API-KEY") String apiKey,
            @Body Map<String, Object> body
    );

    @GET("api/payment/verify")
    Call<PaymentResponse> verifyPayment(
            @Header("API-KEY") String apiKey,
            @Query("transaction_id") String transactionId
    );

    @POST("api/device-connect")
    Call<DeviceConnectResponse> deviceConnect(
            @Body Map<String, String> body
    );

    @POST("api/add-data")
    Call<SmsDataResponse> addSmsData(
            @Body Map<String, Object> body
    );
}
