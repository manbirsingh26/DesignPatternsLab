package com.lab.patterns.factory;

// 4. Concrete creator
public class SmsNotificationService extends NotificationService {
    @Override
    protected NotificationSender createSender() {
        return new SmsNotificationSender();
    }
}
