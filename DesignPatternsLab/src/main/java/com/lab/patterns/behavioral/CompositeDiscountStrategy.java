package com.lab.patterns.behavioral;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

// NEW (my extension): stacks several strategies, applied left to right.
// Example: 10% off, then $20 off orders of $100+.
// Added as a new class; Order and the existing strategies were not touched (OCP).
public class CompositeDiscountStrategy implements DiscountStrategy {
    private final List<DiscountStrategy> strategies;

    public CompositeDiscountStrategy(DiscountStrategy... strategies) {
        if (strategies == null || strategies.length == 0) {
            throw new IllegalArgumentException("At least one strategy is required.");
        }
        for (DiscountStrategy s : strategies) {
            Objects.requireNonNull(s, "Strategies cannot contain null.");
        }
        this.strategies = List.copyOf(Arrays.asList(strategies));
    }

    @Override
    public double applyDiscount(double originalAmount) {
        double amount = originalAmount;
        for (DiscountStrategy s : strategies) {
            amount = s.applyDiscount(amount);
        }
        return Math.max(0.0, amount);
    }

    @Override
    public String getStrategyName() {
        return "COMPOSITE[" + strategies.stream()
                .map(DiscountStrategy::getStrategyName)
                .collect(Collectors.joining(" -> ")) + "]";
    }
}
