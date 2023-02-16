package model;

public class ListenerReturn {

  String key;
  String value;

  public ListenerReturn(String key, String value) {
    this.key = key;
    this.value = value;
  }

  /***
   * return true if key 1 is included or equal to key2
   * @param key1 key range <startLine>:<charPosition>, <endLine>:<charPosition>
   * @param key2 key range <startLine>:<charPosition>, <endLine>:<charPosition>
   */
  public static boolean belongsTo(String key1, String key2) {
    String[] t1 = key1.split("-");
    String[] t2 = key2.split("-");
    int s1 = Integer.parseInt(t1[0]);
    int e1 = Integer.parseInt(t1[1]);
    int s2 = Integer.parseInt(t2[0]);
    int e2 = Integer.parseInt(t2[1]);
    return s1 >= s2 && e1 <= e2;
  }

  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }
}
