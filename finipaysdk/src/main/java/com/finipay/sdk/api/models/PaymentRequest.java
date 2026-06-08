package com.finipay.sdk.api.models;

import java.util.Map;

public class PaymentRequest {
    private double amount;
    private String successUrl;
    private String cancelUrl;
    private String webhookUrl;
    private String cusName;
    private String cusEmail;
    private Map<String, String> metadata;
    private String returnType;

    public PaymentRequest(double amount, String successUrl, String cancelUrl) {
        this.amount = amount;
        this.successUrl = successUrl;
        this.cancelUrl = cancelUrl;
        this.cusName = "Default Name";
        this.cusEmail = "default@gmail.com";
        this.returnType = "GET";
    }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getSuccessUrl() { return successUrl; }
    public void setSuccessUrl(String successUrl) { this.successUrl = successUrl; }

    public String getCancelUrl() { return cancelUrl; }
    public void setCancelUrl(String cancelUrl) { this.cancelUrl = cancelUrl; }

    public String getWebhookUrl() { return webhookUrl; }
    public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }

    public String getCusName() { return cusName; }
    public void setCusName(String cusName) { this.cusName = cusName; }

    public String getCusEmail() { return cusEmail; }
    public void setCusEmail(String cusEmail) { this.cusEmail = cusEmail; }

    public Map<String, String> getMetadata() { return metadata; }
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }

    public String getReturnType() { return returnType; }
    public void setReturnType(String returnType) { this.returnType = returnType; }
}
