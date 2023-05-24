package Strategy;

import com.google.gson.JsonObject;
import java.io.File;
import model.ASTNode;

public class FeatureContext {

  private Strategy strategy;

  public FeatureContext() {

  }

  public FeatureContext(Strategy strategy) {
    this.strategy = strategy;
  }

  public void setStrategy(Strategy strategy) {
    this.strategy = strategy;
  }

  public JsonObject executeStrategy(File path) {
    JsonObject jsonObject = strategy.execute(path);
    return jsonObject;
  }
}
