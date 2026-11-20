package com.lab.patterns.decorator;

// 4. Concrete decorator: reversible shift cipher over printable ASCII (32..126)
public class EncryptionDecorator extends DataProcessorDecorator {
    private final int shiftKey;

    public EncryptionDecorator(DataProcessor wrapped, int shiftKey) {
        super(wrapped);
        this.shiftKey = shiftKey;
    }

    @Override
    public String process(String input) {
        String text = wrapped.process(input);
        StringBuilder ciphered = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            // Shift printable ASCII characters cleanly while preserving structure
            if (c >= 32 && c <= 126) {
                int range = 126 - 32 + 1;
                int shifted = 32 + Math.floorMod((c - 32) + shiftKey, range);
                ciphered.append((char) shifted);
            } else {
                ciphered.append(c);
            }
        }
        return ciphered.toString();
    }
}
