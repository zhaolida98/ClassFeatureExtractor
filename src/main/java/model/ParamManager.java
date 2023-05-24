package model;

import com.beust.jcommander.Parameter;
import com.google.gson.annotations.SerializedName;
import utils.Constant;

public class ParamManager {
  @Parameter(names = "-f", description = "file path")
  private String filePath = "";
  @Parameter(names = {"-p", "--printTree"}, description = "print tree")
  private boolean printTree = false;
  @Parameter(names = "--debug", description = "debug", hidden = true)
  private boolean debug = false;
  @Parameter(names = "-o", description = "output dir")
  private String output = System.getProperty("user.dir");
  @Parameter(names = "-s", description = "strategy name in [classHashSet, classVec, functionHash]")
  private String strategyName = "";
  @Parameter(names = "-h", description = "help")
  private boolean help = false;
  @Parameter(names = "-t", description = "threshold. default to 60")
  private int threshold = 60;


  public static ParamManager paramManager;


  public static ParamManager getInstance() {
    if (paramManager == null) {
      paramManager = new ParamManager();
      Constant.CLASS_THRESHOLD = paramManager.threshold;
    }
    return paramManager;
  }

  public String getFilePath() {
    return filePath;
  }

  public boolean isPrintTree() {
    return printTree;
  }

  public boolean isDebug() {
    return debug;
  }

  public String getOutput() {
    return output;
  }

  public String getStrategyName() {
    return strategyName;
  }

  public boolean isHelp() {
    return help;
  }
}
