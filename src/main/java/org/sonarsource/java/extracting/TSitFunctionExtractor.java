package org.sonarsource.java.extracting;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.sonarsource.java.parsing.AstResult;
import org.sonarsource.java.utils.PerformanceMetrics;
import org.treesitter.TSNode;

public class TSitFunctionExtractor implements IFunctionExtractor {

  @Override
  public List<FunctionInfo> extract(AstResult astResult, String source, int minLines, boolean oneline, PerformanceMetrics metrics) {
    Instant startTime = Instant.now();
    if (!(astResult.ast() instanceof TSNode rootNode)) {
      throw new RuntimeException("Root node is not a TSNode");
    }
    List<FunctionInfo> list = new ArrayList<>();
    traverse(rootNode, source, list, minLines, oneline, metrics);

    Duration processingTime = Duration.between(startTime, Instant.now());
    metrics.recordExtractionTime(processingTime.toNanos());
    return list;
  }

  private static void traverse(TSNode node, String source, List<FunctionInfo> out, int minLines, boolean oneline, PerformanceMetrics metrics) {
    String type = node.getType();
    if ("method_declaration".equals(type) || "constructor_declaration".equals(type)) {
      int start = node.getStartByte();
      int end = node.getEndByte();
      String content = source.substring(start, Math.min(end, source.length()));

      String name = extractNameViaTree(node, source);
      int lineCount = (int) content.lines().count();
      if (lineCount >= minLines) {
        var comments = new ArrayList<TSNode>();
        getComments(node, source.getBytes(), comments);
        comments.sort(Comparator.comparingInt(TSNode::getStartByte));

        Instant normStartTime = Instant.now();
        String normalizedContent = TextNormalizer.normalizeTSMethodText(content, node.getStartByte(), comments);
        if (normalizedContent.lines().count() < minLines) {
          return;
        }
        if (oneline) {
          normalizedContent = TextNormalizer.normalizeOneLine(normalizedContent);
        }

        Duration normProcessingTime = Duration.between(normStartTime, Instant.now());
        metrics.recordNormalizationTime(normProcessingTime.toNanos());

        out.add(new FunctionInfo(name, content, normalizedContent, 0));
      }
      return; // don't recurse inside methods
    }

    int childCnt = node.getChildCount();
    for (int i = 0; i < childCnt; i++) {
      TSNode child = node.getChild(i);
      if (child != null) {
        traverse(child, source, out, minLines, oneline, metrics);
      }
    }
  }

  /**
   * Extracts the name of a method/constructor purely via the Tree-sitter AST.
   * No regex fallback – if Tree-sitter doesn't give us a name we label it "anonymous".
   */
  private static String extractNameViaTree(TSNode methodNode, String source) {
    TSNode nameNode = methodNode.getChildByFieldName("name");
    if (nameNode != null) {
      int start = nameNode.getStartByte();
      int end = nameNode.getEndByte();
      if (start >= 0 && end <= source.length() && start < end) {
        return source.substring(start, end);
      }
    }
    return "anonymous";
  }

  public static void getComments(TSNode node, byte[] sourceBytes, List<TSNode> comments) {
    if (node == null) {
      return;
    }

    String nodeType = node.getType();
    if ("line_comment".equals(nodeType) || "block_comment".equals(nodeType)) {
      comments.add(node);
    } else {
      for (int i = 0; i < node.getChildCount(); i++) {
        TSNode child = node.getChild(i);
        getComments(child, sourceBytes, comments);
      }
    }
  }

}
