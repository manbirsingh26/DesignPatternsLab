package com.lab.patterns.behavioral;

public class LoyaltyPointsObserver implements OrderObserver {
    @Override
    public void onOrderConfirmed(String orderId, double finalAmount) {
        int pointsEarned = (int) Math.floor(finalAmount / 10.0);
        System.out.println("  [LoyaltyPointsObserver] Order " + orderId
                + " awarded " + pointsEarned + " loyalty points.");
    }
}
