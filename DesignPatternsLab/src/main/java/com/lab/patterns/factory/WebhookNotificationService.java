package com.lab.patterns.factory;

// 4. Concrete creator for the Webhook extension.
// Adding this class required ZERO edits to NotificationService or EmailNotificationSender.
public class WebhookNotificationService extends NotificationService {
    @Override
    protected NotificationSender createSender() {
        return new WebhookNotificationSender();
    }
}
