# FiniPay Android SDK

একটি Android SDK যা FiniPay পেমেন্ট গেটওয়ে আপনার অ্যাপে ইন্টিগ্রেট করার জন্য তৈরি। SDK টি SMS-ভিত্তিক পেমেন্ট ভেরিফিকেশন সাপোর্ট করে — bKash, Nagad, Rocket, Upay সহ সব বাংলাদেশি মোবাইল ব্যাংকিং সার্ভিস।

## Features

- **SMS মনিটরিং** — পেমেন্ট এসএমএস অটোমেটিক্যালি ডিটেক্ট করে
- **পেমেন্ট ক্রিয়েট** — FiniPay API এর মাধ্যমে পেমেন্ট রিকোয়েস্ট তৈরি
- **পেমেন্ট ভেরিফিকেশন** — পেমেন্ট স্ট্যাটাস চেক
- **ডিভাইস রেজিস্ট্রেশন** — SMS ফরওয়ার্ড করার জন্য ডিভাইস রেজিস্টার
- **ব্যাকগ্রাউন্ড সার্ভিস** — ফোরগ্রাউন্ড সার্ভিস হিসেবে নির্ভরযোগ্য SMS মনিটরিং

## Integration

### Step 1: JitPack থেকে SDK অ্যাড করুন

**root** `build.gradle.kts` বা `settings.gradle.kts` এ JitPack রিপোজিটরি যোগ করুন:

```kotlin
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}
```

**app** `build.gradle.kts` এ ডিপেন্ডেন্সি অ্যাড করুন:

```kotlin
dependencies {
    implementation("com.github.golammostofasadhin:finipay-android-sdk:v1.0.0")
}
```

### Step 2: SDK initialize করুন

আপনার `Application` class এ:

```java
public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        new FiniPaySDK.Builder(this)
                .setApiKey("YOUR_BRAND_API_KEY")
                .setBaseUrl("https://payment.axuretech.bio6.top/")
                .enableLogging(true)
                .build();
    }
}
```

### Step 3: Permission রিকোয়েস্ট

```java
if (!PermissionHelper.hasAllPermissions(this)) {
    PermissionHelper.requestPermissions(this);
}
```

Result হ্যান্ডেল করুন:

```java
@Override
public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    if (PermissionHelper.isSmsGranted(requestCode, permissions, grantResults)) {
        startSmsMonitoring();
    }
}
```

### Step 4: SMS মনিটরিং শুরু

```java
FiniPaySDK sdk = FiniPaySDK.getInstance();

sdk.startSmsMonitoring(new FiniPaySDK.SmsCallback() {
    @Override
    public void onSmsReceived(String sender, String body, ParsedSms parsed) {
        // পেমেন্ট SMS ডিটেক্ট হয়েছে
    }

    @Override
    public void onSmsForwarded(String sender, boolean matched) {
        // SMS সার্ভারে ফরওয়ার্ড করা হয়েছে
        if (matched) { /* পেমেন্ট অটো-ভেরিফাইড! */ }
    }

    @Override
    public void onError(String message) {
        Log.e("FiniPay", "Error: " + message);
    }
});
```

### Step 5: পেমেন্ট তৈরি

```java
sdk.createPayment(500.0,
        "https://yourapp.com/success",
        "https://yourapp.com/cancel",
        null, "Customer Name", "customer@email.com",
        new FiniPaySDK.Callback<PaymentResponse>() {
            @Override
            public void onSuccess(PaymentResponse response) {
                // paymentUrl খুলুন
                String url = response.getPaymentUrl();
            }

            @Override
            public void onError(String error) { }
        });
```

### Step 6: ডিভাইস রেজিস্টার করুন (মার্চেন্ট)

```java
sdk.registerDevice("merchant@email.com", "DEVICE_KEY", "Samsung S24",
        new FiniPaySDK.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean registered) { }

            @Override
            public void onError(String error) { }
        });
```

## Architecture

```
Mobile Banking (bKash/Nagad/Rocket)
        │
        │ SMS পাঠায়
        ▼
Android Device (FiniPay SDK)
  ┌──────────────┐
  │ SmsReceiver  │── BroadcastReceiver (SMS ডিটেক্ট)
  └──────┬───────┘
  ┌──────▼───────┐
  │ SmsParser    │── amount, TrxID এক্সট্রাক্ট
  └──────┬───────┘
  ┌──────▼──────────┐
  │ SmsForwardService│── Foreground Service (সার্ভারে পাঠায়)
  └──────┬───────────┘
  ┌──────▼────────┐
  │ FiniPayApiClient│── Retrofit HTTP Client
  └──────┬─────────┘
         │
         ▼
FiniPay Server (payment.axuretech.bio6.top)
```

## Supported Payment Methods (SMS Recognition)

| Method | SMS Sender |
|--------|------------|
| bKash | bkash |
| Nagad | nagad |
| Rocket | rocket, 16216 |
| Upay | upay |
| Cellfin | cellfin |
| Tap | tap |
| Ipay | ipay |
| Sure Cash | surecash |
| OK Wallet | ok_wallet |
| mCash | mcash |
| Easy Paisa | easypaisa |
| myCash | mycash |

## Requirements

- Minimum SDK: 21 (Android 5.0)
- Compile SDK: 34
- Java 17+
- Permissions: `RECEIVE_SMS`, `READ_SMS`, `INTERNET`, `FOREGROUND_SERVICE`
