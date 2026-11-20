package com.lab.patterns;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Test helper: temporarily redirects System.out so tests can assert on
 * what the patterns print to the console.
 */
public class ConsoleCapture {
    private final PrintStream original = System.out;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    public void start() {
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    public void stop() {
        System.setOut(original);
    }

    public String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }
}
