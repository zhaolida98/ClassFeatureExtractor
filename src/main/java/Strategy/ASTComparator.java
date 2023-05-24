package Strategy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import model.ASTClassNode;

public class ASTComparator {

  Map<String, Long> nodeHashMap = new HashMap<>();

  public ASTComparator(ASTClassNode n1) {
    n1.getHash();
    this.nodeHashMap = n1.getHashCache();
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
    long curCnt = countMapItems(this.nodeHashMap);
    long othCnt = countMapItems(astComparator.getNodeHashMap());
    long commonCnt = countMapItems(commonNode, this.nodeHashMap, astComparator.getNodeHashMap());

    return 1.0f * commonCnt / (curCnt + othCnt - commonCnt);
  }

  private int countMapItems(Map<String, Long> map) {
    int count = 0;
    for (Entry<String, Long> entry : map.entrySet()) {
      count += entry.getValue();
    }
    return count;
  }

  private int countMapItems(Set<String> index, Map<String, Long> map1,
      Map<String, Long> map2) {
    int count = 0;
    for (String i : index) {
      count += Math.min(map1.get(i), map2.get(i));
    }
    return count;
  }

  public Map<String, Long> getNodeHashMap() {
    return nodeHashMap;
  }
}
