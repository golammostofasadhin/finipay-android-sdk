package com.finipay.sdk.service;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.finipay.sdk.FiniPaySDK;
import com.finipay.sdk.api.FiniPayApiClient;
import com.finipay.sdk.api.models.SmsDataResponse;
import com.finipay.sdk.sms.ParsedSms;

import java.util.LinkedList;
import java.util.Queue;

public class SmsForwardService extends Service {

    private static final String TAG = "FiniPaySmsService";
    private static final String CHANNEL_ID = "finipay_sms_channel";
    private static final int NOTIFICATION_ID = 9001;
    public static final String ACTION_STOP = "com.finipay.sdk.action.STOP_SMS_SERVICE";

    private final Queue<SmsEvent> smsQueue = new LinkedList<>();
    private boolean processing = false;
    private Thread processingThread;

    public static class SmsEvent {
        public final String sender;
        public final String body;
        public final long timestamp;
        public final ParsedSms parsed;

        public SmsEvent(String sender, String body, long timestamp, ParsedSms parsed) {
            this.sender = sender;
            this.body = body;
            this.timestamp = timestamp;
            this.parsed = parsed;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(NOTIFICATION_ID, createNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopSelf();
            return START_NOT_STICKY;
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public synchronized void queueSms(SmsEvent event) {
        smsQueue.add(event);
        if (!processing) {
            processQueue();
        }
    }

    private synchronized void processQueue() {
        processing = true;
        processingThread = new Thread(() -> {
            while (true) {
                SmsEvent event;
                synchronized (this) {
                    if (smsQueue.isEmpty()) {
                        processing = false;
                        return;
                    }
                    event = smsQueue.poll();
                }
                if (event != null) {
                    forwardSms(event);
                }
            }
        });
        processingThread.setDaemon(true);
        processingThread.start();
    }

    private void forwardSms(SmsEvent event) {
        try {
            FiniPaySDK sdk = FiniPaySDK.getInstance();
            if (sdk == null) return;

            sdk.getApiClient().addSmsData(event.body, event.sender,
                    new FiniPayApiClient.Callback<SmsDataResponse>() {
                        @Override
                        public void onSuccess(SmsDataResponse result) {
                            Log.d(TAG, "SMS forwarded, matched: " + result.isMatched());
                            sdk.onSmsForwarded(event.sender, result.isMatched());
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "Failed to forward SMS: " + error);
                            synchronized (smsQueue) {
                                smsQueue.add(event);
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error forwarding SMS: " + e.getMessage());
        }
    }

    private Notification createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "FiniPay SMS Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Forwards payment SMS to server");
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("FiniPay")
                .setContentText("Monitoring payment SMS...")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }

    @Override
    public void onDestroy() {
        if (processingThread != null) {
            processingThread.interrupt();
        }
        super.onDestroy();
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, SmsForwardService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void stop(Context context) {
        Intent intent = new Intent(context, SmsForwardService.class);
        intent.setAction(ACTION_STOP);
        context.startService(intent);
    }
}
