import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import model.ASTClassNode;
import model.ASTNode;
import utils.ASTNodeUtils;
import utils.FileUtils;

public class Main {

  private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
      .create();

  private static String filePath = "";

  private static boolean printTree = false;
  private static boolean debug = false;
  private static String output = System.getProperty("user.dir");

  private static final String readMe = "Java Class Feature Extractor\n"
      + "============================\n"
      + "-f: file path, can be file or folder\n"
      + "-o: output path dir\n"
      + "--printTree: default false\n";


  public static void main(String[] args) throws IOException {
    // resolve parameters
    Iterator<String> it = Arrays.stream(args).iterator();
    while (it.hasNext()) {
      String flag = it.next();
      if (flag.startsWith("--")) {
        switch (flag) {
          case "--printTree":
            printTree = !printTree;
            break;
          case "--debug":
            debug = !debug;
            break;
          default:
            System.out.println(readMe);
        }
      } else if (flag.startsWith("-")) {
        String value = it.next();
        switch (flag) {
          case "-f":
            filePath = value;
            break;
          case "-o":
            output = value;
            break;
          default:
            System.out.println(readMe);
            break;
        }
      } else {
        System.err.println("Invalid args found: " + flag);
        System.exit(1);
      }
    }

    // resolve path list, record all .java files
    File file = new File(filePath);
    List<File> pathList = new ArrayList<>();
    BasicFileAttributes basicFileAttributes = Files.readAttributes(file.toPath(),
        BasicFileAttributes.class);
    if (basicFileAttributes.isRegularFile()) {
      pathList.add(file);
    } else if (basicFileAttributes.isDirectory()) {
      pathList = FileUtils.findFilesToDepth(filePath, "*.java", Integer.MAX_VALUE);
    } else {
      System.err.println("File not found: " + filePath);
    }
    if (debug) {
      System.out.printf("Found %d files%n", pathList.size());
      for (File p : pathList) {
        System.out.println("  - " + p.getAbsolutePath());
      }
    }

    // start extracting features of all class of all java files
    JsonObject jsonObject = new JsonObject();

    int cnt = 0;
    for (File path : pathList) {
      if (debug) {
        System.out.println("On " + path.getAbsolutePath());
      }

      ClassParser classParser = new ClassParser();
      ASTNode fullAst = classParser.buildFullAST(path);
      List<ASTNode> resolvedAST = new ArrayList<>();
      List<ASTClassNode> classNodeList = classParser.extractClassNode(fullAst);
      for (ASTClassNode cn : classNodeList) {
        if (cn.isSignificant()) {
          resolvedAST.add(classParser.methodLinker(cn));
        }
      }
      // print JClass
      if (printTree) {
        if (pathList.size() < 5) {
          for (ASTNode cn : resolvedAST) {
            ASTNodeUtils.walkNode(cn, 0);
            System.out.println();
          }
        } else {
          System.err.println("too many files to print");
        }
      }

      // transfer class features to json object
      for (ASTNode cn : resolvedAST) {
        ASTComparator astComparator = new ASTComparator((ASTClassNode) cn);

        JsonElement nodeHash = GSON.toJsonTree(astComparator.getNodeHashMap());
        String addr = cn.getName() + "@" + path.getAbsolutePath();
        jsonObject.add(addr, nodeHash);
      }
      System.out.printf("finish processing %d/%d%n", ++cnt, pathList.size());
    }

    // write to file out.json
    File outFile = new File(output);
    if (outFile.isDirectory()) {
      outFile = new File(Paths.get(output, "feature.json").toString());
    }
    FileUtils.writeToFile(jsonObject.toString(), outFile);
  }
}
