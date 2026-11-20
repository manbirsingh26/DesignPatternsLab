package com.lab.patterns.decorator;

// NEW (my extension): reverses EncryptionDecorator.
// Decrypting a shift cipher is just shifting by the negative key, so this class
// reuses EncryptionDecorator's logic instead of duplicating it.
public class DecryptionDecorator extends EncryptionDecorator {
    public DecryptionDecorator(DataProcessor wrapped, int shiftKey) {
        super(wrapped, -shiftKey);
    }
}
