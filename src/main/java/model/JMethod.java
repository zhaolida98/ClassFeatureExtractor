package model;

import static utils.Constant.SIGNIFICANCE_THRESHOLD;

import com.google.gson.annotations.Expose;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import utils.Java8Lexer;

public class JMethod {

  private final String[] CC_OPERATOR_LIST = {"if", "while", "for", "do", "switch"};
  @Expose
  private String name = "";
  @Expose
  private String sha256 = "";
  @Expose
  private int cc = -1;
  @Expose
  private int loc = -1;
  @Expose
  private double hv = -1;
  @Expose
  private double complexity = -1;
  @Expose
  private String content = "";
  @Expose
  private boolean isTest = false;
  private int start = -1;
  private int end = -1;

  public JMethod(String content) {
    // remove comments
    content = content.replaceAll("\\/\\/[^\\n]*(?:\\n|$)|\\/\\*(?:[^*]|\\*(?!\\/))*\\*\\/", "");
    // remove annotations
    content = content.replaceAll("@\\w+\\s*(?:\\([^()]*\\),*)?", "");
    // normalize content, only single space
    content = content.replaceAll("\\s+", " ");
    this.content = content;
    getFuncNameParam(this.content);
    calculateSha256(this.content);
    calculateCyclomaticComplexity(this.content);
    calculateHalsteadVolume(this.content);
    calculateLineOfCode(this.content);
    isTestFunction(this.content);
    calculateComplexity();
  }

  public JMethod(String content, String name) {
    this.name = name;
    // remove comments, already removed when building node
//    content = content.replaceAll("\\/\\/[^\\n]*(?:\\n|$)|\\/\\*(?:[^*]|\\*(?!\\/))*\\*\\/", "");
    // remove annotations
    content = content.replaceAll("@\\w+\\s*(?:\\([^()]*\\),*)?", "");
    // normalize content, only single space
    content = content.replaceAll("\\s+", " ");
    this.content = content;
    calculateSha256(this.content);
    calculateCyclomaticComplexity(this.content);
    calculateHalsteadVolume(this.content);
    calculateLineOfCode(this.content);
    isTestFunction(this.content);
    calculateComplexity();
  }

  private String getMethodBodyContent(String content) {
    content = content.substring(content.indexOf("{"), content.lastIndexOf("}"));
    content = content.replace(" ", "");
    content = content.replace(System.lineSeparator(), " ");
    content = content.replace("\t", " ");
    return content;
  }

  private void calculateSha256(String content) {
    content = getMethodBodyContent(content);
    this.sha256 = DigestUtils.sha256Hex(content);
  }

  private void calculateCyclomaticComplexity(String content) {
    int cyclomaticComplexity = 1;
    for (String operator : CC_OPERATOR_LIST) {
      cyclomaticComplexity += StringUtils.countMatches(content, operator);
    }
    this.cc = cyclomaticComplexity;
  }

  private void calculateHalsteadVolume(String content) {
    try {
      InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
      Java8Lexer lexer = new Java8Lexer(CharStreams.fromStream(stream, StandardCharsets.UTF_8));
      CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
      commonTokenStream.fill();
      ArrayList<String> operands = new ArrayList<>();
      ArrayList<String> operators = new ArrayList<>();
      HashSet<String> uniqueOperands = new HashSet<>();
      HashSet<String> uniqueOperators = new HashSet<>();

      for (Token token : commonTokenStream.getTokens()) {
        if (token.getType() == Java8Lexer.Identifier ||
            token.getType() == Java8Lexer.CONST ||
            token.getType() == Java8Lexer.DOUBLE ||
            token.getType() == Java8Lexer.FLOAT ||
            token.getType() == Java8Lexer.StringLiteral) {
          operands.add(token.getText());
          uniqueOperands.add(token.getText());
        } else {
          operators.add(token.getText());
          uniqueOperators.add(token.getText());
        }
      }

      int N1 = operators.size();
      int N2 = operands.size();
      int n1 = uniqueOperators.size();
      int n2 = uniqueOperands.size();

      double halsteadVolumn = 0;
      double n = n1 + n2;
      double N = N1 + N2;
      if (n > 0) {
        halsteadVolumn = N * Math.log(n)/Math.log(2);
      } else {
        halsteadVolumn = 1;
      }
      this.hv = halsteadVolumn;
    } catch (Exception e) {
      this.hv = 0;
    }
  }

  private void calculateLineOfCode(String content) {
    int start = content.indexOf('{');
    int end = content.lastIndexOf('}');
    if (start == -1 || end == -1) {
      this.loc = -1;
      return;
    }
    content = content.substring(start, end);
    this.loc = (int) content.chars().filter(ch -> ch == ';' || ch == '{' || ch == '}').count();
  }

  private void calculateComplexity() {
    this.complexity = 5.2 * Math.log(this.hv) + 0.23 * this.cc + 16.2 * Math.log(this.loc);
  }

  private void getFuncNameParam(String content) {
    String param = content.substring(content.indexOf('('), content.indexOf(')') + 1);
    String functionHeader = content.substring(0, content.indexOf('(')).trim();
    String[] functionHeaderList = functionHeader.split(" ");
    String funcName = functionHeaderList[functionHeaderList.length - 1].trim();
    String returnType = functionHeaderList[functionHeaderList.length - 2];
    this.name = returnType + " " + funcName + param;
  }

  private void isTestFunction(String content) {
    String functionHeader = content.substring(0, content.indexOf('(')).trim();
    String[] functionHeaderList = functionHeader.split(" ");
    String returnType = "";
    String funcName = "";
    funcName = functionHeaderList[functionHeaderList.length - 1].trim().toLowerCase();
    if (functionHeaderList.length > 1) {
      returnType = functionHeaderList[functionHeaderList.length - 2];
    }

    this.isTest = (funcName.startsWith("test") ||
        funcName.endsWith("test")) && returnType.equals("void");
  }

  public boolean isSignificant() {
    return this.getComplexity() > SIGNIFICANCE_THRESHOLD;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getCc() {
    return cc;
  }

  public void setCc(int cc) {
    this.cc = cc;
  }

  public int getLoc() {
    return loc;
  }

  public void setLoc(int loc) {
    this.loc = loc;
  }

  public double getHv() {
    return hv;
  }

  public void setHv(double hv) {
    this.hv = hv;
  }

  public double getComplexity() {
    return complexity;
  }

  public void setComplexity(double complexity) {
    this.complexity = complexity;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
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

  public String getHash() {
    return getPosition() + ":" + getName();
  }

  public String getSha256() {
    return sha256;
  }

  public void setSha256(String sha256) {
    this.sha256 = sha256;
  }

  public boolean isTest() {
    return isTest;
  }

  public void setTest(boolean test) {
    isTest = test;
  }

  public String toString() {
    return getName();
  }

}
