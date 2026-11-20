package com.lab.patterns.behavioral;

public class TieredThresholdDiscountStrategy implements DiscountStrategy {
    private final double threshold;
    private final double deduction;

    public TieredThresholdDiscountStrategy(double threshold, double deduction) {
        if (threshold < 0 || deduction < 0) {
            throw new IllegalArgumentException("Threshold and deduction must be non-negative.");
        }
        this.threshold = threshold;
        this.deduction = deduction;
    }

    @Override
    public double applyDiscount(double originalAmount) {
        if (originalAmount >= threshold) {
            return Math.max(0.0, originalAmount - deduction);
        }
        return originalAmount;
    }

    @Override
    public String getStrategyName() {
        return "SPEND_" + threshold + "_SAVE_" + deduction;
    }
}
