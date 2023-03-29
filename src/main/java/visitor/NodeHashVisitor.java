package visitor;

import java.util.HashMap;
import java.util.Map;
import model.ASTNode;

public class NodeHashVisitor implements Visitor {

  private final Map<String, Integer> hashMap = new HashMap<>();

  public Map<String, Integer> getHashMap() {
    return hashMap;
  }

  public void visitHash(ASTNode astNode) {
    String nodeHash = astNode.getHash();
    hashMap.put(nodeHash, hashMap.getOrDefault(nodeHash, 0) + 1);
  }

}
