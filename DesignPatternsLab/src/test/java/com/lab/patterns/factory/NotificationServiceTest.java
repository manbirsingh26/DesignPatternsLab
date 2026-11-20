package com.lab.patterns.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lab.patterns.ConsoleCapture;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NotificationServiceTest {

    private final ConsoleCapture console = new ConsoleCapture();

    @BeforeEach
    void setUp() {
        console.start();
    }

    @AfterEach
    void tearDown() {
        console.stop();
    }

    // ---- Checkpoint 1: each concrete creator builds the right product ----

    @Test
    @DisplayName("T1.1 EmailNotificationService creates an EMAIL sender and sends via email")
    void emailServiceUsesEmailSender() {
        NotificationService service = new EmailNotificationService();
        assertTrue(service.createSender() instanceof EmailNotificationSender);

        service.notifyUser("student@example.com", "Hello");
        String out = console.output();
        assertTrue(out.contains("[FactoryMethod] Channel selected: EMAIL"));
        assertTrue(out.contains("[Email -> student@example.com] Subject: Lab Alert | Body: Hello"));
    }

    @Test
    @DisplayName("T1.2 SmsNotificationService creates an SMS sender")
    void smsServiceUsesSmsSender() {
        NotificationService service = new SmsNotificationService();
        assertTrue(service.createSender() instanceof SmsNotificationSender);

        service.notifyUser("+1-604-555-0101", "Hi");
        String out = console.output();
        assertTrue(out.contains("Channel selected: SMS"));
        assertTrue(out.contains("[SMS -> +1-604-555-0101] Text: Hi"));
    }

    @Test
    @DisplayName("T1.3 WebhookNotificationService creates a WEBHOOK sender")
    void webhookServiceUsesWebhookSender() {
        NotificationService service = new WebhookNotificationService();
        assertTrue(service.createSender() instanceof WebhookNotificationSender);

        service.notifyUser("https://hooks.example.com", "{\"ok\":true}");
        assertTrue(console.output().contains("[Webhook POST -> https://hooks.example.com] Payload: {\"ok\":true}"));
    }

    @Test
    @DisplayName("T1.4 (new) PushNotificationService creates a PUSH sender")
    void pushServiceUsesPushSender() {
        NotificationService service = new PushNotificationService();
        assertTrue(service.createSender() instanceof PushNotificationSender);
        assertEquals("PUSH", service.createSender().getChannelName());

        service.notifyUser("device-42", "Ping");
        assertTrue(console.output().contains("[Push -> device device-42] Alert: Ping"));
    }

    @Test
    @DisplayName("T1.5 createSender() returns a fresh instance on every call")
    void factoryMethodReturnsNewInstanceEachTime() {
        NotificationService service = new EmailNotificationService();
        assertFalse(service.createSender() == service.createSender());
    }

    // ---- Checkpoint 2: validation happens BEFORE createSender() ----

    @Test
    @DisplayName("T1.6 null, empty and whitespace recipients are rejected")
    void blankRecipientsRejected() {
        NotificationService service = new EmailNotificationService();
        for (String bad : new String[] {null, "", "   ", "\t\n"}) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> service.notifyUser(bad, "msg"));
            assertEquals("Recipient must not be empty.", ex.getMessage());
        }
        assertFalse(console.output().contains("Channel selected"));
    }

    @Test
    @DisplayName("T1.7 createSender() is never invoked when the recipient is invalid")
    void createSenderNotCalledForInvalidRecipient() {
        int[] calls = {0};
        NotificationService spy = new NotificationService() {
            @Override
            protected NotificationSender createSender() {
                calls[0]++;
                return new EmailNotificationSender();
            }
        };

        assertThrows(IllegalArgumentException.class, () -> spy.notifyUser("  ", "msg"));
        assertEquals(0, calls[0]);

        spy.notifyUser("ok@example.com", "msg");
        assertEquals(1, calls[0]);
    }

    // ---- Checkpoint 3: Open/Closed - new channels need zero edits ----

    @Test
    @DisplayName("T1.8 a brand-new channel plugs in without changing NotificationService (OCP)")
    void newChannelWorksWithoutModifyingCreator() {
        // A channel invented inside the test: no production class was edited for it.
        NotificationService slack = new NotificationService() {
            @Override
            protected NotificationSender createSender() {
                return new NotificationSender() {
                    @Override
                    public void send(String recipient, String message) {
                        System.out.println("  [Slack -> " + recipient + "] " + message);
                    }

                    @Override
                    public String getChannelName() {
                        return "SLACK";
                    }
                };
            }
        };

        slack.notifyUser("#lab07", "deployed");
        assertTrue(console.output().contains("Channel selected: SLACK"));
        assertTrue(console.output().contains("[Slack -> #lab07] deployed"));
    }

    @Test
    @DisplayName("T1.9 client code works polymorphically through the abstract creator")
    void polymorphicClientCode() {
        List<NotificationService> services = List.of(new EmailNotificationService(),
                new SmsNotificationService(), new WebhookNotificationService(), new PushNotificationService());
        for (NotificationService s : services) {
            s.notifyUser("r", "m");
        }
        String out = console.output();
        assertTrue(out.indexOf("EMAIL") < out.indexOf("SMS"));
        assertTrue(out.indexOf("SMS") < out.indexOf("WEBHOOK"));
        assertTrue(out.indexOf("WEBHOOK") < out.indexOf("PUSH"));
    }
}
