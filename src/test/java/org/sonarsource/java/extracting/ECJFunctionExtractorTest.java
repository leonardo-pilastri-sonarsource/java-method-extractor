package org.sonarsource.java.extracting;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonarsource.java.parsing.ECJParser;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ECJFunctionExtractorTest {

  ECJParser parser = new ECJParser();

  @ParameterizedTest(name = "Normalized output should match for input: {0}")
  @MethodSource("provideNormalizationSamples")
  void testNormalizeMethodText(String input, String expected) {
    String actual = TextNormalizer.testNormalizedMethodText(parser, input, true);
    assertEquals(expected, actual);
  }

  @Test
  void testNormalizeMethodTextOnMultipleLines() {
    String input = """
      // this is a full-line comment
      public void test() { // method starts
        int x = 5; // initialize x
        /*
          this is a block comment
          that spans multiple lines
        */
        System.out.println(x); /* print value */
      } // end of method
      """;
    String actual = TextNormalizer.testNormalizedMethodText(parser, input, false);
    String expected = """
      public void test() {
        int x = 5;
        System.out.println(x);
      }""";
    assertEquals(expected, actual);
  }

  @Test
  void testNormalizeMethodText_emptyAndNull() {
    assertEquals("", TextNormalizer.testNormalizedMethodText(parser, "", true));
    assertEquals("", TextNormalizer.testNormalizedMethodText(parser, null, true));
  }

  private static Stream<Arguments> provideNormalizationSamples() {
    return getNormalizationSamples()
      .entrySet()
      .stream()
      .map(e -> Arguments.of(e.getKey(), e.getValue()));
  }

  private static Map<String, String> getNormalizationSamples() {
    Map<String, String> samples = new LinkedHashMap<>(); // keep order

    samples.put("""
        public void test() {// method starts
          int x = 5;
        } // end of method
        """,
      "public void test() { int x = 5;}"
    );

    samples.put("""
        // this is a full-line comment
        public void test() {// method starts
          int x = 5;// initialize x
            /*
              this is a block comment
              that spans multiple lines
            */
          System.out.println(x);/* print value */
        } // end of method
        """,
      "public void test() { int x = 5; System.out.println(x);}"
    );

    samples.put("""
        public void foo() {\n\tSystem.out.println("Hello");\n}
        """,
      "public void foo() { System.out.println(\"Hello\");}"
    );

    samples.put("""
        public void say() {
          System.out.println("http://example.com"); // comment
        }
        """,
      "public void say() { System.out.println(\"http://example.com\");}"
    );

    samples.put("""
        public void nested() {
          int x = 10;/* first comment
          still inside
          */ int y = 20; /* another comment */ int z = x + y;
        }
        """,
      "public void nested() { int x = 10; int y = 20; int z = x + y;}"
    );

    samples.put("", "");
    samples.put(null, "");

    return samples;
  }

}
