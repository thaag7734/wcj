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
    String ignore = null;
    Boolean countBlank = false;
    Boolean recurse = false;
    Boolean filterSet = false;

    for (int i = 1; i < args.length; i++) {
      switch (args[i]) {
        case "-m":
        case "--match":
          if (args.length <= i + 1) {
            System.out.println("[ERROR] Option --match requires an argument");
            return;
          }

          filterSet = true;
          filter = args[++i];
          break;
        case "-i":
        case "--ignore":
          if (args.length <= i + 1) {
            System.out.println("[ERROR] Option --ignore requires an argument");
            return;
          }

          ignore = args[++i];
          break;
        case "-b":
        case "--count-blank":
          countBlank = true;
          break;
        case "-R":
        case "--recurse":
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

    if (filterSet && !recurse) {
      System.out.println(
          "[ERROR] Option --regex requires option --recurse to be set");
      return;
    }

    if (ignore != null && !recurse) {
      System.out.println(
          "[ERROR] Option --ignore requires option --recurse to be set");
      return;
    }

    Pattern mPattern;
    Pattern iPattern;

    try {
      mPattern = Pattern.compile(filter);
    } catch (PatternSyntaxException e) {
      System.out.println("[ERROR] Invalid regex passed to option --match: " + e.getMessage());
      return;
    }

    try {
      // "a^" is an unmatchable string, so we default to it so we
      // can ignore nothing by default
      iPattern = Pattern.compile(ignore != null ? ignore : "a^");
    } catch (PatternSyntaxException e) {
      System.out.println("[ERROR] Invalid regex passed to option --ignore: " + e.getMessage());
      return;
    }

    File file = new File(args[0]);

    if (recurse) {
      Map<String, Integer> counts = WordCounter.countLinesInDir(file, mPattern, iPattern, countBlank);

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
            "  -R | --recurse..............Treat <file> as a directory and recursively count all its descendants");
    System.out.println(
        "  -m | --match [pattern]........Only count in files whose names match pattern (requires that --recurse is set)");
    System.out.println(
        "  -i | --ignore [pattern]........Skip counting in files whose names match pattern (requires that --recurse is set)");
    System.out.println("  -b | --count-blank............Include blank lines in the count");
    System.out.println("  -h | --help...................Show this message");
  }
}
