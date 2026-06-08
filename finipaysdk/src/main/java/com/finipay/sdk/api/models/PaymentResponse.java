package com.finipay.sdk.api.models;

import com.google.gson.annotations.SerializedName;

public class PaymentResponse {
    @SerializedName("status")
    private int status;

    @SerializedName("message")
    private String message;

    @SerializedName("payment_url")
    private String paymentUrl;

    @SerializedName("transaction_id")
    private String transactionId;

    @SerializedName("error")
    private String error;

    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public String getPaymentUrl() { return paymentUrl; }
    public String getTransactionId() { return transactionId; }
    public String getError() { return error; }

    public boolean isSuccess() { return status == 1; }
}
