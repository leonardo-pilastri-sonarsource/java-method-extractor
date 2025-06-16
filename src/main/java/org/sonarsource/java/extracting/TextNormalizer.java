package org.sonarsource.java.extracting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.sonarsource.java.parsing.ECJParser;
import org.treesitter.TSNode;

public class TextNormalizer {

  private TextNormalizer() {
    // Prevent instantiation
  }

  public static String normalizeECJMethodText(String methodText, int methodStartOffset, List<Comment> comments) {
    if (methodText == null) return "";
    StringBuilder cleaned = new StringBuilder(methodText);
    for (Comment comment : comments) {
      int commentStart = comment.getStartPosition();
      int commentEnd = commentStart + comment.getLength();

      // Only include comments that fall within the method's bounds
      if (commentStart >= methodStartOffset && commentEnd <= methodStartOffset + methodText.length()) {
        int relativeStart = commentStart - methodStartOffset;
        int relativeEnd = commentEnd - methodStartOffset;

        cleaned.delete(relativeStart, relativeEnd);
      }
    }
    return cleanJavaCode(cleaned.toString());
  }

  public static String normalizeTSMethodText(String methodText, int methodStartOffset, List<TSNode> comments) {
    if (methodText == null) return "";
    StringBuilder cleaned = new StringBuilder();
    int currentOffset = 0;

    for (var comment : comments) {
      if (comment.getStartByte() > methodStartOffset && comment.getEndByte() < methodStartOffset + methodText.length()) {
        int relativeStart = comment.getStartByte() - methodStartOffset;
        int relativeEnd = comment.getEndByte() - methodStartOffset;
        if (relativeStart > currentOffset) {
          cleaned.append(methodText, currentOffset, relativeStart);
        }
        currentOffset = relativeEnd;
      }
    }

    if (currentOffset < methodText.length()) {
      cleaned.append(methodText.substring(currentOffset));
    }

    return cleaned.toString();
  }

  public static String normalizeOneLine(String text) {
    return text
      .replaceAll("[\\n\\r]+", "")
      .replaceAll("\\s+", " ")
      .trim();
  }

  static String cleanJavaCode(String codeString) {
    StringBuilder cleanedCode = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new StringReader(codeString))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String trimmedLine = line.stripTrailing();
        if (!trimmedLine.isEmpty()) {
          cleanedCode.append(trimmedLine).append("\n");
        }
      }
    } catch (IOException e) {
      System.err.println(e.getMessage());
      return null; // Fallback: return original string on error
    }
    if (!cleanedCode.isEmpty()) {
      cleanedCode.setLength(cleanedCode.length() - 1);
    }
    return cleanedCode.toString();
  }

  //for testing purposes
  protected static String testNormalizedMethodText(ECJParser parser, String methodText, boolean oneline) {
    String compilationUnitSourceCode = "class C { " + methodText + " }";
    var ast = parser.parse("C", compilationUnitSourceCode);
    var comments = ECJFunctionExtractor.getComments((CompilationUnit) ast.ast());
    var text = normalizeECJMethodText(methodText, 10, comments);
    if (oneline) {
      text = normalizeOneLine(text);
    }
    return text;
  }

}
