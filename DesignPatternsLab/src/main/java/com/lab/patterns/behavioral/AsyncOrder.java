package com.lab.patterns.behavioral;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

// NEW (my extension): answers "How would you notify observers asynchronously
// without altering DiscountStrategy?"
//
// AsyncOrder overrides only the notifyObservers() hook. The discount is still
// computed synchronously by the strategy inside confirmOrder(); only the broadcast
// is handed to an Executor. DiscountStrategy and its implementations are untouched.
public class AsyncOrder extends Order {
    private final Executor executor;
    private final List<CompletableFuture<Void>> pending = new ArrayList<>();

    public AsyncOrder(String orderId, double rawAmount, Executor executor) {
        super(orderId, rawAmount);
        this.executor = Objects.requireNonNull(executor, "Executor cannot be null.");
    }

    @Override
    protected void notifyObservers(double finalAmount) {
        String id = getOrderId();
        // Same defensive snapshot as Order, so observers can unsubscribe safely
        for (OrderObserver observer : new ArrayList<>(getObservers())) {
            CompletableFuture<Void> future = CompletableFuture
                    .runAsync(() -> observer.onOrderConfirmed(id, finalAmount), executor)
                    // One failing observer must not stop the others or crash the caller
                    .exceptionally(ex -> {
                        System.err.println("  [AsyncOrder] Observer failed: " + ex.getMessage());
                        return null;
                    });
            pending.add(future);
        }
    }

    /** Lets callers (or tests) wait until every observer has finished. */
    public CompletableFuture<Void> allNotificationsDone() {
        return CompletableFuture.allOf(pending.toArray(new CompletableFuture<?>[0]));
    }
}
