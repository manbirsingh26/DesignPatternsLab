package com.lab.patterns.factory;

// 4. Concrete creator
// (public so the runner package can use it; the starter had it package-private)
public class EmailNotificationService extends NotificationService {
    @Override
    protected NotificationSender createSender() {
        return new EmailNotificationSender();
    }
}
