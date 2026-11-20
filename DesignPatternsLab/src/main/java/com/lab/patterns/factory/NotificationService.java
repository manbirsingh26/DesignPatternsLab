package com.lab.patterns.factory;

// 3. Creator abstract class
public abstract class NotificationService {

    // Factory Method: subclasses decide which concrete NotificationSender to instantiate
    protected abstract NotificationSender createSender();

    // Template/business method relies only on the NotificationSender abstraction
    public void notifyUser(String recipient, String message) {
        // Validation happens BEFORE createSender() is called
        if (recipient == null || recipient.isBlank()) {
            throw new IllegalArgumentException("Recipient must not be empty.");
        }
        NotificationSender sender = createSender();
        System.out.println("[FactoryMethod] Channel selected: " + sender.getChannelName());
        sender.send(recipient, message);
    }
}
