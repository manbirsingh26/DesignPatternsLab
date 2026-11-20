package com.lab.patterns.behavioral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lab.patterns.ConsoleCapture;
import com.lab.patterns.factory.EmailNotificationService;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderTest {

    private static final double DELTA = 1e-9;
    private final ConsoleCapture console = new ConsoleCapture();

    @BeforeEach
    void setUp() {
        console.start();
    }

    @AfterEach
    void tearDown() {
        console.stop();
    }

    /** Test double that records every notification it receives. */
    private static class RecordingObserver implements OrderObserver {
        final List<String> calls = new ArrayList<>();

        @Override
        public void onOrderConfirmed(String orderId, double finalAmount) {
            calls.add(orderId + "=" + String.format("%.2f", finalAmount));
        }
    }

    // ================= STRATEGY =================

    @Test
    @DisplayName("T3.1 default strategy is NoDiscount")
    void defaultStrategyIsNoDiscount() {
        assertEquals(120.0, new Order("A", 120.0).calculateFinalAmount(), DELTA);
    }

    @Test
    @DisplayName("T3.2 PercentageDiscountStrategy: 15% off 120 = 102; 0% and 100% boundaries")
    void percentageStrategy() {
        assertEquals(102.0, new PercentageDiscountStrategy(15).applyDiscount(120.0), DELTA);
        assertEquals(120.0, new PercentageDiscountStrategy(0).applyDiscount(120.0), DELTA);
        assertEquals(0.0, new PercentageDiscountStrategy(100).applyDiscount(120.0), DELTA);
        assertEquals("PERCENTAGE_15.0%", new PercentageDiscountStrategy(15).getStrategyName());
    }

    @Test
    @DisplayName("T3.3 PercentageDiscountStrategy rejects values outside 0..100")
    void percentageStrategyValidation() {
        assertThrows(IllegalArgumentException.class, () -> new PercentageDiscountStrategy(-1));
        assertThrows(IllegalArgumentException.class, () -> new PercentageDiscountStrategy(100.01));
    }

    @Test
    @DisplayName("T3.4 TieredThreshold: below, exactly at and above the threshold; never negative")
    void tieredStrategy() {
        DiscountStrategy s = new TieredThresholdDiscountStrategy(200, 25);
        assertEquals(150.0, s.applyDiscount(150.0), DELTA);  // below -> unchanged
        assertEquals(175.0, s.applyDiscount(200.0), DELTA);  // exactly at threshold -> discounted
        assertEquals(225.0, s.applyDiscount(250.0), DELTA);  // above
        assertEquals(0.0, new TieredThresholdDiscountStrategy(0, 50).applyDiscount(20.0), DELTA);
        assertThrows(IllegalArgumentException.class, () -> new TieredThresholdDiscountStrategy(-1, 5));
        assertThrows(IllegalArgumentException.class, () -> new TieredThresholdDiscountStrategy(10, -5));
    }

    @Test
    @DisplayName("T3.5 Checkpoint 1: strategies are swapped at runtime, across and within orders")
    void swapStrategiesDynamically() {
        Order o1 = new Order("ORD-1", 250.0);
        Order o2 = new Order("ORD-2", 250.0);
        o1.setDiscountStrategy(new PercentageDiscountStrategy(15));
        o2.setDiscountStrategy(new TieredThresholdDiscountStrategy(200, 25));
        assertEquals(212.5, o1.calculateFinalAmount(), DELTA);
        assertEquals(225.0, o2.calculateFinalAmount(), DELTA);

        // Same order, strategy swapped before confirmation
        o1.setDiscountStrategy(new NoDiscountStrategy());
        assertEquals(250.0, o1.calculateFinalAmount(), DELTA);
        o1.setDiscountStrategy(new TieredThresholdDiscountStrategy(200, 25));
        assertEquals(225.0, o1.calculateFinalAmount(), DELTA);

        assertThrows(NullPointerException.class, () -> o1.setDiscountStrategy(null));
    }

    @Test
    @DisplayName("T3.6 (new) CompositeDiscountStrategy stacks strategies left to right")
    void compositeStrategy() {
        DiscountStrategy combo = new CompositeDiscountStrategy(
                new PercentageDiscountStrategy(10), new TieredThresholdDiscountStrategy(250, 20));
        assertEquals(250.0, combo.applyDiscount(300.0), DELTA);  // 300 -> 270 -> 250
        assertEquals(180.0, combo.applyDiscount(200.0), DELTA);  // 200 -> 180 (below 250)
        assertEquals("COMPOSITE[PERCENTAGE_10.0% -> SPEND_250.0_SAVE_20.0]", combo.getStrategyName());

        // Order of strategies matters: 20 off first, then 10%
        DiscountStrategy reversed = new CompositeDiscountStrategy(
                new TieredThresholdDiscountStrategy(250, 20), new PercentageDiscountStrategy(10));
        assertEquals(252.0, reversed.applyDiscount(300.0), DELTA); // 300 -> 280 -> 252

        assertThrows(IllegalArgumentException.class, CompositeDiscountStrategy::new);
    }

    // ================= ORDER VALIDATION =================

    @Test
    @DisplayName("T3.7 Order rejects blank IDs and negative amounts; allows 0")
    void orderValidation() {
        assertThrows(IllegalArgumentException.class, () -> new Order(null, 10));
        assertThrows(IllegalArgumentException.class, () -> new Order("  ", 10));
        assertThrows(IllegalArgumentException.class, () -> new Order("X", -0.01));
        assertEquals(0.0, new Order("FREE", 0).calculateFinalAmount(), DELTA);
    }

    // ================= OBSERVER =================

    @Test
    @DisplayName("T3.8 Checkpoint 2: register 3 observers, remove Audit, only 2 are notified")
    void unsubscribeAuditBeforeConfirm() {
        Order order = new Order("ORD-1001", 120.0);
        order.setDiscountStrategy(new PercentageDiscountStrategy(15));
        AuditLogObserver audit = new AuditLogObserver();
        order.registerObserver(new InventoryObserver());
        order.registerObserver(new LoyaltyPointsObserver());
        order.registerObserver(audit);
        assertEquals(3, order.getObservers().size());

        order.removeObserver(audit);
        assertEquals(2, order.getObservers().size());
        order.confirmOrder();

        String out = console.output();
        assertTrue(out.contains("[Order] Order ORD-1001 confirmed using strategy [PERCENTAGE_15.0%]. "
                + "Raw: $120.00 -> Final: $102.00"));
        assertTrue(out.contains("[InventoryObserver] Reserved stock for order ORD-1001 (confirmed total: $102.00)"));
        assertTrue(out.contains("[LoyaltyPointsObserver] Order ORD-1001 awarded 10 loyalty points."));
        assertFalse(out.contains("[AuditLogObserver]"));
    }

    @Test
    @DisplayName("T3.9 observers are notified once each, in registration order, with the discounted total")
    void observersNotifiedInOrder() {
        Order order = new Order("ORD-7", 250.0);
        order.setDiscountStrategy(new TieredThresholdDiscountStrategy(200, 25));
        List<String> sequence = new ArrayList<>();
        order.registerObserver((id, amt) -> sequence.add("first:" + amt));
        order.registerObserver((id, amt) -> sequence.add("second:" + amt));
        order.confirmOrder();
        assertEquals(List.of("first:225.0", "second:225.0"), sequence);
    }

    @Test
    @DisplayName("T3.10 duplicate registration is ignored; null observer rejected")
    void duplicateAndNullObservers() {
        Order order = new Order("ORD-8", 50.0);
        RecordingObserver rec = new RecordingObserver();
        order.registerObserver(rec);
        order.registerObserver(rec);
        assertEquals(1, order.getObservers().size());
        assertThrows(NullPointerException.class, () -> order.registerObserver(null));

        order.confirmOrder();
        assertEquals(List.of("ORD-8=50.00"), rec.calls);
    }

    @Test
    @DisplayName("T3.11 getObservers() is read-only")
    void observersListIsUnmodifiable() {
        Order order = new Order("ORD-9", 10.0);
        assertThrows(UnsupportedOperationException.class,
                () -> order.getObservers().add(new AuditLogObserver()));
    }

    @Test
    @DisplayName("T3.12 loyalty points use floor(total / 10)")
    void loyaltyPointsRounding() {
        Order order = new Order("ORD-10", 99.99);
        order.registerObserver(new LoyaltyPointsObserver());
        order.confirmOrder();
        assertTrue(console.output().contains("awarded 9 loyalty points."));
    }

    // ================= STATE GUARDS =================

    @Test
    @DisplayName("T3.13 Checkpoint 3: second confirmOrder() throws IllegalStateException; no second broadcast")
    void doubleConfirmationRejected() {
        Order order = new Order("ORD-1001", 120.0);
        RecordingObserver rec = new RecordingObserver();
        order.registerObserver(rec);

        assertFalse(order.isConfirmed());
        order.confirmOrder();
        assertTrue(order.isConfirmed());

        IllegalStateException ex = assertThrows(IllegalStateException.class, order::confirmOrder);
        assertEquals("Order ORD-1001 is already confirmed.", ex.getMessage());
        assertEquals(1, rec.calls.size());
    }

    @Test
    @DisplayName("T3.14 an observer that unsubscribes itself during broadcast causes no ConcurrentModificationException")
    void selfUnsubscribeDuringBroadcast() {
        Order order = new Order("ORD-11", 40.0);
        RecordingObserver after = new RecordingObserver();
        OrderObserver selfRemoving = new OrderObserver() {
            @Override
            public void onOrderConfirmed(String orderId, double finalAmount) {
                order.removeObserver(this);
            }
        };
        order.registerObserver(selfRemoving);
        order.registerObserver(after);

        order.confirmOrder(); // would throw CME without the defensive copy
        assertEquals(1, after.calls.size());
        assertEquals(1, order.getObservers().size());
    }

    // ================= NEW: cross-pattern observer =================

    @Test
    @DisplayName("T3.15 (new) CustomerNotificationObserver sends the confirmation through Task 1's factory")
    void customerNotificationObserverUsesFactory() {
        Order order = new Order("ORD-12", 80.0);
        order.registerObserver(new CustomerNotificationObserver(new EmailNotificationService(), "a@example.com"));
        order.confirmOrder();
        String out = console.output();
        assertTrue(out.contains("[FactoryMethod] Channel selected: EMAIL"));
        assertTrue(out.contains("Your order ORD-12 is confirmed. Total: $80.00"));
    }

    // ================= NEW: asynchronous notification =================

    @Test
    @DisplayName("T3.16 (new) AsyncOrder notifies observers on worker threads, strategy unchanged")
    void asyncOrderNotifiesOnOtherThreads() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            AsyncOrder order = new AsyncOrder("ORD-A1", 200.0, pool);
            order.setDiscountStrategy(new PercentageDiscountStrategy(25));
            Queue<String> threads = new ConcurrentLinkedQueue<>();
            Queue<Double> amounts = new ConcurrentLinkedQueue<>();
            for (int i = 0; i < 3; i++) {
                order.registerObserver((id, amt) -> {
                    threads.add(Thread.currentThread().getName());
                    amounts.add(amt);
                });
            }

            order.confirmOrder();
            order.allNotificationsDone().get(5, TimeUnit.SECONDS);

            assertEquals(3, threads.size());
            String testThread = Thread.currentThread().getName();
            for (String t : threads) {
                assertNotEquals(testThread, t);
            }
            for (double a : amounts) {
                assertEquals(150.0, a, DELTA);
            }
            assertThrows(IllegalStateException.class, order::confirmOrder); // guard still works
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    @DisplayName("T3.17 (new) one failing async observer does not stop the others")
    void asyncOrderIsolatesFailures() throws Exception {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            AsyncOrder order = new AsyncOrder("ORD-A2", 10.0, pool);
            Queue<String> ok = new ConcurrentLinkedQueue<>();
            order.registerObserver((id, amt) -> {
                throw new RuntimeException("boom");
            });
            order.registerObserver((id, amt) -> ok.add(id));

            order.confirmOrder();
            order.allNotificationsDone().get(5, TimeUnit.SECONDS); // completes normally
            assertEquals(List.of("ORD-A2"), new ArrayList<>(ok));
        } finally {
            pool.shutdownNow();
        }
    }
}
