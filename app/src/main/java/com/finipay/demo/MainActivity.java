package com.finipay.demo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.finipay.sdk.FiniPaySDK;
import com.finipay.sdk.api.models.PaymentResponse;
import com.finipay.sdk.sms.ParsedSms;
import com.finipay.sdk.util.PermissionHelper;

public class MainActivity extends AppCompatActivity {

    private TextView statusText;
    private Button toggleSmsBtn;
    private boolean isMonitoring = false;

    private FiniPaySDK getSdk() {
        return FiniPaySDK.getInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        toggleSmsBtn = findViewById(R.id.toggleSmsBtn);

        findViewById(R.id.toggleSmsBtn).setOnClickListener(v -> {
            if (isMonitoring) {
                stopSmsMonitoring();
            } else {
                startSmsMonitoring();
            }
        });

        findViewById(R.id.registerBtn).setOnClickListener(v -> showDeviceRegistrationDialog());
        findViewById(R.id.createPaymentBtn).setOnClickListener(v -> showPaymentDialog());

        updateStatus();
    }

    private void startSmsMonitoring() {
        if (!PermissionHelper.hasAllPermissions(this)) {
            PermissionHelper.requestPermissions(this);
            return;
        }

        FiniPaySDK sdk = getSdk();
        if (sdk == null) return;

        sdk.startSmsMonitoring(new FiniPaySDK.SmsCallback() {
            @Override
            public void onSmsReceived(String sender, String body, ParsedSms parsed) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this,
                        "SMS from: " + sender, Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onSmsForwarded(String sender, boolean matched) {
                runOnUiThread(() -> statusText.setText(
                        "Last SMS from " + sender + ": " + (matched ? "MATCHED" : "forwarded")));
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this,
                        "Error: " + message, Toast.LENGTH_LONG).show());
            }
        });

        isMonitoring = true;
        updateStatus();
    }

    private void stopSmsMonitoring() {
        FiniPaySDK sdk = getSdk();
        if (sdk != null) sdk.stopSmsMonitoring();
        isMonitoring = false;
        updateStatus();
    }

    private void showDeviceRegistrationDialog() {
        EditText emailInput = new EditText(this);
        emailInput.setHint("Email (e.g., merchant@example.com)");

        EditText keyInput = new EditText(this);
        keyInput.setHint("Device Key");

        EditText nameInput = new EditText(this);
        nameInput.setHint("Device Name (e.g., Samsung S24)");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);
        layout.addView(emailInput);
        layout.addView(keyInput);
        layout.addView(nameInput);

        new AlertDialog.Builder(this)
                .setTitle("Register Device")
                .setView(layout)
                .setPositiveButton("Register", (dialog, which) -> {
                    FiniPaySDK sdk = getSdk();
                    if (sdk == null) return;

                    sdk.registerDevice(
                            emailInput.getText().toString(),
                            keyInput.getText().toString(),
                            nameInput.getText().toString(),
                            new FiniPaySDK.Callback<Boolean>() {
                                @Override
                                public void onSuccess(Boolean result) {
                                    runOnUiThread(() -> Toast.makeText(MainActivity.this,
                                            "Device registered!", Toast.LENGTH_SHORT).show());
                                }

                                @Override
                                public void onError(String error) {
                                    runOnUiThread(() -> Toast.makeText(MainActivity.this,
                                            "Failed: " + error, Toast.LENGTH_LONG).show());
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showPaymentDialog() {
        EditText amountInput = new EditText(this);
        amountInput.setHint("Amount (BDT)");
        amountInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        new AlertDialog.Builder(this)
                .setTitle("Create Payment")
                .setView(amountInput)
                .setPositiveButton("Create", (dialog, which) -> {
                    String amountStr = amountInput.getText().toString();
                    double amount;
                    try {
                        amount = Double.parseDouble(amountStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FiniPaySDK sdk = getSdk();
                    if (sdk == null) return;

                    sdk.createPayment(amount,
                            "https://yourapp.com/success",
                            "https://yourapp.com/cancel",
                            null, "Test Customer", "customer@example.com",
                            new FiniPaySDK.Callback<PaymentResponse>() {
                                @Override
                                public void onSuccess(PaymentResponse response) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(MainActivity.this,
                                                "Payment URL created!", Toast.LENGTH_SHORT).show();
                                        if (response.getPaymentUrl() != null) {
                                            Intent intent = new Intent(Intent.ACTION_VIEW,
                                                    Uri.parse(response.getPaymentUrl()));
                                            startActivity(intent);
                                        }
                                    });
                                }

                                @Override
                                public void onError(String error) {
                                    runOnUiThread(() -> Toast.makeText(MainActivity.this,
                                            "Error: " + error, Toast.LENGTH_LONG).show());
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateStatus() {
        statusText.setText(isMonitoring ? "SMS Monitoring: ACTIVE" : "SMS Monitoring: INACTIVE");
        toggleSmsBtn.setText(isMonitoring ? "Stop Monitoring" : "Start Monitoring");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionHelper.isSmsGranted(requestCode, permissions, grantResults)) {
            startSmsMonitoring();
        } else {
            Toast.makeText(this, "SMS permission required", Toast.LENGTH_LONG).show();
        }
    }
}
