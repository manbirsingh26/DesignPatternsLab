package com.lab.patterns.decorator;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

// 4. Concrete decorator: Base64-encodes the wrapped result
public class Base64EncodingDecorator extends DataProcessorDecorator {
    public Base64EncodingDecorator(DataProcessor wrapped) {
        super(wrapped);
    }

    @Override
    public String process(String input) {
        String processed = wrapped.process(input);
        byte[] bytes = processed.getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
