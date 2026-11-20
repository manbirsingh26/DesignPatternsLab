# DesignPatternsLab (CSIS 3275 – Lab 07)

## Open in Eclipse
1. Unzip `DesignPatternsLab.zip`.
2. Eclipse: **File > Import > Maven > Existing Maven Projects** and select the `DesignPatternsLab` folder.
3. Wait for Eclipse to download JUnit 5 (needs internet the first time).

If you would rather use a plain Java project (no Maven):
1. **File > New > Java Project** named `DesignPatternsLab`.
2. Copy the `src` folder into it.
3. Right-click `src/main/java` and `src/test/java` > **Build Path > Use as Source Folder**.
4. Right-click the project > **Build Path > Add Libraries > JUnit > JUnit 5**.

## Run
- Tests: right-click `src/test/java` > **Run As > JUnit Test** (38 tests).
- Demo: right-click `com.lab.patterns.runner.LabRunner` > **Run As > Java Application**.

Requires Java 17 or newer.

## Layout
```
src/main/java/com/lab/patterns/
├── factory/      Factory Method (Email, SMS, Webhook, + Push)
├── decorator/    Decorator (Timestamp, Encryption, Base64, + Base64Decoding, Decryption)
├── behavioral/   Strategy + Observer (Order, discounts, observers, + Composite, CustomerNotification, AsyncOrder)
└── runner/       LabRunner (console demo of every checkpoint)
src/test/java/com/lab/patterns/
├── ConsoleCapture.java
├── factory/NotificationServiceTest.java
├── decorator/DataProcessorTest.java
└── behavioral/OrderTest.java
```
