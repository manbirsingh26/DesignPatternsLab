package com.lab.patterns.decorator;

// 2. Concrete component (base payload cleaner)
public class PlainTextProcessor implements DataProcessor {
    @Override
    public String process(String input) {
        if (input == null) {
            return "";
        }
        return input.trim();
    }
}
