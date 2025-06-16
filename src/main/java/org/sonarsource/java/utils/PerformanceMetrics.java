package org.sonarsource.java.utils;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class PerformanceMetrics {

  private final AtomicLong totalAstGenerationTime = new AtomicLong(0);
  private final AtomicInteger astGeneratedCount = new AtomicInteger(0);
  private final AtomicLong totalExtractionTime = new AtomicLong(0);
  private final AtomicLong totalNormalizationTime = new AtomicLong(0);

  public void recordAstGenerationTime(long time) {
    totalAstGenerationTime.addAndGet(time);
    astGeneratedCount.incrementAndGet();
  }

  public void recordExtractionTime(long time) {
    totalExtractionTime.addAndGet(time);
  }

  public void recordNormalizationTime(long time) {
    totalNormalizationTime.addAndGet(time);
  }

  @Override
  public String toString() {
    return """
      Performance Metrics:
      --------------------
      Total AST Generation Time: %d ns  ~ %.2f ms
      Number of AST Generated: %d
      Total Extraction Time: %d ns ~ %.2f ms
      --of which normalization time: %d ns ~ %.2f ms
      """.formatted(
      totalAstGenerationTime.get(),
      totalAstGenerationTime.get() / 1_000_000.0,
      astGeneratedCount.get(),
      totalExtractionTime.get(),
      totalExtractionTime.get() / 1_000_000.0,
      totalNormalizationTime.get(),
      totalNormalizationTime.get() / 1_000_000.0);
  }
}
