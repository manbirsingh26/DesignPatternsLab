package com.lab.patterns.decorator;

// 3. Abstract decorator: implements the SAME interface and holds a wrapped DataProcessor
abstract class DataProcessorDecorator implements DataProcessor {
    protected final DataProcessor wrapped;

    protected DataProcessorDecorator(DataProcessor wrapped) {
        if (wrapped == null) {
            throw new IllegalArgumentException("Wrapped DataProcessor cannot be null.");
        }
        this.wrapped = wrapped;
    }
}
