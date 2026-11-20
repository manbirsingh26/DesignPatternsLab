package com.lab.patterns.decorator;

import java.time.Instant;

// 4. Concrete decorator: prepends an ISO-8601 timestamp header
public class HeaderTimestampDecorator extends DataProcessorDecorator {
    public HeaderTimestampDecorator(DataProcessor wrapped) {
        super(wrapped);
    }

    @Override
    public String process(String input) {
        String baseResult = wrapped.process(input);
        return "[" + Instant.now() + "] " + baseResult;
    }
}
