package com.lab.patterns.factory;

// Extension: Add Webhook without modifying existing classes
class WebhookNotificationSender implements NotificationSender {
    @Override
    public void send(String recipient, String message) {
        System.out.println("  [Webhook POST -> " + recipient + "] Payload: " + message);
    }

    @Override
    public String getChannelName() {
        return "WEBHOOK";
    }
}
