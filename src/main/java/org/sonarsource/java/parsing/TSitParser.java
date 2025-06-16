package org.sonarsource.java.parsing;

import java.time.Duration;
import java.time.Instant;
import org.sonarsource.java.utils.PerformanceMetrics;
import org.treesitter.TSLanguage;
import org.treesitter.TSParser;
import org.treesitter.TreeSitterJava;

public class TSitParser implements IParser {

  private static final TSParser parser;

  static {
    parser = new TSParser();
    TSLanguage language = new TreeSitterJava();
    parser.setLanguage(language);
  }

  @Override
  public AstResult parse(String unitName, String sourceCode, PerformanceMetrics metrics) {
    Instant startTime = Instant.now();
    try {
      return new AstResult(parser.parseString(null, sourceCode).getRootNode());
    } catch (Exception e) {
      System.err.println("TreeSitter: Unable to parse file" + e.getMessage());
      return null;
    } finally {
      Duration processingTime = Duration.between(startTime, Instant.now());
      metrics.recordAstGenerationTime(processingTime.toNanos());
    }
  }
}
