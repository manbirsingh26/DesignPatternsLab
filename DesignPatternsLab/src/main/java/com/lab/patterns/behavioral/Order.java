package com.lab.patterns.behavioral;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

// --- CONTEXT (Strategy) & SUBJECT (Observer) ---
public class Order {
    private final String orderId;
    private final double rawAmount;
    private DiscountStrategy discountStrategy;
    private final List<OrderObserver> observers = new ArrayList<>();
    private boolean confirmed = false;

    public Order(String orderId, double rawAmount) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Order ID cannot be blank.");
        }
        if (rawAmount < 0) {
            throw new IllegalArgumentException("Raw amount cannot be negative.");
        }
        this.orderId = orderId;
        this.rawAmount = rawAmount;
        this.discountStrategy = new NoDiscountStrategy(); // Sensible default strategy
    }

    public void setDiscountStrategy(DiscountStrategy discountStrategy) {
        this.discountStrategy = Objects.requireNonNull(discountStrategy, "DiscountStrategy cannot be null.");
    }

    public void registerObserver(OrderObserver observer) {
        Objects.requireNonNull(observer, "Observer cannot be null.");
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(OrderObserver observer) {
        observers.remove(observer);
    }

    public List<OrderObserver> getObservers() {
        return Collections.unmodifiableList(observers);
    }

    public double calculateFinalAmount() {
        return discountStrategy.applyDiscount(rawAmount);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getOrderId() {
        return orderId;
    }

    public void confirmOrder() {
        if (confirmed) {
            throw new IllegalStateException("Order " + orderId + " is already confirmed.");
        }
        confirmed = true;
        double finalAmount = calculateFinalAmount();

        System.out.println("[Order] Order " + orderId + " confirmed using strategy ["
                + discountStrategy.getStrategyName() + "]. Raw: $"
                + String.format("%.2f", rawAmount) + " -> Final: $"
                + String.format("%.2f", finalAmount));

        notifyObservers(finalAmount);
    }

    /**
     * Broadcast step, pulled out of confirmOrder() into a protected hook so a subclass
     * (see AsyncOrder) can change HOW observers are notified without touching
     * the confirmation logic or any DiscountStrategy.
     */
    protected void notifyObservers(double finalAmount) {
        // Iterate over a snapshot to avoid ConcurrentModificationException
        for (OrderObserver observer : new ArrayList<>(observers)) {
            observer.onOrderConfirmed(orderId, finalAmount);
        }
    }
}
