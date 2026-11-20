package com.lab.patterns.behavioral;

public class NoDiscountStrategy implements DiscountStrategy {
    @Override
    public double applyDiscount(double originalAmount) {
        return originalAmount;
    }

    @Override
    public String getStrategyName() {
        return "NO_DISCOUNT";
    }
}
