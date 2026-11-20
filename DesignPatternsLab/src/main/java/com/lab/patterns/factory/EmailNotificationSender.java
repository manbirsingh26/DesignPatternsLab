package com.lab.patterns.factory;

// 2. Concrete product (package-private: clients only ever see NotificationSender)
class EmailNotificationSender implements NotificationSender {
    @Override
    public void send(String recipient, String message) {
        System.out.println("  [Email -> " + recipient + "] Subject: Lab Alert | Body: " + message);
    }

    @Override
    public String getChannelName() {
        return "EMAIL";
    }
}
