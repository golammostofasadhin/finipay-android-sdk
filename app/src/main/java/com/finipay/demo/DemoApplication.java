package com.finipay.demo;

import android.app.Application;

import com.finipay.sdk.FiniPaySDK;

public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        new FiniPaySDK.Builder(this)
                .setApiKey("YOUR_BRAND_API_KEY_HERE")
                .setBaseUrl("https://payment.axuretech.bio6.top/")
                .enableLogging(true)
                .build();
    }
}
