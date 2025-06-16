package org.sonarsource.java.utils;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

public class GitUtils {

  public static boolean isPublicGitHubRepo(String repoUrl) {
    try {
      // Clean URL for API use: remove ".git" suffix if present
      if (!repoUrl.startsWith("https://github.com/")) return false;

      String cleanedUrl = repoUrl.replaceFirst("\\.git$", "");
      // Use GitHub's HTML view to check accessibility
      URL url = new URL(cleanedUrl);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("HEAD");
      connection.setInstanceFollowRedirects(true);
      connection.connect();

      int responseCode = connection.getResponseCode();
      return responseCode == 200; // OK
    } catch (IOException e) {
      return false;
    }
  }

  public static Path cloneRepository(String repoUrl) {
    URI uri = URI.create(repoUrl);
    String repoName = new File(uri.getPath()).getName().replace(".git", "");
    String prefix = "cloned-" + repoName + "-";
    try {
      Path tempDir = Files.createTempDirectory(prefix);
      System.out.println("Cloning repository to: " + tempDir);

      clone(repoUrl, tempDir);

      return tempDir;
    } catch (IOException e) {
      System.err.println("Could not create temp dir: " + prefix);
      throw new RuntimeException(e);
    }
  }

  private static void clone(String repoUrl, Path tempDir) {
    try {
      var cmd = Git.cloneRepository()
        .setURI(repoUrl)
        .setDirectory(tempDir.toFile())
        .call();
      cmd.close();
    } catch (GitAPIException e) {
      System.err.println("Could not clone repo: " + repoUrl);
      System.exit(1);
    }
  }

}
