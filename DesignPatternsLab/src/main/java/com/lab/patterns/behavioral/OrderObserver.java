package com.lab.patterns.behavioral;

// --- OBSERVER PATTERN ---
public interface OrderObserver {
    void onOrderConfirmed(String orderId, double finalAmount);
}
