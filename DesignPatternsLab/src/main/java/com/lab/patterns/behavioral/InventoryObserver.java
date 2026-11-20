package com.lab.patterns.behavioral;

public class InventoryObserver implements OrderObserver {
    @Override
    public void onOrderConfirmed(String orderId, double finalAmount) {
        System.out.println("  [InventoryObserver] Reserved stock for order " + orderId
                + " (confirmed total: $" + String.format("%.2f", finalAmount) + ")");
    }
}
