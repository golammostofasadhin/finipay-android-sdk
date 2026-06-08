package com.finipay.sdk.api.models;

public class DeviceConnectRequest {
    private String userEmail;
    private String deviceKey;
    private String deviceName;

    public DeviceConnectRequest(String userEmail, String deviceKey, String deviceName) {
        this.userEmail = userEmail;
        this.deviceKey = deviceKey;
        this.deviceName = deviceName;
    }

    public String getUserEmail() { return userEmail; }
    public String getDeviceKey() { return deviceKey; }
    public String getDeviceName() { return deviceName; }
}
