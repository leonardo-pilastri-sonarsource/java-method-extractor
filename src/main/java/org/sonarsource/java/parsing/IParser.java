package org.sonarsource.java.parsing;

import org.sonarsource.java.utils.PerformanceMetrics;

public interface IParser {

  AstResult parse(String unitName, String sourceCode, PerformanceMetrics metrics);

}
