package com.flyspring.flyfly.commands.jbang;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.*;

@Component
@Command(name = "jbang", description = "Activate jbang through the jar placed in resources")
public class JbangCommand implements Runnable {

  @Parameters(description = "Java file to be executed with jbang")
  private String javaFile;

  @Parameters(description = "ClassPath Jar to be used")
  private String classPathJar;

  @Override
  public void run() {
    String resourcePath = "/jbang.jar";
    try {
      File jarFile = extractFileFromResources(resourcePath);
      if (jarFile != null) {
        runJbang(jarFile, javaFile, classPathJar);
      } else {
        System.out.println("Could not find jbang.jar in resources.");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private File extractFileFromResources(String resourcePath) throws IOException {
    InputStream inputStream = getClass().getResourceAsStream(resourcePath);
    if (inputStream == null) {
      return null;
    }
    File jarFile = File.createTempFile("jbang", ".jar");
    jarFile.deleteOnExit();
    try (FileOutputStream outputStream = new FileOutputStream(jarFile)) {
      inputStream.transferTo(outputStream);
    }
    return jarFile;
  }

  private void runJbang(File jarFile, String javaFile, String classPathJar) {
    try {
      // Step One: Execute the initial command to get the classpath
      ProcessBuilder pb = new ProcessBuilder(
          "java",
          "-cp",
          jarFile.getAbsolutePath(),
          "dev.jbang.Main",
          "--cp",
          classPathJar,
          javaFile);
      pb.redirectErrorStream(true);
      Process process = pb.start();
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String classPath = extractClassPathFromOutput(bufferedReader);
      String mainClass = null;

      if (classPath.contains(".java")) {
        mainClass = extractClassName(classPath);
      }

      System.out.println("Extracted Classpath: " + classPath);
      System.out.println("Main Class: " + mainClass);

      process.waitFor();

      // Step Two: Execute the final command with the extracted classpath
      if (classPath != null && !classPath.isEmpty() && mainClass != null && !mainClass.isEmpty()) {
        runJavaWithClassPath(classPath, mainClass);
      } else {
        System.out.println("Could not extract classpath or main class from the output.");
      }
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  private String extractClassPathFromOutput(BufferedReader bufferedReader) throws IOException {
    String line;
    String classPath = null;
    final String pattern = "-classpath '";
    while ((line = bufferedReader.readLine()) != null) {
      // System.out.println("Line: " + line); // added debug message
      int startIndex = line.indexOf(pattern);
      if (startIndex > -1) {
        startIndex += pattern.length();
        int endIndex = line.indexOf('\'', startIndex);
        if (endIndex > startIndex) {
          classPath = line.substring(startIndex, endIndex);
          break;
        }
      }
    }
    return classPath;
  }

  private String extractClassName(String classPath) {
    // Assuming that the class name with package follows the format "com.packagename.ClassName.java"
    int lastIndex = classPath.lastIndexOf(".java");
    if (lastIndex > -1) {
      String fileName = classPath.substring(0, lastIndex); // Extracting "com.packagename.ClassName"
      String[] parts = fileName.split("/");
      StringBuilder packageName = new StringBuilder();
      for (int i = 0; i < parts.length - 1; i++) {
        packageName.append(parts[i]);
        if (i < parts.length - 2) {
          packageName.append(".");
        }
      }
      String className = parts[parts.length - 1];
      return packageName.toString() + "." + className;
    }
    return null;
  }

  private void runJavaWithClassPath(String classPath, String mainClass) {
    try {
      ProcessBuilder pb = new ProcessBuilder("java", "-classpath", classPath, mainClass);
      pb.inheritIO();
      pb.start().waitFor();
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }
}
