package utils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import model.ASTNode;

public class ASTNodeUtils {

  public static void walkNode(ASTNode astNode, int indent) {
    Node node = astNode.getNode();
    String[] tmpClassName = node.getClass().getName().split("\\.");
    String className = tmpClassName[tmpClassName.length - 1];
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < indent; i++) {
      sb.append("  ");
    }
    if (node instanceof ClassOrInterfaceDeclaration) {
      sb.append(className).append(" -> ").append(astNode.getName());
    } else if (
        node instanceof SimpleName ||
            node instanceof LiteralExpr ||
            node instanceof Modifier ||
            node instanceof PrimitiveType ||
            node instanceof MethodCallExpr ||
            node instanceof ClassOrInterfaceType ||
            node instanceof FieldDeclaration) {
      sb.append(className).append(" -> ").append(node);
    } else {
      sb.append(className);
    }
    System.out.println(sb);
    for (ASTNode an : astNode.getChildren()) {
      walkNode(an, indent + 1);
    }
  }

  public static Map<String, List<ASTNode>> locateMethodCall(ASTNode astNode) {
    // string: class::func(arg1,...)
    // Node: address of MethodCallExpr
    Map<String, List<ASTNode>> methodCallMap = new HashMap<>();
    recursivelyLocateMethodCall(methodCallMap, astNode);
    return methodCallMap;
  }

  public static Map<String, Long> mergeMap(Map<String, Long> a,
      Map<String, Long> b) {
    Map<String, Long> res = new HashMap<>(a);
    for (String k : b.keySet()) {
      if (!res.containsKey(k)) {
        res.put(k, 0L);
      }
      res.put(k, Math.min(res.get(k) + b.get(k), Integer.MAX_VALUE));
    }
    return res;
  }


  private static void recursivelyLocateMethodCall(Map<String, List<ASTNode>> methodCallMap,
      ASTNode astNode) {
    if (astNode.getNode() instanceof MethodCallExpr) {
      if (!methodCallMap.containsKey(astNode.getNode().toString())) {
        methodCallMap.put(astNode.getNode().toString(), new ArrayList<>());
      }
      methodCallMap.get(astNode.getNode().toString()).add(astNode);
    }
    List<ASTNode> childNodes = astNode.getChildren();
    for (ASTNode a : childNodes) {
      recursivelyLocateMethodCall(methodCallMap, a);
    }
  }

  /**
   * We suppose if the function name and arg numbers are the same, they are the same. Ignore the
   * overload of functions.
   *
   * @param callPattern
   * @param defPattern
   * @return
   */
  public static String methodCallMatch(String callPattern, Set<String> defPattern) {
    // call pattern name(arg1, ...)
    // def pattern returnType name(type arg1, ...)
    int cpArgStartIdx = callPattern.indexOf('(');
    String cpName = callPattern.substring(0, cpArgStartIdx);
    String cpArgs = callPattern.substring(cpArgStartIdx + 1, callPattern.length() - 1);
    String[] cpArgList = cpArgs.split(",");
    for (String dp : defPattern) {
      String[] dpSplit = dp.split(";");
      assert dpSplit.length == 3;
      String dpName = dpSplit[1].trim();
      String dpArgs = dpSplit[2].trim();
      String[] dpArgList = dpArgs.split(",");
      if (cpName.equals(dpName) && cpArgList.length == dpArgList.length) {
        return dp;
      }
    }
    return null;
  }

  public static ASTNode getMethodBody(ASTNode methodNode) {
    for (ASTNode astNode : methodNode.getChildren()) {
      Node node = astNode.getNode();
      if (node instanceof BlockStmt) {
        return astNode;
      }
    }
    System.err.println("Failed to resolve method body for " + methodNode);
    return null;
  }

  public static Node getAST(String content) {
    // add dummy class header for parsing
    // TODO remove comment
    StringBuffer sb = new StringBuffer();
    sb.append("class __DummyClass__ {");
    sb.append(content);
    sb.append("}");
    CompilationUnit cu = null;
    try {
      cu = StaticJavaParser.parse(sb.toString());
    } catch (Exception e) {
      return null;
    }
    Node o = cu.getTypes().get(0).getMember(0);
    if (o instanceof MethodDeclaration) {
      return o;
    }
    return null;
  }
  // further resolve node into other AST format

  public static ASTNode getFileAST(File path) {
    CompilationUnit cu = null;
    ASTNode fullAst = null;
    try {
      String content = FileUtils.readFile(path.getAbsolutePath(), StandardCharsets.UTF_8);

      if (content.matches("[Gg]enerated [Bb]y")) {
        return null;
      }
      cu = StaticJavaParser.parse(content);
    } catch (Exception e) {
      System.err.println("Error on parsing file, could be grammar error： " + path.getAbsolutePath());
      return null;
    }
    fullAst  = new ASTNode(cu, null);
    return fullAst;
  }
}
