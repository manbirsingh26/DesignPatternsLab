package com.lab.patterns.behavioral;

public class AuditLogObserver implements OrderObserver {
    @Override
    public void onOrderConfirmed(String orderId, double finalAmount) {
        System.out.println("  [AuditLogObserver] Recorded audit entry -> orderId=" + orderId
                + ", finalAmount=$" + String.format("%.2f", finalAmount));
    }
}
