package model;

import java.util.HashMap;
import java.util.Map.Entry;

public class NodeTypeEnum {

  public static final HashMap<String, Integer> TypeCollection = new HashMap<String, Integer>() {
    {
      put("AnnotationDeclaration", 1);
      put("AnnotationMemberDeclaration", 2);
      put("ArrayAccessExpr", 3);
      put("ArrayCreationExpr", 4);
      put("ArrayCreationLevel", 5);
      put("ArrayInitializerExpr", 6);
      put("ArrayType", 7);
      put("AssertStmt", 8);
      put("AssignExpr", 9);
      put("BinaryExpr", 10);
      put("BlockStmt", 11);
      put("BooleanLiteralExpr", 12);
      put("BreakStmt", 13);
      put("CastExpr", 14);
      put("CatchClause", 15);
      put("CharLiteralExpr", 16);
      put("ClassExpr", 17);
      put("ClassOrInterfaceDeclaration", 18);
      put("ClassOrInterfaceType", 19);
      put("CompactConstructorDeclaration", 20);
      put("CompilationUnit", 21);
      put("ConditionalExpr", 22);
      put("ConstructorDeclaration", 23);
      put("ContinueStmt", 24);
      put("ConvertibleToUsage", 25);
      put("DoStmt", 26);
      put("DoubleLiteralExpr", 27);
      put("EmptyStmt", 28);
      put("EnclosedExpr", 29);
      put("EnumConstantDeclaration", 30);
      put("EnumDeclaration", 31);
      put("ExplicitConstructorInvocationStmt", 32);
      put("ExpressionStmt", 33);
      put("ExternalMethodNode", 34);
      put("FieldAccessExpr", 35);
      put("FieldDeclaration", 36);
      put("ForEachStmt", 37);
      put("ForStmt", 38);
      put("IfStmt", 39);
      put("InitializerDeclaration", 40);
      put("InsignificantMethodNode", 41);
      put("InstanceOfExpr", 42);
      put("IntegerLiteralExpr", 43);
      put("IntersectionType", 44);
      put("LabeledStmt", 45);
      put("LambdaExpr", 46);
      put("LocalClassDeclarationStmt", 47);
      put("LocalRecordDeclarationStmt", 48);
      put("LongLiteralExpr", 49);
      put("MarkerAnnotationExpr", 50);
      put("MemberValuePair", 51);
      put("MethodCallExpr", 52);
      put("MethodDeclaration", 53);
      put("MethodReferenceExpr", 54);
      put("ModuleDeclaration", 55);
      put("ModuleExportsDirective", 56);
      put("ModuleOpensDirective", 57);
      put("ModuleRequiresDirective", 58);
      put("ModuleUsesDirective", 59);
      put("Name", 60);
      put("NameExpr", 61);
      put("NormalAnnotationExpr", 62);
      put("NullLiteralExpr", 63);
      put("ObjectCreationExpr", 64);
      put("Parameter", 65);
      put("PatternExpr", 66);
      put("PrimitiveType", 67);
      put("ReceiverParameter", 68);
      put("RecordDeclaration", 69);
      put("ReturnStmt", 70);
      put("SelfLoopNode", 71);
      put("SimpleName", 72);
      put("SingleMemberAnnotationExpr", 73);
      put("StringLiteralExpr", 74);
      put("SuperExpr", 75);
      put("SwitchEntry", 76);
      put("SwitchExpr", 77);
      put("SwitchStmt", 78);
      put("SynchronizedStmt", 79);
      put("TextBlockLiteralExpr", 80);
      put("ThisExpr", 81);
      put("ThrowStmt", 82);
      put("TryStmt", 83);
      put("TypeExpr", 84);
      put("TypeParameter", 85);
      put("UnaryExpr", 86);
      put("UnionType", 87);
      put("UnknownType", 88);
      put("UnparsableStmt", 89);
      put("VariableDeclarationExpr", 90);
      put("VariableDeclarator", 91);
      put("VarType", 92);
      put("VoidType", 93);
      put("WhileStmt", 94);
      put("WildcardType", 95);
      put("YieldStmt", 96);
    }
  };

  public static String index2Type(int a) {
    int parentIdx = a / TypeCollection.size() + 1;
    int childIdx = a - (parentIdx - 1) * TypeCollection.size();
    String parentName = "";
    String childName = "";
    for (Entry<String, Integer> e : TypeCollection.entrySet()) {
      if (e.getValue() == parentIdx) {
        parentName = e.getKey();
      }
      if (e.getValue() == childIdx) {
        childName = e.getKey();
      }
    }
    return parentName + " -> " + childName;
  }


}
