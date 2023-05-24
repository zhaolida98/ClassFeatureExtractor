package Strategy;

import com.google.gson.JsonObject;
import java.io.File;
import model.ASTNode;

public interface Strategy {

  JsonObject execute(File path);

}
