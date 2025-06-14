package org.sonarsource.java;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class FunctionExtractor {

  public static List<String> extractMethodsText(CompilationUnit cu, String source) {
    List<String> methodList = new ArrayList<>();
    List<Comment> comments = getComments(cu);

    cu.accept(new ASTVisitor() {
      @Override
      public boolean visit(MethodDeclaration node) {
        int start = node.getStartPosition();
        int length = node.getLength();
        String methodSource = source.substring(start, start + length);
        methodList.add(normalizeMethodText(methodSource, start, comments));
        return super.visit(node);
      }
    });

    return methodList;
  }

  private static List<Comment> getComments(CompilationUnit cu) {
    var comments = new ArrayList<Comment>(cu.getCommentList());
    comments.sort((a, b) -> Integer.compare(b.getStartPosition(), a.getStartPosition()));
    return comments;
  }

  private static String normalizeMethodText(String methodText, int methodStartOffset, List<Comment> comments) {
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
      .replaceAll("[\\t\\n\\r]+", " ")   // tab e newline → spazio
      .replaceAll(" +", " ")             // spazi multipli → singolo spazio
      .trim();
  }

  //for testing purposes
  protected static String testNormalizedMethodText(String methodText) {
    String compilationUnitSourceCode = "class C { " + methodText + " }";
    var cu = Parser.parse("C", compilationUnitSourceCode);
    var comments = getComments(cu);
    return normalizeMethodText(methodText, 10, comments);
  }

}
