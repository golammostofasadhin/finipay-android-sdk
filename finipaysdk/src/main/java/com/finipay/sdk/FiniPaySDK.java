package com.finipay.sdk;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.finipay.sdk.api.FiniPayApiClient;
import com.finipay.sdk.api.models.DeviceConnectRequest;
import com.finipay.sdk.api.models.DeviceConnectResponse;
import com.finipay.sdk.api.models.PaymentRequest;
import com.finipay.sdk.api.models.PaymentResponse;
import com.finipay.sdk.service.SmsForwardService;
import com.finipay.sdk.sms.ParsedSms;
import com.finipay.sdk.sms.SmsReceiver;

public class FiniPaySDK {

    private static final String TAG = "FiniPaySDK";
    private static FiniPaySDK instance;

    private final Context appContext;
    private final FiniPayApiClient apiClient;

    private SmsCallback smsCallback;
    private PaymentCallback paymentCallback;

    private FiniPaySDK(Context context, FiniPayApiClient client) {
        this.appContext = context.getApplicationContext();
        this.apiClient = client;
        instance = this;
    }

    public FiniPayApiClient getApiClient() {
        return apiClient;
    }

    public void registerDevice(String email, String deviceKey, String deviceName,
                               final Callback<Boolean> callback) {
        DeviceConnectRequest request = new DeviceConnectRequest(email, deviceKey, deviceName);
        apiClient.deviceConnect(request, new FiniPayApiClient.Callback<DeviceConnectResponse>() {
            @Override
            public void onSuccess(DeviceConnectResponse result) {
                callback.onSuccess(result.isSuccess());
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void createPayment(double amount, String successUrl, String cancelUrl,
                              String webhookUrl, String cusName, String cusEmail,
                              final Callback<PaymentResponse> callback) {
        PaymentRequest request = new PaymentRequest(amount, successUrl, cancelUrl);
        if (webhookUrl != null) request.setWebhookUrl(webhookUrl);
        if (cusName != null) request.setCusName(cusName);
        if (cusEmail != null) request.setCusEmail(cusEmail);

        apiClient.createPayment(request, new FiniPayApiClient.Callback<PaymentResponse>() {
            @Override
            public void onSuccess(PaymentResponse result) {
                if (paymentCallback != null) {
                    paymentCallback.onPaymentCreated(result);
                }
                callback.onSuccess(result);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void verifyPayment(String transactionId, final Callback<PaymentResponse> callback) {
        apiClient.verifyPayment(transactionId, new FiniPayApiClient.Callback<PaymentResponse>() {
            @Override
            public void onSuccess(PaymentResponse result) {
                if (paymentCallback != null) {
                    paymentCallback.onPaymentVerified(result);
                }
                callback.onSuccess(result);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void startSmsMonitoring(SmsCallback callback) {
        if (!hasSmsPermission()) {
            Log.w(TAG, "SMS permission not granted");
            callback.onError("SMS permission not granted");
            return;
        }
        this.smsCallback = callback;
        SmsForwardService.start(appContext);
        Log.d(TAG, "SMS monitoring started");
    }

    public void stopSmsMonitoring() {
        SmsForwardService.stop(appContext);
        this.smsCallback = null;
        Log.d(TAG, "SMS monitoring stopped");
    }

    public void setPaymentCallback(PaymentCallback callback) {
        this.paymentCallback = callback;
    }

    public boolean hasSmsPermission() {
        return ContextCompat.checkSelfPermission(appContext, Manifest.permission.RECEIVE_SMS)
                == PackageManager.PERMISSION_GRANTED;
    }

    public void onSmsReceived(String sender, String body, long timestamp, ParsedSms parsed) {
        if (smsCallback != null) {
            smsCallback.onSmsReceived(sender, body, parsed);
        }
    }

    public void onSmsForwarded(String sender, boolean matched) {
        if (smsCallback != null) {
            smsCallback.onSmsForwarded(sender, matched);
        }
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public interface SmsCallback {
        void onSmsReceived(String sender, String body, ParsedSms parsed);
        void onSmsForwarded(String sender, boolean matched);
        void onError(String message);
    }

    public interface PaymentCallback {
        void onPaymentCreated(PaymentResponse response);
        void onPaymentVerified(PaymentResponse response);
        void onError(String message);
    }

    public static FiniPaySDK getInstance() {
        return instance;
    }

    public static class Builder {
        private final Context context;
        private String baseUrl = "https://payment.axuretech.bio6.top/";
        private String apiKey = "";
        private boolean enableLogging = false;

        public Builder(Context context) {
            this.context = context;
        }

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

        public FiniPaySDK build() {
            if (apiKey == null || apiKey.isEmpty()) {
                throw new IllegalStateException("API key must be set before building FiniPaySDK");
            }

            FiniPayApiClient client = new FiniPayApiClient.Builder()
                    .setBaseUrl(baseUrl)
                    .setApiKey(apiKey)
                    .enableLogging(enableLogging)
                    .build();

            return new FiniPaySDK(context, client);
        }
    }
}
