package org.sonarsource.java.parsing;

public interface IParser {

  AstResult parse(String unitName, String sourceCode);

}
