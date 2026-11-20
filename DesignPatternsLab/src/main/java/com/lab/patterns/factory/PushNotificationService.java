package com.lab.patterns.factory;

// NEW (my extension): concrete creator for the Push channel.
public class PushNotificationService extends NotificationService {
    @Override
    protected NotificationSender createSender() {
        return new PushNotificationSender();
    }
}
