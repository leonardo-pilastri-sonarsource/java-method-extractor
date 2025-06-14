package org.sonarsource.java;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class App {

  private static final List<String> allMethodsText = new ArrayList<>();

  public static void main(String[] args) throws IOException {
    if (args.length != 3) {
      System.err.println("Usage: java -jar app.jar --local <local_dir> <output_dir_name>");
      System.err.println("   or: java -jar app.jar --github <github_repo_url> <output_dir_name>");
      System.exit(1);
    }

    String mode = args[0];
    String inputPath = args[1];
    String outputDirName = args[2];

    var repoDir = getRepoPathByMode(mode, inputPath);

    Path outputDir = Path.of(outputDirName);
    Files.createDirectories(outputDir);

    var javaFiles = collectJavaFiles(repoDir);

    for (Path path : javaFiles) {
      String code = Files.readString(path, StandardCharsets.UTF_8);

      var compilationUnit = Parser.parse(path.toFile().getName(), code);
      if (compilationUnit != null) {
        List<String> methodsText = FunctionExtractor.extractMethodsText(compilationUnit, code);
        writeMethodsToFile(methodsText, path, outputDir);
      }
    }
  }

  private static Path getRepoPathByMode(String mode, String inputPath) {
    Path repoDir = null;
    switch (mode) {
      case "--local":
        repoDir = Path.of(inputPath);
        if (!Files.exists(repoDir) || !Files.isDirectory(repoDir)) {
          System.err.println("Provided path " + inputPath + " is not an existing directory");
          System.exit(1);
        }
        break;

      case "--github":
        if (!GitUtils.isPublicGitHubRepo(inputPath)) {
          System.err.println("Provided URL " + inputPath + " is not a public GitHub repository");
          System.exit(1);
        }
        repoDir = GitUtils.cloneRepository(inputPath);
        break;

      default:
        System.err.println("Invalid mode: " + mode);
        System.err.println("Use --local for a local directory or --github for a GitHub repository");
        System.exit(1);
    }
    return repoDir;
  }

  private static List<Path> collectJavaFiles(Path dir) throws IOException {
    List<Path> javaFiles;
    try (Stream<Path> paths = Files.walk(dir)) {
      javaFiles = paths
        .filter(Files::isRegularFile)
        .filter(p -> p.toString().endsWith(".java"))
        .collect(Collectors.toList());
    }
    return javaFiles;
  }

  private static void writeMethodsToFile(List<String> methods, Path path, Path outputDir) {
    String outputFileName = path.getFileName().toString().replaceAll("\\.java$", "");
    Path outputPath = outputDir.resolve(outputFileName);
    String content = String.join("\n\n", methods); // separati da una riga vuota
    try {
      Files.writeString(outputPath, content, StandardCharsets.UTF_8,
        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
      System.out.println("Wrote " + methods.size() + " methods text to " + path.toAbsolutePath());
    } catch (IOException e) {
      System.err.println("Was not able to write to " + path.toAbsolutePath());
    }
  }

}
