package Strategy;

import static utils.ASTNodeUtils.getFileAST;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.gson.JsonObject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import model.ASTNode;
import org.apache.commons.codec.digest.DigestUtils;

public class ClassHashStrategy implements Strategy{

  @Override
  public JsonObject execute(File path) {
    JsonObject jsonObject = new JsonObject();
    ASTNode astNode = getFileAST(path);
    if (astNode == null) {
      return null;
    }
    List<ASTNode> classList = findClass(astNode);
    for (ASTNode cn : classList) {
      String name = cn.getName();
      String id = name + "@" + path.getAbsolutePath();
      String content = cn.getNode().toString();
      jsonObject.addProperty(id, getHash(content));
    }
    return jsonObject;
  }

  private String getHash(String content) {
    content = content.replaceAll(":\\/\\/", "");
    content = content.replaceAll("\\/\\/[^\\n]*(?:\\n|$)|\\/\\*(?:[^*]|\\*(?!\\/))*\\*\\/", "");
    // remove annotations
    content = content.replaceAll("@\\w+\\s*(?:\\([^()]*\\),*)?", "");
    // normalize content, only single space
    content = content.replaceAll("\\s+", " ");
    String contentHash = DigestUtils.sha256Hex(content);
    return contentHash;
  }

  private List<ASTNode> findClass(ASTNode astNode) {
    List<ASTNode> classList = new ArrayList<>();
    Node node = astNode.getNode();
    if (node instanceof ClassOrInterfaceDeclaration || node instanceof EnumDeclaration) {
      classList.add(astNode);
    }
    for (ASTNode an : astNode.getChildren()) {
      classList.addAll(findClass(an));
    }
    return classList;
  }
}
