package com.lab.patterns.runner;

import com.lab.patterns.behavioral.AsyncOrder;
import com.lab.patterns.behavioral.AuditLogObserver;
import com.lab.patterns.behavioral.CompositeDiscountStrategy;
import com.lab.patterns.behavioral.CustomerNotificationObserver;
import com.lab.patterns.behavioral.InventoryObserver;
import com.lab.patterns.behavioral.LoyaltyPointsObserver;
import com.lab.patterns.behavioral.Order;
import com.lab.patterns.behavioral.OrderObserver;
import com.lab.patterns.behavioral.PercentageDiscountStrategy;
import com.lab.patterns.behavioral.TieredThresholdDiscountStrategy;
import com.lab.patterns.decorator.Base64DecodingDecorator;
import com.lab.patterns.decorator.Base64EncodingDecorator;
import com.lab.patterns.decorator.DataProcessor;
import com.lab.patterns.decorator.DecryptionDecorator;
import com.lab.patterns.decorator.EncryptionDecorator;
import com.lab.patterns.decorator.HeaderTimestampDecorator;
import com.lab.patterns.decorator.PlainTextProcessor;
import com.lab.patterns.factory.EmailNotificationService;
import com.lab.patterns.factory.NotificationService;
import com.lab.patterns.factory.PushNotificationService;
import com.lab.patterns.factory.SmsNotificationService;
import com.lab.patterns.factory.WebhookNotificationService;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Console demo that walks through every Hands-On Checkpoint of Lab 07.
 * Run: right-click LabRunner.java > Run As > Java Application.
 */
public class LabRunner {

    public static void main(String[] args) throws Exception {
        runFactoryDemo();
        runDecoratorDemo();
        runStrategyObserverDemo();
        runAsyncDemo();
        System.out.println("\n=== All Lab 07 demos finished ===");
    }

    // ---------------------------------------------------------------- Task 1
    private static void runFactoryDemo() {
        header("TASK 1: FACTORY METHOD");

        // Client code depends only on the abstract creator
        List<NotificationService> services = List.of(
                new EmailNotificationService(),
                new SmsNotificationService(),
                new WebhookNotificationService(),   // starter extension
                new PushNotificationService());     // my extension

        String[] recipients = {"student@example.com", "+1-604-555-0101",
                "https://hooks.example.com/lab07", "device-token-42"};

        for (int i = 0; i < services.size(); i++) {
            services.get(i).notifyUser(recipients[i], "Lab 07 build passed.");
        }

        System.out.println("\nCheckpoint 2: blank recipient is rejected before createSender():");
        tryAndReport(() -> new EmailNotificationService().notifyUser("   ", "should fail"));
        tryAndReport(() -> new SmsNotificationService().notifyUser(null, "should fail"));
    }

    // ---------------------------------------------------------------- Task 2
    private static void runDecoratorDemo() {
        header("TASK 2: DECORATOR");
        String input = "  Hello Lab  ";

        // Pipeline A: PlainText -> Encryption(key=4) -> Base64
        DataProcessor pipelineA = new Base64EncodingDecorator(
                new EncryptionDecorator(new PlainTextProcessor(), 4));

        // Pipeline B: PlainText -> Timestamp -> Base64
        DataProcessor pipelineB = new Base64EncodingDecorator(
                new HeaderTimestampDecorator(new PlainTextProcessor()));

        System.out.println("Input                : \"" + input + "\"");
        System.out.println("Pipeline A output    : " + pipelineA.process(input));
        System.out.println("Pipeline B output    : " + pipelineB.process(input));

        // My extension: reverse Pipeline A to prove the transformations are reversible
        DataProcessor reverseA = new DecryptionDecorator(
                new Base64DecodingDecorator(pipelineA), 4);
        System.out.println("Pipeline A reversed  : \"" + reverseA.process(input) + "\"");

        System.out.println("\nEdge cases (Checkpoint 3):");
        System.out.println("A(null)              : \"" + pipelineA.process(null) + "\"");
        System.out.println("A(\"   \")             : \"" + pipelineA.process("   ") + "\"");
        System.out.println("B(null) decoded      : \""
                + new Base64DecodingDecorator(pipelineB).process(null) + "\"");
        System.out.println("B(\"   \") decoded     : \""
                + new Base64DecodingDecorator(pipelineB).process("   ") + "\"");

        System.out.println("\nOrder sensitivity (swapping wrappers):");
        DataProcessor encryptThenBase64 = pipelineA;
        DataProcessor base64ThenEncrypt = new EncryptionDecorator(
                new Base64EncodingDecorator(new PlainTextProcessor()), 4);
        DataProcessor timestampThenEncrypt = new EncryptionDecorator(
                new HeaderTimestampDecorator(new PlainTextProcessor()), 4);
        DataProcessor encryptThenTimestamp = new HeaderTimestampDecorator(
                new EncryptionDecorator(new PlainTextProcessor(), 4));
        System.out.println("Encrypt -> Base64    : " + encryptThenBase64.process(input));
        System.out.println("Base64  -> Encrypt   : " + base64ThenEncrypt.process(input));
        System.out.println("Timestamp -> Encrypt : " + timestampThenEncrypt.process(input));
        System.out.println("Encrypt -> Timestamp : " + encryptThenTimestamp.process(input));
    }

    // ---------------------------------------------------------------- Task 3
    private static void runStrategyObserverDemo() {
        header("TASK 3: STRATEGY + OBSERVER");

        // Checkpoint 1: swap strategies dynamically across different orders
        Order order1 = new Order("ORD-1001", 120.00);
        order1.setDiscountStrategy(new PercentageDiscountStrategy(15));

        Order order2 = new Order("ORD-1002", 250.00);
        order2.setDiscountStrategy(new TieredThresholdDiscountStrategy(200, 25));

        Order order3 = new Order("ORD-1003", 300.00);
        order3.setDiscountStrategy(new CompositeDiscountStrategy(   // my extension
                new PercentageDiscountStrategy(10),
                new TieredThresholdDiscountStrategy(250, 20)));

        // Checkpoint 2: register 3 observers, then unsubscribe AuditLogObserver
        OrderObserver inventory = new InventoryObserver();
        OrderObserver loyalty = new LoyaltyPointsObserver();
        OrderObserver audit = new AuditLogObserver();

        order1.registerObserver(inventory);
        order1.registerObserver(loyalty);
        order1.registerObserver(audit);
        order1.registerObserver(audit); // duplicate is ignored
        System.out.println("order1 observers before removal: " + order1.getObservers().size());
        order1.removeObserver(audit);
        System.out.println("order1 observers after removing Audit: " + order1.getObservers().size());
        order1.confirmOrder();

        order2.registerObserver(inventory);
        order2.registerObserver(loyalty);
        order2.registerObserver(audit);
        order2.confirmOrder();

        // My extension: observer that reuses the Factory Method from Task 1
        order3.registerObserver(inventory);
        order3.registerObserver(new CustomerNotificationObserver(
                new SmsNotificationService(), "+1-604-555-0101"));
        order3.confirmOrder();

        // Checkpoint 3: second confirmation must throw IllegalStateException
        System.out.println("\nCheckpoint 3: confirming ORD-1001 a second time:");
        tryAndReport(order1::confirmOrder);
    }

    private static void runAsyncDemo() throws Exception {
        header("EXTENSION: ASYNCHRONOUS OBSERVERS");
        ExecutorService pool = Executors.newFixedThreadPool(3);
        try {
            AsyncOrder order = new AsyncOrder("ORD-2001", 180.00, pool);
            order.setDiscountStrategy(new PercentageDiscountStrategy(20)); // same strategy class, unchanged
            order.registerObserver((id, amt) -> System.out.println("  [Async] Inventory on "
                    + Thread.currentThread().getName()));
            order.registerObserver((id, amt) -> System.out.println("  [Async] Loyalty on "
                    + Thread.currentThread().getName()));
            order.registerObserver(new AuditLogObserver());
            order.confirmOrder();
            System.out.println("confirmOrder() returned on thread: " + Thread.currentThread().getName());
            order.allNotificationsDone().get(); // wait for all observers
        } finally {
            pool.shutdown();
        }
    }

    // ---------------------------------------------------------------- helpers
    private static void header(String title) {
        System.out.println("\n==================== " + title + " ====================");
    }

    private static void tryAndReport(Runnable action) {
        try {
            action.run();
            System.out.println("  !! No exception thrown");
        } catch (RuntimeException ex) {
            System.out.println("  Caught " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }
}
