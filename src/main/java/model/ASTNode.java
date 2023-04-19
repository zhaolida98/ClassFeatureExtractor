package model;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import utils.ASTNodeUtils;

public class ASTNode implements Cloneable {

  private final List<ASTNode> children = new ArrayList<>();
  private final ASTNode parent;
  private Node node;
  private ASTNode funcRef;

  private String name;

  private HashMap<Integer, Integer> edgeVector = null;

  public ASTNode(Node node, ASTNode parentNode) {
    node = node.removeComment();
    this.node = node;
    this.parent = parentNode;
    if (node != null) {
      for (Node n : node.getChildNodes()) {
        ASTNode astNode = null;
        if (n instanceof ImportDeclaration || n instanceof PackageDeclaration
            || n instanceof AnnotationExpr || n instanceof Comment
            || n instanceof Modifier) {
          continue;
        }
        String[] classNameList = node.getClass().getName().split("\\.");
        String className = classNameList[classNameList.length - 1];
        if (!NodeTypeEnum.TypeCollection.containsKey(className)) {
          System.err.println(node.getClass().getName());
        }
        if (n instanceof ClassOrInterfaceDeclaration) {
          astNode = new ASTClassNode(n, this);
        } else if (n instanceof MethodDeclaration) {
          astNode = new ASTMethodNode(n, this);
        } else {
          astNode = new ASTNode(n, this);
        }
        // exclude abstract function
        if (!(astNode instanceof ASTMethodNode) || ((ASTMethodNode) astNode).getjMethod() != null) {
          this.children.add(astNode);
        }
      }
    }
  }


  public ASTNode clone() {
    ASTNode myclone = new ASTNode(this.node, null);
    // using children of ASTNode instead of the origin Node children
    myclone.getChildren().clear();
    for (ASTNode n : children) {
      myclone.addChildren(n.clone());
    }
    return myclone;
  }

  public Node getNode() {
    return node;
  }

  public void setNode(Node node) {
    this.node = node;
  }

  public ASTNode getParent() {
    return parent;
  }

// To avoid cloning object with high cost, we re-used the same object multiple times
// However, only one parent is allowed for each object.
//  public void setParent(ASTNode parent) {
//    this.parent = parent;
//  }

  public List<ASTNode> getChildren() {
    return children;
  }

//  public void setChildren(List<ASTNode> children) {
//    this.children = children;
//  }

  public void addChildren(ASTNode astNode) {
    this.children.add(astNode);
//    ASTNode parent = astNode.getParent();
//    if (parent != null && parent != this) {
//      parent.getChildren().remove(astNode);
//    }
//    astNode.parent = this;
  }

  public ASTNode getFuncRef() {
    return funcRef;
  }

  public void setFuncRef(ASTNode funcRef) {
    this.funcRef = funcRef;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public HashMap<Integer, Integer> getEdgeVector(ASTNode parent) {
    if (this.edgeVector == null) {
      int nodeTypeSize = NodeTypeEnum.TypeCollection.size();
      this.edgeVector = new HashMap<>();
      // ASTNode build in parent is different from the param here
      // To avoid cloning object with high cost, we re-used the same object multiple times
      // However, only one parent is allowed for each object.
      if (parent != null) {
        String parentClassName = parent.getNode().getClass().getSimpleName();
        String currentClassname = this.getNode().getClass().getSimpleName();
        if (NodeTypeEnum.TypeCollection.containsKey(parentClassName)
            && NodeTypeEnum.TypeCollection.containsKey(currentClassname)) {
          int parentClassSeq = NodeTypeEnum.TypeCollection.get(parentClassName);
          int currentClassSeq = NodeTypeEnum.TypeCollection.get(currentClassname);
          this.edgeVector.put((parentClassSeq - 1) * nodeTypeSize + currentClassSeq, 1);
        } else {
          System.err.println(
              parentClassName + " or " + currentClassname + " not in type collection");
          System.exit(1);
        }
      }

      for (ASTNode child : this.getChildren()) {
        this.edgeVector = ASTNodeUtils.addVector(this.edgeVector, child.getEdgeVector(this));
      }
    }
    return this.edgeVector;

  }

//  public void accept(Visitor visitor) {
//    visitor.visitHash(this);
//    for (ASTNode child : this.getChildren()) {
//      child.accept(visitor);
//    }
//  }

}
