package Strategy;

import static org.apache.commons.codec.digest.MessageDigestAlgorithms.MD5;
import static org.apache.commons.codec.digest.MessageDigestAlgorithms.SHA_256;
import static utils.ASTNodeUtils.getFileAST;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import model.ASTMethodNode;
import model.ASTNode;
import model.JMethod;
import model.ParamManager;
import org.apache.commons.codec.digest.DigestUtils;
import utils.ASTNodeUtils;
import utils.HashUtils;

public class FunctionHashStrategy implements Strategy{

  ParamManager paramManager = ParamManager.getInstance();

  @Override
  public JsonObject execute(File path) {
    JsonObject jsonObject = new JsonObject();
    ASTNode astNode = getFileAST(path);
    if (astNode == null) {
      return null;
    }
    List<ASTNode> methodList = findMethodDec(astNode);
    JsonArray jsonArray = new JsonArray();
    for (int i = 0; i < methodList.size(); i++) {
      ASTNode methodNode = methodList.get(i);
      JMethod jMethod = ((ASTMethodNode) methodNode).getjMethod();
      String methodSkeleton = walkMethod(methodNode, 0);
      if (paramManager.isPrintTree()) {
        System.out.println(methodSkeleton);
      }
      String methodContent = methodNode.getNode().removeComment().toString();
      String methodHash = new DigestUtils(SHA_256).digestAsHex(methodSkeleton);
      int startLine = methodNode.getNode().getRange().get().begin.line;
      int endLine = methodNode.getNode().getRange().get().end.line;
      int complexity = (int) jMethod.getComplexity();
      String inLineFeature = String.format("%s %d %d %d", methodHash, startLine, endLine, complexity);
      jsonArray.add(inLineFeature);
    }
    jsonObject.add(path.getAbsolutePath(), jsonArray);


    return jsonObject;
  }

  private List<ASTNode> findMethodDec(ASTNode astNode) {
    List<ASTNode> methodList = new ArrayList<>();
    Node node = astNode.getNode();
    if (node instanceof MethodDeclaration) {
      methodList.add(astNode);
    } else{
      for (ASTNode an : astNode.getChildren()) {
        methodList.addAll(findMethodDec(an));
      }
    }
    return methodList;
  }

  private String walkMethod(ASTNode astNode, int indent) {
    String[] tmpClassName = astNode.getNode().getClass().getName().split("\\.");
    String className = tmpClassName[tmpClassName.length - 1];
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < indent; i++) {
      sb.append("-");
    }
    if (astNode.getNode() instanceof PrimitiveType) {
      sb.append(className).append("->").append(astNode.getNode()).append(System.lineSeparator());
    } else {
      sb.append(className).append(System.lineSeparator());
    }

    for (ASTNode an : astNode.getChildren()) {
      sb.append(walkMethod(an, indent + 1));
    }
    return sb.toString();
  }

}
