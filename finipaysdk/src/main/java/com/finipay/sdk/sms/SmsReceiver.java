package com.finipay.sdk.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import com.finipay.sdk.FiniPaySDK;

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "FiniPaySmsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            return;
        }

        SmsMessage[] messages = getMessagesFromIntent(intent);
        if (messages == null) return;

        for (SmsMessage msg : messages) {
            String sender = msg.getOriginatingAddress();
            String body = msg.getMessageBody();
            long timestamp = msg.getTimestampMillis();

            if (sender == null || body == null) continue;

            ParsedSms parsed = SmsParser.parse(sender, body);
            if (!parsed.isPaymentSms()) continue;

            Log.d(TAG, "Payment SMS received from: " + sender);

            FiniPaySDK sdk = FiniPaySDK.getInstance();
            if (sdk != null) {
                sdk.onSmsReceived(sender, body, timestamp, parsed);
            }
        }
    }

    private SmsMessage[] getMessagesFromIntent(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return Telephony.Sms.Intents.getMessagesFromIntent(intent);
        }

        Bundle extras = intent.getExtras();
        if (extras == null) return null;

        Object[] pdus = (Object[]) extras.get("pdus");
        if (pdus == null) return null;

        SmsMessage[] messages = new SmsMessage[pdus.length];
        for (int i = 0; i < pdus.length; i++) {
            messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
        }
        return messages;
    }
}
