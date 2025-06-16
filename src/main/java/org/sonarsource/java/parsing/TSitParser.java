package org.sonarsource.java.parsing;

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
  public AstResult parse(String unitName, String sourceCode) {
    try {
      return new AstResult(parser.parseString(null, sourceCode).getRootNode());
    } catch (Exception e) {
      System.err.println("TreeSitter: Unable to parse file" + e.getMessage());
      return null;
    }
  }
}
