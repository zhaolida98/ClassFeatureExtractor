package model;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.VoidType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ASTMethodNode extends ASTNode {

  private final Map<String, List<ASTNode>> indegreeMeth = new HashMap<>();
  private final Map<String, List<ASTNode>> outdegreeMeth = new HashMap<>();
  private JMethod jMethod;

  public ASTMethodNode(Node node, ASTNode astNode) {
    super(node, astNode);
    String returnType = "";
    String funcName = "";
    StringBuilder param = new StringBuilder();
    boolean isAbstractMethod = true;
    String prefix = "";
    if (node == null) {
      return;
    }
    for (Node child : node.getChildNodes()) {
      if (child instanceof SimpleName) {
        funcName = child.toString();
      }
      if (child instanceof Type) {
        returnType = child.toString();
      }
      if (child instanceof VoidType) {
        returnType = "void";
      }
      if (child instanceof Parameter) {
        for (Node paramChild : child.getChildNodes()) {
          if (paramChild instanceof Type) {
            param.append(prefix).append(paramChild);
            prefix = ",";
          }
        }
      }
      if (child instanceof BlockStmt) {
        isAbstractMethod = false;
      }
    }
    String methodId = String.format("%s; %s; %s", returnType, funcName, param);
    this.setName(methodId);
    if (isAbstractMethod) {
      this.jMethod = null;
    } else {
      this.jMethod = new JMethod(node.toString(), methodId);
    }
  }


  public Map<String, List<ASTNode>> getIndegreeMeth() {
    return indegreeMeth;
  }

  public Map<String, List<ASTNode>> getOutdegreeMeth() {
    return outdegreeMeth;
  }

  public JMethod getjMethod() {
    return jMethod;
  }

  public String getName() {
    return super.getName();
  }

  public boolean isSignificant() {
    return this.jMethod.isSignificant();
  }
}

