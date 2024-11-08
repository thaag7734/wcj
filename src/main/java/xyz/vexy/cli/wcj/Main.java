package xyz.vexy.cli.wcj;

import java.io.File;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Main {
  public static void main(String[] args) {
    if (args.length < 1) {
      printUsage();
      return;
    }

    String filter = ".+";
    Boolean countBlank = false;
    Boolean recurse = false;
    Boolean regexSet = false;

    for (int i = 1; i < args.length; i++) {
      switch (args[i]) {
        case "-r":
        case "--regex":
          if (args.length <= i + 1) {
            System.out.println("[ERROR] Option --regex requires an argument");
            return;
          }

          regexSet = true;
          filter = args[i + 1];
          i++;
          break;
        case "-b":
        case "--count-blank":
          countBlank = true;
          break;
        case "-R":
        case "--recursive":
          recurse = true;
          break;
        case "-h":
        case "--help":
          printUsage();
          return;
        default:
          System.out.println("[ERROR] Invalid option: " + args[i] + "\n");
          printUsage();
          return;
      }
    }

    if (regexSet && !recurse) {
      System.out.println(
          "[ERROR] Option --regex requires option --recursive to be set");
      return;
    }

    Pattern pattern;

    try {
      pattern = Pattern.compile(filter);
    } catch (PatternSyntaxException e) {
      System.out.println("[ERROR] Invalid regex: " + e.getMessage());
      return;
    }

    File file = new File(args[0]);

    if (recurse) {
      Map<String, Integer> counts = WordCounter.countLinesInDir(file, pattern, countBlank);

      int total = 0;
      int fileCount = 0;

      int maxCount = 0;
      for (Map.Entry<String, Integer> entry : counts.entrySet()) {
        int val = entry.getValue();

        if (val > maxCount)
          maxCount = val;

        total += val;
        fileCount++;
      }

      int charNum = String.valueOf(maxCount).length();
      for (Map.Entry<String, Integer> entry : counts.entrySet()) {
        String key = entry.getKey();

        String numberToPrint = String.valueOf(entry.getValue());
        while (numberToPrint.length() < charNum) {
          numberToPrint = " " + numberToPrint;
        }

        System.out.println(numberToPrint + " : " + key);
      }

      System.out.println(total + " total lines in " + fileCount + " files");
    } else {
      System.out.println(WordCounter.countLinesInFile(file) + " : " + file.getPath());
    }

    return;
  }

  public static void printUsage() {
    System.out.println("Usage: wcj <file> [options]\n");
    System.out.println("Options:");
    System.out
        .println(
            "  -R | --recursive..............Treat <file> as a directory and recursively count all its descendants");
    System.out.println(
        "  -r | --regex [pattern]........Only count in files whose names match pattern (requires that --recursive is set)");
    System.out.println("  -b | --count-blank............Include blank lines in the count");
    System.out.println("  -h | --help...................Show this message");
  }
}
