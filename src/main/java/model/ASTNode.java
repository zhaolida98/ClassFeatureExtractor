package model;

import static org.apache.commons.codec.digest.MessageDigestAlgorithms.MD5;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.digest.DigestUtils;
import utils.ASTNodeUtils;
import utils.HashUtils;
import visitor.Visitor;

public class ASTNode implements Cloneable {

  private Node node;
  private ASTNode parent;
  private final List<ASTNode> children = new ArrayList<>();
  private ASTNode funcRef;

  private String name;

  private String hash = null;

  private Map<String, Long> hashCache = new HashMap<>();

  public ASTNode(Node node, ASTNode parentNode) {
    node = node.removeComment();
    this.node = node;
    this.parent = parentNode;
    if (node != null) {
      for (Node n : node.getChildNodes()) {
        ASTNode astNode = null;
        if (n instanceof ImportDeclaration || n instanceof PackageDeclaration
            || n instanceof AnnotationExpr || n instanceof Comment) {
          continue;
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

  public Map<String, Long> getHashCache() {
    return hashCache;
  }

  public String getHash() {
    if (this.hash == null) {
      String origin = this.node.getClass().getName();
      String current = new DigestUtils(MD5).digestAsHex(origin);
      for (ASTNode child : this.getChildren()) {
        current = HashUtils.addHash(current, child.getHash());
        this.hashCache = ASTNodeUtils.mergeMap(this.getHashCache(), child.getHashCache());
      }
      this.hash = current;
      this.hashCache.put(current, Math.min(Integer.MAX_VALUE, this.hashCache.getOrDefault(current, 0L) + 1));
    }
    return this.hash;

  }

  public void accept(Visitor visitor) {
    visitor.visitHash(this);
    for (ASTNode child : this.getChildren()) {
      child.accept(visitor);
    }
  }

}
