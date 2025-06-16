package org.sonarsource.java.parsing;

import org.junit.jupiter.api.Test;
import org.treesitter.TSNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TSitParserTest {

  @Test
  void testParse() {
    String sourceCode = "public class Test { public void method() {} }";
    var parser = new TSitParser();
    TSNode node = (TSNode) parser.parse("Test", sourceCode).ast();
    assertNotNull(node, "Parsed node should not be null");
    assertEquals("program", node.getType(), "Root node type should be 'program'");
  }

}