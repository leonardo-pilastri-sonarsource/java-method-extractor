package org.sonarsource.java.extracting;

public record FunctionInfo(String name,
                           String content,
                           String normalizedContent,
                           long timeMs) {
}
