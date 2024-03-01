package model;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import java.util.HashMap;
import java.util.Map;

public class ASTClassNode extends ASTNode {

  private final JClass jc;

  private double complexity = -1;

  private final Map<String, ASTMethodNode> methodNodeMap = new HashMap<>();



  public ASTClassNode(Node n, ASTNode astNode) {
    super(n, astNode);
    jc = new JClass(n);
    this.setName(jc.getName());
  }

  public Map<String, ASTMethodNode> getMethodNodeMap() {
    return methodNodeMap;
  }

  public boolean isSignificant() {
    String lowName = this.getName().toLowerCase();
    // avoid support functions
    if (lowName.contains("test")
        || lowName.contains("example")
        || lowName.contains("factory")
        || lowName.contains("adapter")
        || lowName.contains("converter")) {
      return false;
    }
    // if no method is significant, the class is not significant
    for (ASTMethodNode m : getMethodNodeMap().values()) {
      if (m.isSignificant()) {
        return true;
      }
    }
    return false;
  }

  public double getComplexity() {
    if (complexity == -1) {
      complexity = 0;
      for (ASTMethodNode m : getMethodNodeMap().values()) {
        if (m.isSignificant()) {
          complexity += m.getjMethod().getComplexity();
        }
      }
      for (ASTNode n : this.getChildren()) {
        if (n instanceof ASTClassNode) {
          complexity += ((ASTClassNode) n).getComplexity();
        }
      }
    }

    return complexity;
  }
}