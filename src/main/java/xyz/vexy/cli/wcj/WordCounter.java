package xyz.vexy.cli.wcj;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.Boolean;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordCounter {
  public static int countLinesInFile(
      final File file,
      final Boolean countBlank) {
    if (file.isDirectory()) {
      System.out.println(
          "Skipping " + file.getName() + " because it is a directory");

      return 0;
    }

    if (!file.canRead()) {
      System.out.println(
          "WARNING: " + file.getName() + " could not be read");

      return 0;
    }

    FileReader fr;

    try {
      fr = new FileReader(file);
    } catch (FileNotFoundException e) {
      System.out.println(
          "[WARNING] File " + file.getName() + " could not be found, skipping");

      return 0;
    }

    BufferedReader br;
    int lineCount = 0;

    try {
      br = new BufferedReader(fr);
      String line;

      while ((line = br.readLine()) != null) {
        if (!countBlank && line.isBlank())
          continue;

        lineCount++;
      }
    } catch (IOException e) {
      System.out.println(
          "[WARNING] Error reading "
              + file.getName()
              + " (after "
              + lineCount
              + " lines):"
              + e.getMessage());

      return 0;
    }

    try {
      br.close();
    } catch (IOException e) {
      System.out.println("[WARNING] Failed to close reader for file " + file.getName());
    }

    return lineCount;
  }

  public static int countLinesInFile(final File file) {
    return WordCounter.countLinesInFile(file, true);
  }

  public static Map<String, Integer> countLinesInDir(
      final File file,
      final Pattern filter,
      final Pattern ignore,
      final Boolean countBlank,
      Map<String, Integer> lineCounts) {
    if (!file.isDirectory()) {
      System.out.println("[WARNING] " + file.getName() + " is not a directory");
      return new HashMap<>();
    }

    final File[] fileList = file.listFiles();

    for (File thisFile : fileList) {
      if (!thisFile.canRead()) {
        System.out.println("[WARNING] File " + thisFile.getName() + " could not be read");
        continue;
      }

      if (thisFile.isDirectory()) {
        lineCounts = countLinesInDir(thisFile, filter, ignore, countBlank, lineCounts);
        continue;
      }

      Matcher fMatcher = filter.matcher(thisFile.getPath());
      Matcher iMatcher = ignore.matcher(thisFile.getPath());

      if (fMatcher.matches() && !iMatcher.matches()) {
        lineCounts.put(thisFile.getPath(), countLinesInFile(thisFile, countBlank));
      }
    }

    return lineCounts;
  }

  public static Map<String, Integer> countLinesInDir(
      final File file,
      final Pattern filter,
      final Pattern ignore,
      final Boolean countBlank) {
    return countLinesInDir(file, filter, ignore, countBlank, new HashMap<>());
  }
}
