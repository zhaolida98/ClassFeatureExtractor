package utils;

import java.util.Comparator;
import model.ASTMethodNode;

public class MethodComparator implements Comparator<ASTMethodNode> {

  /**
   * sort the result according to out degree, in degree and MI
   *
   * @param a1 the first object to be compared.
   * @param a2 the second object to be compared.
   * @return
   */
  public int compare(ASTMethodNode a1, ASTMethodNode a2) {
    if (a1.getOutdegreeMeth().size() != a2.getOutdegreeMeth().size()) {
      return Integer.compare(a1.getOutdegreeMeth().size(), a2.getOutdegreeMeth().size());
    } else {
      if (a1.getIndegreeMeth().size() != a2.getIndegreeMeth().size()) {
        return -Integer.compare(a1.getIndegreeMeth().size(), a2.getIndegreeMeth().size());
      } else {
        return -Double.compare(a1.getjMethod().getComplexity(), a2.getjMethod().getComplexity());
      }
    }

  }
}
