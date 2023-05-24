import static Strategy.EnumStrategy.CLASS_HASHSET;
import static Strategy.EnumStrategy.CLASS_VEC;
import static Strategy.EnumStrategy.FUNCTION_HASH;

import Strategy.ClassHashSetStrategy;
import Strategy.ClassVecStrategy;
import Strategy.FeatureContext;
import Strategy.FunctionHashStrategy;
import com.beust.jcommander.JCommander;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import model.ParamManager;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.Constant;
import utils.FileUtils;

public class Main {
  private static final ParamManager paramManager = ParamManager.getInstance();
  private static final Logger logger = LogManager.getLogger(Main.class);

  public static void main(String[] args) throws IOException {
    // resolve parameters
    JCommander jc = JCommander.newBuilder().addObject(paramManager).build();
    try {
      jc.parse(args);
      jc.setProgramName("Java Feature Extractor");
    } catch (Exception e) {
      logger.info(
          "Error parsing command parameters.\nPlease use `-help` for available commands\n\n"
              + ExceptionUtils.getStackTrace(e));
      System.exit(2);
    }
    if (paramManager.isHelp()) {
      jc.usage();
      return;
    }
    LogManager.getRootLogger();

    String filePath = paramManager.getFilePath();
    // resolve path list, record all .java files
    File file = new File(filePath);
    List<File> pathList = new ArrayList<>();
    if (file.isDirectory()) {
      pathList = FileUtils.findFilesToDepth(filePath, "*.java", Integer.MAX_VALUE);
    } else if (file.isFile()) {
      pathList.add(file);
    } else {
      System.err.println("File not found: " + filePath);
    }
    if (paramManager.isDebug()) {
      logger.info(String.format("Threshold %d %n", Constant.SIGNIFICANCE_THRESHOLD));
      logger.info(String.format("Found %d files%n", pathList.size()));
      for (File p : pathList) {
        System.out.println("  - " + p.getAbsolutePath());
      }
    }
    String strategyName = paramManager.getStrategyName();
    JsonObject totalJsonObject = new JsonObject();
    int cnt = 0;
    for (File path : pathList) {
      logger.debug("On " + path.getAbsolutePath());
      try {
        // start extracting features of all class of all java files
        FeatureContext featureContext = new FeatureContext();
        if (strategyName.equals(CLASS_HASHSET.getName())) {
          featureContext.setStrategy(new ClassHashSetStrategy());
        } else if (strategyName.equals(CLASS_VEC.getName())) {
          featureContext.setStrategy(new ClassVecStrategy());
        } else if (strategyName.equals(FUNCTION_HASH.getName())) {
          featureContext.setStrategy(new FunctionHashStrategy());
        } else {
          logger.error("Unacceptable strategy " + strategyName + ". Only in [classHashSet, classVec, functionHash]");
        }
        JsonObject jsonObject = featureContext.executeStrategy(path);
        for (Entry<String, JsonElement> entry : jsonObject.asMap().entrySet()) {
          totalJsonObject.add(entry.getKey(), entry.getValue());
        }
      } catch (Error error) {
        logger.error(String.format("failed processing %s%n%s", path.getAbsolutePath(), error.getMessage()));
        continue;
      } catch (Exception exception) {
        logger.error(String.format("failed processing %s%n", path.getAbsolutePath()));
        exception.printStackTrace();
        continue;
      }
      logger.debug(String.format("finish processing %d/%d%n", ++cnt, pathList.size()));
    }

    // write to file out.json
    File outFile = new File(paramManager.getOutput());
    if (outFile.isDirectory()) {
      outFile = new File(Paths.get(outFile.getAbsolutePath(), "feature.json").toString());
    } else {
      File outFileDir = new File(outFile.toPath().getParent().toString());
      if (!outFileDir.isDirectory()) {
        Files.createDirectories(outFileDir.toPath());
      }
    }
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    JsonElement je = JsonParser.parseString(totalJsonObject.toString());
    String prettyJsonString = gson.toJson(je);
    FileUtils.writeToFile(prettyJsonString, outFile);
  }
}
