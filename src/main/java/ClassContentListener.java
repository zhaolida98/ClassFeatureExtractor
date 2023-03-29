import java.util.HashMap;
import model.ListenerReturn;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import utils.Java8Parser;
import utils.Java8ParserBaseListener;

/***
 * Excerpted from "The Definitive ANTLR 4 Reference",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material,
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose.
 * Visit http://www.pragmaticprogrammer.com/titles/tpantlr2 for more book information.
 ***/
public class ClassContentListener extends Java8ParserBaseListener {

  HashMap<String, String> classMap = new HashMap<>();
  HashMap<String, String> methodMap = new HashMap<>();
  HashMap<String, String> memberVarMap = new HashMap<>();
  HashMap<String, String> classMemberMap = new HashMap<>();
  HashMap<String, String> interfaceMap = new HashMap<>();


  public ListenerReturn getCtx(ParserRuleContext ctx) {
    Token start = ctx.getStart();
    Token stop = ctx.getStop();

    String key;
    String value = stop.getInputStream()
        .getText(new Interval(start.getStartIndex(), stop.getStopIndex()));

    /* NOTE! This critical step glooms on the line number of the function */
    key = String.format("%s-%s", start.getStartIndex(), stop.getStartIndex());
    return new ListenerReturn(key, value);
  }

  public void enterClassDeclaration(Java8Parser.ClassDeclarationContext ctx) {
    ListenerReturn listenerReturn = getCtx(ctx);
    classMap.put(listenerReturn.getKey(), listenerReturn.getValue());
//    System.out.println("enterClassDeclaration");
//    System.out.println(listenerReturn.getKey() + ":" + listenerReturn.getValue());
  }

  public void enterMethodDeclaration(Java8Parser.MethodDeclarationContext ctx) {
    ListenerReturn listenerReturn = getCtx(ctx);
    methodMap.put(listenerReturn.getKey(), listenerReturn.getValue());
//    System.out.println("enterMethodDeclaration");
//    System.out.println(listenerReturn.getKey() + ":" + listenerReturn.getValue());
  }

  public void enterInterfaceDeclaration(Java8Parser.InterfaceDeclarationContext ctx) {
    ListenerReturn listenerReturn = getCtx(ctx);
    interfaceMap.put(listenerReturn.getKey(), listenerReturn.getValue());
//    System.out.println("InterfaceDeclaration");
//    System.out.println(listenerReturn.getKey() + ":" + listenerReturn.getValue());
  }

  public void enterClassMemberDeclaration(Java8Parser.ClassMemberDeclarationContext ctx) {
    ListenerReturn listenerReturn = getCtx(ctx);
    classMemberMap.put(listenerReturn.getKey(), listenerReturn.getValue());
//    System.out.println("ClassMemberDeclaration");
//    System.out.println(listenerReturn.getKey() + ":" + listenerReturn.getValue());
  }

  public void enterVariableDeclaratorId(Java8Parser.VariableDeclaratorIdContext ctx) {
    ListenerReturn listenerReturn = getCtx(ctx);
    memberVarMap.put(listenerReturn.getKey(), listenerReturn.getValue());
//    System.out.println("VariableDeclaratorId");
//    System.out.println(listenerReturn.getKey() + ":" + listenerReturn.getValue());
  }

  public HashMap<String, String> getClassMap() {
    return classMap;
  }

  public HashMap<String, String> getMethodMap() {
    return methodMap;
  }

  public HashMap<String, String> getMemberVarMap() {
    return memberVarMap;
  }

  public HashMap<String, String> getClassMemberMap() {
    return classMemberMap;
  }

  public HashMap<String, String> getInterfaceMap() {
    return interfaceMap;
  }
}
