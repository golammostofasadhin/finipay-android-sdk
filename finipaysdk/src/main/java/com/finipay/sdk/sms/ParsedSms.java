package com.finipay.sdk.sms;

public class ParsedSms {
    private Double amount;
    private String transactionId;
    private String sender;
    private boolean paymentSms;

    public ParsedSms(Double amount, String transactionId, String sender, boolean paymentSms) {
        this.amount = amount;
        this.transactionId = transactionId;
        this.sender = sender;
        this.paymentSms = paymentSms;
    }

    public Double getAmount() { return amount; }
    public String getTransactionId() { return transactionId; }
    public String getSender() { return sender; }
    public boolean isPaymentSms() { return paymentSms; }
}
