package model;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import java.util.HashMap;
import java.util.Map;

public class ASTClassNode extends ASTNode {

  private final JClass jc;

  private final Map<String, ASTMethodNode> methodNodeMap = new HashMap<>();

  public ASTClassNode(JClass jc) {
    super(new ClassOrInterfaceDeclaration(), null);
    this.jc = jc;
    super.setName(jc.getName());
    this.getChildren().clear();
  }


  public ASTClassNode(Node n, ASTNode astNode) {
    super(n, astNode);
    jc = new JClass(n);
    this.setName(jc.getName());
  }

  public Map<String, ASTMethodNode> getMethodNodeMap() {
    return methodNodeMap;
  }

  public boolean isSignificant() {
    for (ASTMethodNode m : getMethodNodeMap().values()) {
      if (m.isSignificant()) {
        return true;
      }
    }
    return false;
  }
}
