package Strategy;

import static model.ASTMethodNode.getTokenSet;
import static org.apache.commons.codec.digest.MessageDigestAlgorithms.SHA_256;
import static utils.ASTNodeUtils.getFileAST;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.PrimitiveType;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import model.ASTMethodNode;
import model.ASTNode;
import model.JMethod;
import model.ParamManager;
import org.apache.commons.codec.digest.DigestUtils;

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
    HashSet<String> nameList = new HashSet<>();
    for (ASTNode m : methodList) {
      String methodName = m.getName().split("; ")[1];
      nameList.add(methodName);
    }
    for (int i = 0; i < methodList.size(); i++) {
      ASTNode methodNode = methodList.get(i);
      String methodName = methodNode.getName().split("; ")[1];
      JMethod jMethod = ((ASTMethodNode) methodNode).getjMethod();
      String methodSkeleton = walkMethod(methodNode, 0);
      if (paramManager.isPrintTree()) {
        System.out.println(methodSkeleton);
      }
      String methodContent = methodNode.getNode().removeComment().toString();
      HashSet<String> tokenSet = getTokenSet(methodNode.getNode());
      String methodHash = new DigestUtils(SHA_256).digestAsHex(methodSkeleton);
      String parentClass = ((ASTMethodNode) methodNode).getTopClass().getName();
      int startLine = methodNode.getNode().getRange().get().begin.line;
      int endLine = methodNode.getNode().getRange().get().end.line;
      int complexity = (int) jMethod.getComplexity();
      boolean isTest = methodName.toLowerCase().startsWith("test") || methodName.toLowerCase().endsWith("test");
      JsonObject funcObj = new JsonObject();
      funcObj.add("range", new JsonPrimitive(String.format("%s %s", startLine, endLine)));
      funcObj.add("name", new JsonPrimitive(methodName));
      funcObj.add("hash", new JsonPrimitive(methodHash));
      funcObj.add("complexity", new JsonPrimitive(complexity));
      funcObj.add("class", new JsonPrimitive(parentClass));
      funcObj.add("isTest", new JsonPrimitive(isTest));
      JsonArray calleeList = new JsonArray();
      for (String m_name : nameList) {
        if (!m_name.equals(methodName) &&
            tokenSet.contains(m_name)) {
          calleeList.add(m_name);
        }
      }
      funcObj.add("callee", calleeList);
      jsonArray.add(funcObj);
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
