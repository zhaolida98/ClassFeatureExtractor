package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

public class FileUtils {

  public static String readFile(String path, Charset encoding)
      throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, encoding);
  }

  public static void writeToFile(String content, File filePath) {
    try {
      FileOutputStream outputStream = new FileOutputStream(filePath);
      byte[] strToBytes = content.getBytes();
      outputStream.write(strToBytes);

      outputStream.close();
      System.out.println("write file to " + filePath);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static List<File> findFilesToDepth(
      final String sourceDirectory, final String filenamePattern, final int maxDepth) {
    return findFilesToDepth(new File(sourceDirectory), filenamePattern, maxDepth);
  }

  public static List<File> findFilesToDepth(
      final File sourceDirectory, final String filenamePattern, final int maxDepth) {
    return findFilesRecursive(sourceDirectory, 0, maxDepth, null, true, filenamePattern);
  }

  private static List<File> findFilesRecursive(
      final File sourceDirectory,
      final int currentDepth,
      final int maxDepth,
      StringBuilder maxDepthHitMsgPattern,
      final Boolean recurseIntoDirectoryMatch,
      final String... filenamePatterns) {
    final List<File> files = new ArrayList<>();
    if (currentDepth >= maxDepth) {
      if (StringUtils.isNotBlank(maxDepthHitMsgPattern)) {
        System.err.printf((maxDepthHitMsgPattern) + "%n",
            sourceDirectory.getAbsolutePath());
        // Ensure msg only shown once
        maxDepthHitMsgPattern.setLength(0);
      }
    } else if (sourceDirectory.isDirectory()
        && sourceDirectory.listFiles().length > 0
        && null != filenamePatterns
        && filenamePatterns.length >= 1) {
      for (final File file : sourceDirectory.listFiles()) {
        final boolean fileMatchesPatterns =
            Arrays.stream(filenamePatterns)
                .anyMatch(pattern -> FilenameUtils.wildcardMatchOnSystem(file.getName(), pattern));

        if (fileMatchesPatterns && file.isFile()) {
          files.add(file);
        }

        if (file.isDirectory() && (!fileMatchesPatterns || recurseIntoDirectoryMatch)) {
          // only go into the directory if it is not a match OR it is a match and the flag is set to
          // go into matching directories
          files.addAll(
              findFilesRecursive(
                  file,
                  currentDepth + 1,
                  maxDepth,
                  maxDepthHitMsgPattern,
                  recurseIntoDirectoryMatch,
                  filenamePatterns));
        }
      }
    }
    return files;
  }

}
