package com.lab.patterns.factory;

// 1. Product interface
public interface NotificationSender {
    void send(String recipient, String message);
    String getChannelName();
}
