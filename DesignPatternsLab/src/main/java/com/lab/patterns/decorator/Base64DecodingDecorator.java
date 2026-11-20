package com.lab.patterns.decorator;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

// NEW (my extension): reverses Base64EncodingDecorator.
// Added as a new class; no existing decorator was modified (OCP).
// Throws IllegalArgumentException if the wrapped result is not valid Base64.
public class Base64DecodingDecorator extends DataProcessorDecorator {
    public Base64DecodingDecorator(DataProcessor wrapped) {
        super(wrapped);
    }

    @Override
    public String process(String input) {
        String encoded = wrapped.process(input);
        byte[] bytes = Base64.getDecoder().decode(encoded);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
