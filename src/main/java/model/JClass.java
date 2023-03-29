package model;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.SimpleName;
import com.google.gson.annotations.Expose;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JClass {

  int start = -1;
  int end = -1;
  @Expose
  private String name = "";
  @Expose
  private Map<String, JMethod> methods;
  private Map<String, String> constructors;
  @Expose
  private boolean isInnerClass = false;
  @Expose
  private List<String> innerClass = new ArrayList<>();
  @Expose
  private String extend = "";
  @Expose
  private List<String> implement;
  private Map<String, String> classParameter;

  public JClass() {
    init();
  }

  public JClass(String name) {
    this.name = name;
  }

  public JClass(Node node) {
    init();
    for (Node child : node.getChildNodes()) {
      if (child instanceof SimpleName) {
        this.name = child.toString();
      }
    }
  }


  public static JClass classBuilder(String content) {
    JClass cck = new JClass();
    String classDefHeader = content.substring(0, content.indexOf('{'));
    Pattern classNamePattern = Pattern.compile("(?:class|enum) (\\S*)", Pattern.CASE_INSENSITIVE);
    Matcher classNameMatcher = classNamePattern.matcher(classDefHeader);
    String className = "";
    String extend = "";
    String[] implementList = null;
    if (classNameMatcher.find()) {
      className = classNameMatcher.group(1);
    }
    Pattern extendPattern = Pattern.compile("extends (\\S*)", Pattern.CASE_INSENSITIVE);
    Matcher extendMatcher = extendPattern.matcher(classDefHeader);
    if (extendMatcher.find()) {
      extend = extendMatcher.group(1);
    }
    Pattern implementPattern = Pattern.compile("implements ((\\S*[\\s,]?)*)",
        Pattern.CASE_INSENSITIVE);
    Matcher implementMatcher = implementPattern.matcher(classDefHeader);
    if (implementMatcher.find()) {
      String implement = implementMatcher.group(1);
      implementList = implement.split("(\\s|,)+");
    }
    cck.setName(className);
    cck.setExtend(extend);
    if (implementList != null) {
      cck.setImplement(Arrays.asList(implementList));
    }
    return cck;
  }

  private void init() {
    methods = new HashMap<>();
    constructors = new HashMap<>();
    innerClass = new ArrayList<>();
    implement = new ArrayList<>();
    classParameter = new HashMap<>();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(name).append(System.lineSeparator());
    sb.append("  extends: ").append(extend).append(System.lineSeparator());
    sb.append("  impliments: ");
    for (String imp :
        implement) {
      sb.append(imp).append(" ");
    }
    sb.append(System.lineSeparator());
    sb.append("  methods:").append(System.lineSeparator());
    for (JMethod method : methods.values()) {
      sb.append("    ").append(method.toString()).append(System.lineSeparator());
    }
    sb.append("  inner class:").append(System.lineSeparator());
    for (String classs : innerClass) {
      sb.append("    ").append(classs).append(System.lineSeparator());
    }
    return sb.toString();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<String, JMethod> getMethods() {
    return methods;
  }

  public void setMethods(Map<String, JMethod> methods) {
    this.methods = methods;
  }

  public Map<String, String> getConstructors() {
    return constructors;
  }

  public void setConstructors(Map<String, String> constructors) {
    this.constructors = constructors;
  }

  public boolean isInnerClass() {
    return isInnerClass;
  }

  public List<String> getInnerClass() {
    return innerClass;
  }

  public void setInnerClass(boolean innerClass) {
    isInnerClass = innerClass;
  }

  public void setInnerClass(List<String> innerClass) {
    this.innerClass = innerClass;
  }

  public String getExtend() {
    return extend;
  }

  public void setExtend(String extend) {
    this.extend = extend;
  }

  public List<String> getImplement() {
    return implement;
  }

  public void setImplement(List<String> implement) {
    this.implement = implement;
  }

  public Map<String, String> getClassParameter() {
    return classParameter;
  }

  public void setClassParameter(Map<String, String> classParameter) {
    this.classParameter = classParameter;
  }

  public int getStart() {
    return start;
  }

  public void setStart(int start) {
    this.start = start;
  }

  public int getEnd() {
    return end;
  }

  public void setEnd(int end) {
    this.end = end;
  }

  public String getPosition() {
    return this.start + "-" + this.end;
  }
}
