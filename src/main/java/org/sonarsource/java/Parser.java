package org.sonarsource.java;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class Parser {

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

  public static CompilationUnit parse(String unitName, String sourceCode) {
    parser.setUnitName(unitName);
    parser.setSource(sourceCode.toCharArray());
    try {
      return (CompilationUnit) parser.createAST(null);
    } catch (Exception e) {
      System.err.println("ECJ: Unable to parse file" + e.getMessage());
      return null;
    }
  }

}
