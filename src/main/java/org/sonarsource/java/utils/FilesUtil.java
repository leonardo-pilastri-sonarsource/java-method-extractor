package org.sonarsource.java.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import org.sonarsource.java.extracting.FunctionInfo;

public class FilesUtil {

  private FilesUtil() {
    // Prevent instantiation
  }

  /**
   * When in --oneline mode, only 1 file per class is created, containing all methods
   * Otherwise, a directory is created for each class and inside a file for each method
   */
  public static void writeMethodsToFile(List<FunctionInfo> functions, Path path, Path outputDir, boolean oneline) throws IOException {
    String outputFileName = path.getFileName().toString().replaceAll("\\.java$", "");
    if (oneline) {
      // If methods are oneliners we create a file for each class with all methods inside
      Path classFile = outputDir.resolve(outputFileName + ".txt");
      StringBuilder sb = new StringBuilder();
      for (FunctionInfo function : functions) {
        sb.append(function.normalizedContent()).append("\n\n");
      }
      writeContent(classFile, sb.toString(), functions.size());
    } else {
      // If methods are not oneliners we create a directory for each class and inside
      // a file for each method
      Path classDir = Files.createDirectories(outputDir.resolve(outputFileName));
      for (int i = 0; i < functions.size(); i++) {
        String methodFileName = outputFileName + "_" + (i + 1) + ".txt";
        Path methodOutputPath = classDir.resolve(methodFileName);
        writeContent(methodOutputPath, functions.get(i).normalizedContent(), 1);
      }
    }

  }

  public static void writeContent(Path outputPath, String content, int size) {
    try {
      Files.writeString(outputPath, content, StandardCharsets.UTF_8,
        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
      System.out.println("Wrote " + size + " methods text to " + outputPath.toAbsolutePath());
    } catch (IOException e) {
      System.err.println("Was not able to write to " + outputPath.toAbsolutePath());
    }
  }

}
