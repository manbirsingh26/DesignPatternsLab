package com.lab.patterns.behavioral;

import com.lab.patterns.factory.NotificationService;
import java.util.Objects;

// NEW (my extension): an observer that reuses Task 1's Factory Method.
// It depends only on the abstract NotificationService (DIP), so the same observer
// can send by Email, SMS, Webhook or Push depending on which service is injected.
public class CustomerNotificationObserver implements OrderObserver {
    private final NotificationService notificationService;
    private final String recipient;

    public CustomerNotificationObserver(NotificationService notificationService, String recipient) {
        this.notificationService = Objects.requireNonNull(notificationService,
                "NotificationService cannot be null.");
        this.recipient = recipient;
    }

    @Override
    public void onOrderConfirmed(String orderId, double finalAmount) {
        notificationService.notifyUser(recipient,
                "Your order " + orderId + " is confirmed. Total: $" + String.format("%.2f", finalAmount));
    }
}
