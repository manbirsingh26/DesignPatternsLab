package com.lab.patterns.factory;

// NEW (my extension): Push channel added without editing any existing class (OCP)
class PushNotificationSender implements NotificationSender {
    @Override
    public void send(String recipient, String message) {
        System.out.println("  [Push -> device " + recipient + "] Alert: " + message);
    }

    @Override
    public String getChannelName() {
        return "PUSH";
    }
}
