package com.lab.patterns.behavioral;

// --- STRATEGY PATTERN ---
public interface DiscountStrategy {
    double applyDiscount(double originalAmount);
    String getStrategyName();
}
