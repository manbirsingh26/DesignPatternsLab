package com.lab.patterns.factory;

// 2. Concrete product
class SmsNotificationSender implements NotificationSender {
    @Override
    public void send(String recipient, String message) {
        System.out.println("  [SMS -> " + recipient + "] Text: " + message);
    }

    @Override
    public String getChannelName() {
        return "SMS";
    }
}
