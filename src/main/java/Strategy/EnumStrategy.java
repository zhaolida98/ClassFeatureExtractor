package Strategy;

public enum EnumStrategy {
  CLASS_HASHSET("classHashSet"),
  CLASS_VEC("classVec"),
  FUNCTION_HASH("functionHash"),
  CLASS_HASH("classHash");

  String name;
  EnumStrategy(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
