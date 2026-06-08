package com.finipay.sdk.sms;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsParser {

    public static ParsedSms parse(String senderAddress, String messageBody) {
        String text = messageBody.toLowerCase();
        Double amount = extractAmount(text);
        String transactionId = extractTransactionId(text);
        boolean isPayment = isPaymentRelated(senderAddress, text);
        String sender = normalizeSender(senderAddress);

        return new ParsedSms(amount, transactionId, sender, isPayment);
    }

    private static Double extractAmount(String text) {
        Pattern[] patterns = {
                Pattern.compile("(?:tk\\.?\\s*|taka\\s*|amount[:\\s]*taka\\s*)?(\\d+(?:,\\d{3})*(?:\\.\\d{1,3})?)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("received\\s+tk\\.?\\s*(\\d+(?:\\.\\d{1,3})?)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("credited\\s+by\\s+tk\\.?\\s*(\\d+(?:\\.\\d{1,3})?)", Pattern.CASE_INSENSITIVE)
        };

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String cleaned = matcher.group(1).replace(",", "");
                try {
                    return Double.parseDouble(cleaned);
                } catch (NumberFormatException ignored) {}
            }
        }
        return null;
    }

    private static String extractTransactionId(String text) {
        Pattern[] patterns = {
                Pattern.compile("trx[:\\s]*([a-zA-Z0-9]+)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("transaction[:\\s]*([a-zA-Z0-9]+)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("trxid[:\\s]*([a-zA-Z0-9]+)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("ref[:\\s]*([a-zA-Z0-9]+)", Pattern.CASE_INSENSITIVE)
        };

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String id = matcher.group(1);
                if (id.length() >= 8) return id;
            }
        }
        return null;
    }

    private static boolean isPaymentRelated(String sender, String text) {
        String[] paymentKeywords = {
                "received", "credited", "payment", "cash in", "cashout",
                "cash out", "sent money", "transfer"
        };
        String[] paymentSenders = {
                "bkash", "nagad", "rocket", "upay", "16216",
                "cellfin", "tap", "ipay", "surecash", "ok_wallet",
                "mcash", "easypaisa", "mycash"
        };

        String senderLower = sender.toLowerCase();
        for (String ps : paymentSenders) {
            if (senderLower.contains(ps)) return true;
        }
        for (String kw : paymentKeywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }

    private static String normalizeSender(String address) {
        String lower = address.toLowerCase().trim();
        if (lower.contains("bkash")) return "bkash";
        if (lower.contains("nagad")) return "nagad";
        if (lower.contains("rocket")) return "rocket";
        if (lower.equals("16216")) return "rocket";
        if (lower.contains("upay")) return "upay";
        if (lower.contains("cellfin")) return "cellfin";
        if (lower.contains("tap")) return "tap";
        if (lower.contains("ipay")) return "ipay";
        if (lower.contains("surecash") || lower.contains("sure cash")) return "surecash";
        if (lower.contains("ok_wallet") || lower.contains("ok wallet")) return "ok_wallet";
        if (lower.contains("mcash") || lower.contains("m cash")) return "mcash";
        if (lower.contains("easypaisa") || lower.contains("easy paisa")) return "easypaisa";
        if (lower.contains("mycash") || lower.contains("my cash")) return "mycash";
        return address;
    }
}
