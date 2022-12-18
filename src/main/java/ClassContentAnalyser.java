import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;
import model.JClass;
import model.JMethod;
import model.ListenerReturn;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import utils.Java8Lexer;
import utils.Java8Parser;


public class ClassContentAnalyser {

  List<JClass> classList = new ArrayList<>();

  public List<JClass> resolveClassList(String inputFile) {

    try {
      InputStream is = System.in;
      if (inputFile != null) {
        is = new FileInputStream(inputFile);
      }

      CharStream input = CharStreams.fromStream(is);
      Java8Lexer lexer = new Java8Lexer(input);
      lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);
      CommonTokenStream tokens = new CommonTokenStream(lexer);
      Java8Parser parser = new Java8Parser(tokens);
      parser.removeErrorListeners();
      ParserRuleContext tree = parser.compilationUnit(); // parse

      ParseTreeWalker walker = new ParseTreeWalker(); // create standard walker
      ClassContentListener extractor = new ClassContentListener(parser);
      walker.walk(extractor, tree); // initiate walk of tree with listener

      // resolve class-method relations
      processExtractor(extractor);

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return classList;
  }

  public void processExtractor(ClassContentListener extractor) {
    Map<String, String> classMap = extractor.getClassMap();
    Map<String, String> methodMap = extractor.getMethodMap();
    
    Map<String, JClass> className2ObjMap = new HashMap<>();
    Map<String, Integer> classIndexMap = new HashMap<>();

    Map<String, JMethod> methodName2ObjMap = new HashMap<>();
    Map<String, Integer> methodIndexMap = new HashMap<>();

    // build all class object and build className2ObjMap and classIndexMap
    for (String classIndex : classMap.keySet()) {
      String classContent = classMap.get(classIndex);
      JClass jc = JClass.classBuilder(classContent);
      String[] start_end = classIndex.split("-");
      jc.setStart(Integer.parseInt(start_end[0]));
      jc.setEnd(Integer.parseInt(start_end[1]));
      className2ObjMap.put(jc.getName(), jc);

      classIndexMap.put(jc.getName() + "-start", jc.getStart());
      classIndexMap.put(jc.getName() + "-end", jc.getEnd());
    }

    // resolve class relationship
    List<String> sortedClassName = classIndexMap
        .entrySet()
        .stream()
        .sorted(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());
    Stack<String> classStack = new Stack<>();

//    classStack.push(sortedClassName.get(0).replace("-start", "").replace("-end", ""));
    String top = "";
    for (int i = 0; i < sortedClassName.size(); i ++) {
      String className = sortedClassName.get(i);
      className = className.replace("-start", "").replace("-end", "");
      if (className.equals(top)) {
        classStack.pop();
      } else{
        if (top.isEmpty()) {
          classStack.push(className);
        } else {
          JClass topjc = className2ObjMap.get(top);
          topjc.getInnerClass().add(className);
          JClass currentjc = className2ObjMap.get(className);
          currentjc.setInnerClass(true);
          classStack.push(className);
        }
      }
      if (!classStack.isEmpty()) {
        top = classStack.peek();
      } else {
        top = "";
      }
    }

    // build all method object and build methodName2ObjMap and methodIndexMap
    for (String methodIndex : methodMap.keySet()) {
      String methodContent = methodMap.get(methodIndex);
      JMethod jm = JMethod.methodBuilder(methodContent);
      String[] start_end = methodIndex.split("-");
      jm.setStart(Integer.parseInt(start_end[0]));
      jm.setEnd(Integer.parseInt(start_end[1]));
      methodName2ObjMap.put(jm.getHash(), jm);

      methodIndexMap.put(jm.getHash(), jm.getStart());
    }

    // resolve method-class relationship
    List<String> sortedMethodName = methodIndexMap
        .entrySet()
        .stream()
        .sorted(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());
    for (int i = sortedMethodName.size() - 1; i >= 0; i--) {
      JMethod jm = methodName2ObjMap.get(sortedMethodName.get(i));
      String jmp = jm.getPosition();
      for (int j = sortedClassName.size() - 1; j >= 0; j--) {
        String keyName = sortedClassName.get(j);
        // only take start position as ref
        if (keyName.endsWith("-end")) {
          continue;
        }
        keyName = keyName.substring(0, keyName.length()-6);
        JClass jc = className2ObjMap.get(keyName);
        String jcp = jc.getPosition();
        if (ListenerReturn.belongsTo(jmp, jcp)) {
          jc.getMethods().put(jm.getName(), jm);
          break;
        }
      }
    }
    classList = new ArrayList<>(className2ObjMap.values());
  }
}