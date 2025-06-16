package org.sonarsource.java.extracting;

import java.util.List;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.sonarsource.java.parsing.ECJParser;

public class TextNormalizer {

  private TextNormalizer() {
    // Prevent instantiation
  }

  public static String normalizeMethodText(String methodText, int methodStartOffset, List<Comment> comments) {
    if (methodText == null) return "";
    StringBuilder cleaned = new StringBuilder(methodText);
    for (Comment comment : comments) {
      int commentStart = comment.getStartPosition();
      int commentEnd = commentStart + comment.getLength();

      // Only include comments that fall within the method's bounds
      if (commentStart >= methodStartOffset && commentEnd <= methodStartOffset + methodText.length()) {
        int relativeStart = commentStart - methodStartOffset;
        int relativeEnd = commentEnd - methodStartOffset;

        for (int i = relativeStart; i < relativeEnd; i++) {
          cleaned.setCharAt(i, ' '); // preserve spacing
        }
      }
    }
    return cleaned.toString()
      .replaceAll("[\\t\\n\\r]+", " ")
      .replaceAll(" +", " ")
      .trim();
  }

  //for testing purposes
  protected static String testNormalizedMethodText(ECJParser parser, String methodText) {
    String compilationUnitSourceCode = "class C { " + methodText + " }";
    var cu = parser.parse("C", compilationUnitSourceCode);
    var comments = ECJFunctionExtractor.getComments((CompilationUnit) cu.ast());
    return normalizeMethodText(methodText, 10, comments);
  }

}
