package org.sonarsource.java.extracting;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.sonarsource.java.parsing.AstResult;

public class ECJFunctionExtractor implements IFunctionExtractor {

  @Override
  public List<FunctionInfo> extract(AstResult astResult, String source, int minLines, boolean oneline) {
    if (!(astResult.ast() instanceof CompilationUnit cu)) {
      throw new RuntimeException("Root node is not a CompilationUnit");
    }
    List<FunctionInfo> methodList = new ArrayList<>();
    List<Comment> comments = getComments(cu);

    cu.accept(new ASTVisitor() {
      @Override
      public boolean visit(MethodDeclaration node) {
        int start = node.getStartPosition();
        int length = node.getLength();
        String methodName = node.getName().getIdentifier();
        String methodContent = source.substring(start, start + length);
        String methodNormalizedContent = TextNormalizer.normalizeECJMethodText(methodContent, start, comments);
        if (methodNormalizedContent.lines().count() < minLines) {
          return false;
        }
        if (oneline) {
          methodNormalizedContent = TextNormalizer.normalizeOneLine(methodNormalizedContent);
        }
        methodList.add(new FunctionInfo(methodName, methodContent, methodNormalizedContent, 0));
        return false; // don't recurse inside methods
      }
    });
    return methodList;
  }

  static List<Comment> getComments(CompilationUnit cu) {
    var comments = new ArrayList<Comment>(cu.getCommentList());
    comments.sort((a, b) -> Integer.compare(b.getStartPosition(), a.getStartPosition()));
    return comments;
  }

}
