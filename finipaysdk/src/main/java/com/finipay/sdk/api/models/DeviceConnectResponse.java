package com.finipay.sdk.api.models;

import com.google.gson.annotations.SerializedName;

public class DeviceConnectResponse {
    @SerializedName("status")
    private int status;

    @SerializedName("message")
    private String message;

    @SerializedName("device_id")
    private String deviceId;

    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public String getDeviceId() { return deviceId; }
    public boolean isSuccess() { return status == 1; }
}
