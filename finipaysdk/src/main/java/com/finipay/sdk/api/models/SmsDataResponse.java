package com.finipay.sdk.api.models;

import com.google.gson.annotations.SerializedName;

public class SmsDataResponse {
    @SerializedName("status")
    private int status;

    @SerializedName("message")
    private String message;

    @SerializedName("matched")
    private boolean matched;

    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public boolean isMatched() { return matched; }
    public boolean isSuccess() { return status == 1; }
}
