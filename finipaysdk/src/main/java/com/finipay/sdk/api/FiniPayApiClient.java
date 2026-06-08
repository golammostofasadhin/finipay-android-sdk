package com.finipay.sdk.api;

import com.finipay.sdk.api.models.*;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FiniPayApiClient {

    private final FiniPayApi api;
    private final String apiKey;

    private FiniPayApiClient(String baseUrl, String apiKey, boolean enableLogging) {
        this.apiKey = apiKey;

        OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS);

        if (enableLogging) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpBuilder.addInterceptor(logging);
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(httpBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(FiniPayApi.class);
    }

    public void createPayment(PaymentRequest request, Callback<PaymentResponse> callback) {
        new Thread(() -> {
            try {
                Map<String, Object> params = new HashMap<>();
                params.put("amount", request.getAmount());
                params.put("success_url", request.getSuccessUrl());
                params.put("cancel_url", request.getCancelUrl());
                params.put("cus_name", request.getCusName() != null ? request.getCusName() : "Default Name");
                params.put("cus_email", request.getCusEmail() != null ? request.getCusEmail() : "default@gmail.com");
                params.put("return_type", request.getReturnType() != null ? request.getReturnType() : "GET");

                if (request.getWebhookUrl() != null) {
                    params.put("webhook_url", request.getWebhookUrl());
                }
                if (request.getMetadata() != null) {
                    params.put("metadata", new Gson().toJson(request.getMetadata()));
                }

                Response<PaymentResponse> response = api.createPayment(apiKey, params).execute();
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("API error: " + response.code() + " " + response.message());
                }
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    public void verifyPayment(String transactionId, Callback<PaymentResponse> callback) {
        new Thread(() -> {
            try {
                Response<PaymentResponse> response = api.verifyPayment(apiKey, transactionId).execute();
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("API error: " + response.code() + " " + response.message());
                }
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    public void deviceConnect(DeviceConnectRequest request, Callback<DeviceConnectResponse> callback) {
        new Thread(() -> {
            try {
                Map<String, String> params = new HashMap<>();
                params.put("user_email", request.getUserEmail());
                params.put("device_key", request.getDeviceKey());
                params.put("device_name", request.getDeviceName());

                Response<DeviceConnectResponse> response = api.deviceConnect(params).execute();
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("API error: " + response.code() + " " + response.message());
                }
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    public void addSmsData(String message, String address, Callback<SmsDataResponse> callback) {
        new Thread(() -> {
            try {
                Map<String, Object> params = new HashMap<>();
                params.put("message", message);
                params.put("address", address);

                Response<SmsDataResponse> response = api.addSmsData(params).execute();
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("API error: " + response.code() + " " + response.message());
                }
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public static class Builder {
        private String baseUrl = "https://payment.axuretech.bio6.top/";
        private String apiKey = "";
        private boolean enableLogging = false;

        public Builder setBaseUrl(String url) {
            this.baseUrl = url;
            return this;
        }

        public Builder setApiKey(String key) {
            this.apiKey = key;
            return this;
        }

        public Builder enableLogging(boolean enable) {
            this.enableLogging = enable;
            return this;
        }

        public FiniPayApiClient build() {
            if (apiKey == null || apiKey.isEmpty()) {
                throw new IllegalArgumentException("API key must not be blank");
            }
            if (baseUrl == null || baseUrl.isEmpty()) {
                throw new IllegalArgumentException("Base URL must not be blank");
            }
            String url = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
            return new FiniPayApiClient(url, apiKey, enableLogging);
        }
    }
}
