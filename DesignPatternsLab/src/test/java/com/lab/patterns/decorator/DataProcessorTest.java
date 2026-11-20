package com.lab.patterns.decorator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DataProcessorTest {

    // ISO-8601 instant header, e.g. "[2026-11-20T21:37:45.708076066Z] "
    private static final String TIMESTAMP = "\\[\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?Z\\] ";

    private DataProcessor pipelineA() {
        return new Base64EncodingDecorator(new EncryptionDecorator(new PlainTextProcessor(), 4));
    }

    private DataProcessor pipelineB() {
        return new Base64EncodingDecorator(new HeaderTimestampDecorator(new PlainTextProcessor()));
    }

    // ---- Base component ----

    @Test
    @DisplayName("T2.1 PlainTextProcessor trims input and maps null to empty string")
    void plainTextProcessor() {
        DataProcessor p = new PlainTextProcessor();
        assertEquals("Hello Lab", p.process("  Hello Lab  "));
        assertEquals("", p.process(null));
        assertEquals("", p.process("   "));
    }

    // ---- Checkpoint 1: Pipeline A ----

    @Test
    @DisplayName("T2.2 Pipeline A (Encrypt key=4 -> Base64) produces the expected value")
    void pipelineAExactOutput() {
        // "Hello Lab" shifted by 4 = "Lipps$Pef"; Base64("Lipps$Pef") = "TGlwcHMkUGVm"
        assertEquals("TGlwcHMkUGVm", pipelineA().process("  Hello Lab  "));
    }

    @Test
    @DisplayName("T2.3 Pipeline A layers apply in order: decoding Base64 reveals the ciphertext")
    void pipelineALayerOrder() {
        DataProcessor decodedOnly = new Base64DecodingDecorator(pipelineA());
        assertEquals("Lipps$Pef", decodedOnly.process("Hello Lab"));
    }

    @Test
    @DisplayName("T2.4 (new) Pipeline A is fully reversible with the new decoding decorators")
    void pipelineARoundTrip() {
        DataProcessor reverse = new DecryptionDecorator(new Base64DecodingDecorator(pipelineA()), 4);
        assertEquals("Hello Lab", reverse.process("  Hello Lab  "));
        assertEquals("Design Patterns: 100% ~ok!", reverse.process("Design Patterns: 100% ~ok!"));
    }

    @Test
    @DisplayName("T2.5 Encryption wraps around the printable ASCII range")
    void encryptionWrapAround() {
        assertEquals(" ", new EncryptionDecorator(new PlainTextProcessor(), 1).process("~"));
        // Space is the first printable char; shifting it back by 1 wraps to '~'.
        // (Leading space would be trimmed, so put it between two letters.)
        assertEquals("`~`", new EncryptionDecorator(new PlainTextProcessor(), -1).process("a a"));
        // Non-printable characters (e.g. newline inside text) pass through unchanged
        assertEquals("L\nP", new EncryptionDecorator(new PlainTextProcessor(), 4).process("H\nL"));
    }

    // ---- Checkpoint 2: Pipeline B ----

    @Test
    @DisplayName("T2.6 Pipeline B (Timestamp -> Base64) decodes to '[ISO-8601] text'")
    void pipelineBFormat() {
        String encoded = pipelineB().process("  Hello Lab  ");
        assertTrue(encoded.matches("[A-Za-z0-9+/]+=*"), "output must be Base64");

        String decoded = new Base64DecodingDecorator(pipelineB()).process("  Hello Lab  ");
        assertTrue(decoded.matches(TIMESTAMP + "Hello Lab"), "got: " + decoded);
    }

    // ---- Checkpoint 3: edge cases ----

    @Test
    @DisplayName("T2.7 Pipeline A: null and whitespace-only inputs produce an empty string")
    void pipelineAEdgeCases() {
        assertEquals("", pipelineA().process(null));
        assertEquals("", pipelineA().process("   "));
        assertEquals("", pipelineA().process(""));
    }

    @Test
    @DisplayName("T2.8 Pipeline B: null and whitespace-only inputs still get a timestamp header")
    void pipelineBEdgeCases() {
        DataProcessor decodeB = new Base64DecodingDecorator(pipelineB());
        assertTrue(decodeB.process(null).matches(TIMESTAMP));
        assertTrue(decodeB.process("   ").matches(TIMESTAMP));
    }

    @Test
    @DisplayName("T2.9 a decorator cannot wrap null")
    void nullWrappedRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Base64EncodingDecorator(null));
        assertThrows(IllegalArgumentException.class, () -> new EncryptionDecorator(null, 4));
        assertThrows(IllegalArgumentException.class, () -> new HeaderTimestampDecorator(null));
    }

    @Test
    @DisplayName("T2.10 (new) Base64DecodingDecorator rejects input that is not Base64")
    void decodingInvalidBase64() {
        assertThrows(IllegalArgumentException.class,
                () -> new Base64DecodingDecorator(new PlainTextProcessor()).process("not base64!!"));
    }

    // ---- Order sensitivity (discussion question) ----

    @Test
    @DisplayName("T2.11 swapping Encryption and Base64 changes the output")
    void orderSensitivityEncryptBase64() {
        DataProcessor base64ThenEncrypt =
                new EncryptionDecorator(new Base64EncodingDecorator(new PlainTextProcessor()), 4);
        assertEquals("WKZwfK<kXKJm", base64ThenEncrypt.process("Hello Lab"));
        assertNotEquals(pipelineA().process("Hello Lab"), base64ThenEncrypt.process("Hello Lab"));
        // Base64 -> Encrypt output is no longer valid Base64 ('<' is not in the alphabet)
        assertTrue(base64ThenEncrypt.process("Hello Lab").contains("<"));
    }

    @Test
    @DisplayName("T2.12 swapping Timestamp and Encryption decides whether the header is readable")
    void orderSensitivityTimestamp() {
        String encryptThenStamp = new HeaderTimestampDecorator(
                new EncryptionDecorator(new PlainTextProcessor(), 4)).process("Hello Lab");
        String stampThenEncrypt = new EncryptionDecorator(
                new HeaderTimestampDecorator(new PlainTextProcessor()), 4).process("Hello Lab");

        assertTrue(encryptThenStamp.matches(TIMESTAMP + "Lipps\\$Pef"));  // readable header
        assertTrue(stampThenEncrypt.startsWith("_"));                    // '[' shifted by 4
        assertTrue(stampThenEncrypt.endsWith("$Lipps$Pef"));              // header is ciphered too
    }
}
