package com.lab.patterns.behavioral;

public class PercentageDiscountStrategy implements DiscountStrategy {
    private final double discountPercent; // e.g., 15.0 means 15% off

    public PercentageDiscountStrategy(double discountPercent) {
        if (discountPercent < 0 || discountPercent > 100) {
            throw new IllegalArgumentException("Discount percent must be between 0 and 100.");
        }
        this.discountPercent = discountPercent;
    }

    @Override
    public double applyDiscount(double originalAmount) {
        double discounted = originalAmount * (1.0 - discountPercent / 100.0);
        return Math.max(0.0, discounted);
    }

    @Override
    public String getStrategyName() {
        return "PERCENTAGE_" + discountPercent + "%";
    }
}
