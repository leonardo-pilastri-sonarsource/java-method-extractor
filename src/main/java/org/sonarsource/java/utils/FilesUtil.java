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

  public static void writeMethodsToFile(List<FunctionInfo> functions, Path path, Path outputDir) throws IOException {
    String outputFileName = path.getFileName().toString().replaceAll("\\.java$", "");
    Path classDir = Files.createDirectories(outputDir.resolve(outputFileName));
    for (int i = 0; i < functions.size(); i++) {
      String methodFileName = outputFileName + "_" + (i + 1) + ".txt";
      Path methodOutputPath = classDir.resolve(methodFileName);
      writeContent(methodOutputPath, functions.get(i), 1);
    }
  }

  public static void writeContent(Path outputPath, FunctionInfo func, int size) {
    try {
      Files.writeString(outputPath, func.normalizedContent(), StandardCharsets.UTF_8,
        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
      System.out.println("Wrote " + size + " methods text to " + outputPath.toAbsolutePath());
    } catch (IOException e) {
      System.err.println("Was not able to write to " + outputPath.toAbsolutePath());
    }
  }

}
