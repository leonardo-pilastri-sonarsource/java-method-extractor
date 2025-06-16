package org.sonarsource.java.parsing;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.sonarsource.java.utils.PerformanceMetrics;

public class ECJParser implements IParser {

  private static final ASTParser parser;

  static {
    parser = ASTParser.newParser(AST.getJLSLatest());
    Map<String, String> options = new HashMap<>(JavaCore.getOptions());
    JavaCore.setComplianceOptions("17", options);
    parser.setCompilerOptions(options);
    // We are not interested in semantic information
    parser.setResolveBindings(false);
    parser.setBindingsRecovery(false);
  }

  public AstResult parse(String unitName, String sourceCode, PerformanceMetrics metrics) {
    Instant startTime = Instant.now();
    parser.setUnitName(unitName);
    parser.setSource(sourceCode.toCharArray());
    try {
      return new AstResult(parser.createAST(null));
    } catch (Exception e) {
      System.err.println("ECJ: Unable to parse file" + e.getMessage());
      return null;
    } finally {
      Duration processingTime = Duration.between(startTime, Instant.now());
      metrics.recordAstGenerationTime(processingTime.toNanos());
    }
  }

}
