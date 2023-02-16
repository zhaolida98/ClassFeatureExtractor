import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import model.ASTClassNode;
import visitor.NodeHashVisitor;

public class ASTComparator {

  Map<String, Integer> nodeHashMap = new HashMap<>();

  public ASTComparator(ASTClassNode n1) {
    NodeHashVisitor v = new NodeHashVisitor();
    n1.accept(v);
    this.nodeHashMap = v.getHashMap();
  }

  /**
   * using jaccard similarity
   *
   * @param astComparator
   * @return
   */
  public double compareTo(ASTComparator astComparator) {
    if (astComparator.getNodeHashMap().isEmpty()) {
      return -1;
    }
    Set<String> commonNode = new HashSet<>(this.nodeHashMap.keySet());
    commonNode.retainAll(astComparator.getNodeHashMap().keySet());
    int curCnt = countMapItems(this.nodeHashMap);
    int othCnt = countMapItems(astComparator.getNodeHashMap());
    int commonCnt = countMapItems(commonNode, this.nodeHashMap, astComparator.getNodeHashMap());

    return 1.0f * commonCnt / (curCnt + othCnt - commonCnt);
  }

  private int countMapItems(Map<String, Integer> map) {
    int count = 0;
    for (Entry<String, Integer> entry : map.entrySet()) {
      count += entry.getValue();
    }
    return count;
  }

  private int countMapItems(Set<String> index, Map<String, Integer> map1,
      Map<String, Integer> map2) {
    int count = 0;
    for (String i : index) {
      count += Math.min(map1.get(i), map2.get(i));
    }
    return count;
  }

  public Map<String, Integer> getNodeHashMap() {
    return nodeHashMap;
  }
}
