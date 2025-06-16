package org.sonarsource.java;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.sonarsource.java.extracting.ECJFunctionExtractor;
import org.sonarsource.java.extracting.FunctionInfo;
import org.sonarsource.java.extracting.IFunctionExtractor;
import org.sonarsource.java.extracting.TSitFunctionExtractor;
import org.sonarsource.java.parsing.AstResult;
import org.sonarsource.java.parsing.ECJParser;
import org.sonarsource.java.parsing.IParser;
import org.sonarsource.java.parsing.TSitParser;

public class App {

  private static final List<String> allMethodsText = new ArrayList<>();
  private static int minLines = 0;
  private static IParser parser;
  private static IFunctionExtractor functionExtractor;

  /**
   * @param args List of arguments:
   *             1st mandatory: --local or --github
   *             2nd mandatory: path to local directory or GitHub repository URL
   *             3rd mandatory: output directory name
   *             optional: --ml <minLines> to set the minimum number of lines for a method to be evaluated
   *             --ecj to use ECJ parser (default)
   *             --ts to use Tree-sitter parser
   */
  public static void main(String[] args) throws IOException {
    if (args.length < 3) {
      System.err.println("Usage: java -jar app.jar --local <local_dir> <output_dir_name>");
      System.err.println("   or: java -jar app.jar --github <github_repo_url> <output_dir_name>");
      System.exit(1);
    }

    String mode = args[0];
    String inputPath = args[1];
    String outputDirName = args[2];
    checkExtraArgs(args);

    var repoDir = getRepoPathByMode(mode, inputPath);

    Path outputDir = Path.of(outputDirName);
    if (Files.exists(outputDir)) {
      System.err.println("Provided output path " + outputDirName + " already exists");
      System.exit(1);
    }
    Files.createDirectories(outputDir);

    var javaFiles = collectJavaFiles(repoDir);

    for (Path path : javaFiles) {
      String code = Files.readString(path, StandardCharsets.UTF_8);

      AstResult astResult = parser.parse(path.toFile().getName(), code);
      if (astResult != null) {
        List<FunctionInfo> functions = functionExtractor.extract(astResult, code, minLines);
        FilesUtil.writeMethodsToFile(functions, path, outputDir);
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

  private static void checkExtraArgs(String[] args) {
    int index = 3;
    while (index < args.length) {
      boolean expectsValue = checkArg(args, index);
      if (expectsValue) {
        index++;
      }
      index++;
    }
    //Default implementation is ECJ if no parameter is provided
    if (functionExtractor == null) {
      functionExtractor = new ECJFunctionExtractor();
    }
    if (parser == null) {
      parser = new ECJParser();
    }
  }

  /**
   * @return true if the argument expects a value
   */
  private static boolean checkArg(String[] args, int idx) {
    if ("--ml".equals(args[idx])) {
      if (args.length > idx + 1) {
        parseMinLines(args[idx + 1]);
        return true;
      } else {
        System.err.println("Missing value for --ml");
        System.exit(1);
      }
    } else if ("--ecj".equals(args[idx])) {
      functionExtractor = new ECJFunctionExtractor();
      parser = new ECJParser();
    } else if ("--ts".equals(args[idx])) {
      functionExtractor = new TSitFunctionExtractor();
      parser = new TSitParser();
    } else {
      System.err.println("Unknown argument: " + args[idx]);
      System.exit(1);
    }
    return false;
  }

  private static void parseMinLines(String arg) {
    try {
      minLines = Integer.parseInt(arg);
    } catch (NumberFormatException e) {
      System.err.println("Invalid value for --ml: " + arg);
      System.exit(1);
    }
  }

}
