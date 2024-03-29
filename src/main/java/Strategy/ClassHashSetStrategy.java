package Strategy;

import static utils.ASTNodeUtils.getFileAST;
import static utils.Constant.EXTERNAL_METH_REF;
import static utils.Constant.INSIGNIFICANT_METH_REF;
import static utils.Constant.SELF_LOOP_REF;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import model.ASTClassNode;
import model.ASTMethodNode;
import model.ASTNode;
import model.ParamManager;
import utils.ASTDummyMethodFactory;
import utils.ASTNodeUtils;
import utils.MethodComparator;

public class ClassHashSetStrategy implements Strategy{
  private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
      .create();
  List<ASTClassNode> classList = new ArrayList<>();
  List<ASTNode> resolvedAST = new ArrayList<>();

  ParamManager paramManager = ParamManager.getInstance();

  @Override
  public JsonObject execute(File path) {
    ASTNode astNode = getFileAST(path);
    if (astNode == null) {
      return null;
    }
    resolvedAST = resolveAST(astNode);
    JsonObject jsonObject = toJsonObject(path);
    if (paramManager.isPrintTree()) {
      printTree();
    }
    return jsonObject;
  }

  private JsonObject toJsonObject(File path) {
    // transfer class features to json object
    // {
    //  <className>@<java address>: {
    //   "feature": {<hash>:<cnt>,...},
    //   "complexity": <complexity>
    //  },
    //  ...
    // }
    JsonObject jsonObject = new JsonObject();
    for (ASTNode cn : resolvedAST) {
      ASTComparator astComparator = new ASTComparator((ASTClassNode) cn);
      double classComplexity = ((ASTClassNode) cn).getComplexity();
      JsonElement nodeHash = GSON.toJsonTree(astComparator.getNodeHashMap());
      String addr = cn.getName() + "@" + path.getAbsolutePath();
      JsonObject classFeature = new JsonObject();
      classFeature.add("complexity", new JsonPrimitive(classComplexity));
      classFeature.add("feature", nodeHash);
      jsonObject.add(addr, classFeature);
    }
    return jsonObject;
  }

  private void printTree(){
    // print JClass
    for (ASTNode cn : resolvedAST) {
      ASTNodeUtils.walkNode(cn, 0);
      System.out.println();
    }
  }

  private List<ASTNode> resolveAST(ASTNode astNode) {
    List<ASTNode> resolvedAST = new ArrayList<>();
    List<ASTClassNode> classNodeList = extractClassNode(astNode);
    for (ASTClassNode cn : classNodeList) {
      if (cn.isSignificant()) {
        ASTNode tmpClassNode = methodLinker(cn);
        if (!(cn.getParent().getNode() instanceof ClassOrInterfaceDeclaration)) {
          resolvedAST.add(tmpClassNode);
        }
      }
    }
    // the children of parent nodes has been removed when resolve method link
    // restore the class link after method link resolving
    // only top level class is listed alone.
    for (ASTClassNode cn : classNodeList) {
      if (cn.getParent().getNode() instanceof ClassOrInterfaceDeclaration) {
        if (cn.isSignificant()) {
          cn.getParent().addChildren(cn);
        }
      }
    }
      return resolvedAST;
  }

  public List<ASTClassNode> extractClassNode(ASTNode astNode) {

    for (ASTNode child : astNode.getChildren()) {
      if (child instanceof ASTClassNode) {
        resolveClassASTNode((ASTClassNode) child);
      }
    }
    return this.classList;
  }

  private void resolveClassASTNode(ASTClassNode astClassNode) {
    List<ASTMethodNode> methodList = new ArrayList<>();
    for (ASTNode child : astClassNode.getChildren()) {
      if (child instanceof ASTMethodNode) {
        methodList.add((ASTMethodNode) child);
      }
      if (child instanceof ASTClassNode) {
        resolveClassASTNode((ASTClassNode) child);
      }
    }
    for (ASTMethodNode methodNode : methodList) {
      astClassNode.getMethodNodeMap().put(methodNode.getName(), methodNode);
    }
//    astClassNode.getComplexity();
    this.classList.add(astClassNode);
  }

  public ASTNode methodLinker(ASTClassNode astClassNode) {
    // scan all method and record in/out degree
    Map<String, ASTMethodNode> ASTMethodMap = astClassNode.getMethodNodeMap();
    // write outdegree map
    for (String methodName : ASTMethodMap.keySet()) {
      ASTMethodNode astMethodNode = ASTMethodMap.get(methodName);
      Map<String, List<ASTNode>> methodCallMap = ASTNodeUtils.locateMethodCall(astMethodNode);
      for (String methodCallExpr : methodCallMap.keySet()) {
        String methodDefExpr = ASTNodeUtils.methodCallMatch(methodCallExpr, ASTMethodMap.keySet());
        // if found method in class, record into out degree
        // else, replace with dummy
        List<ASTNode> methodCallNodes = methodCallMap.get(methodCallExpr);

        if (methodDefExpr != null) {
          // resolve self recursive methods
          if (methodDefExpr.equals(methodName)) {
            for (ASTNode mcn : methodCallNodes) {
              ASTNode selfLoopNode = new ASTNode(ASTDummyMethodFactory.create(SELF_LOOP_REF), null);
              mcn.addChildren(selfLoopNode);
            }
          } else {
            if (!astMethodNode.getOutdegreeMeth().containsKey(methodDefExpr)) {
              astMethodNode.getOutdegreeMeth().put(methodDefExpr, new ArrayList<>());
            }
            astMethodNode.getOutdegreeMeth().get(methodDefExpr).addAll(methodCallNodes);
          }
        } else {
          for (ASTNode mcn : methodCallNodes) {
            ASTNode dummyNode = new ASTNode(ASTDummyMethodFactory.create(EXTERNAL_METH_REF), null);
            mcn.addChildren(dummyNode);
          }
        }
      }
    }
    // write indegree map
    for (String curMethod : ASTMethodMap.keySet()) {
      ASTMethodNode tmpASTMethod = ASTMethodMap.get(curMethod);
      Map<String, List<ASTNode>> outDegree = tmpASTMethod.getOutdegreeMeth();
      for (String outMethName : outDegree.keySet()) {
        List<ASTNode> methodCallNodes = outDegree.get(outMethName);
        ASTMethodNode invokedMethod = ASTMethodMap.get(outMethName);
        if (!invokedMethod.getIndegreeMeth().containsKey(curMethod)) {
          invokedMethod.getIndegreeMeth().put(curMethod, new ArrayList<>());
        }
        invokedMethod.getIndegreeMeth().get(curMethod).addAll(methodCallNodes);
      }
    }
    // identify insignificant functions
    // if the node is in significant, replace its method body to a simple node
    // replacing all its out degree map value to simple method body
    // multiple call in the insignificant function is not needed, ASTNode List should be overwritten.
    for (String curMethod : ASTMethodMap.keySet()) {
      ASTMethodNode astMethodNode = ASTMethodMap.get(curMethod);
      boolean isSignificant = astMethodNode.isSignificant();
      if (!isSignificant) {
        Node inSignificantMethodNode = ASTDummyMethodFactory.create(INSIGNIFICANT_METH_REF);
        astMethodNode.setNode(inSignificantMethodNode);
        astMethodNode.getChildren().clear();
        Map<String, List<ASTNode>> outDegree = astMethodNode.getOutdegreeMeth();
        for (String outMethName : outDegree.keySet()) {
          outDegree.put(outMethName, new ArrayList<>());
          outDegree.get(outMethName).add(astMethodNode);
          ASTMethodNode invokedMethod = ASTMethodMap.get(outMethName);
          invokedMethod.getIndegreeMeth().put(curMethod, new ArrayList<>());
          invokedMethod.getIndegreeMeth().get(curMethod).add(astMethodNode);
        }
      }
    }

    // sort method by a heap
    PriorityQueue<ASTMethodNode> priorityQueue = new PriorityQueue<>(new MethodComparator());
    priorityQueue.addAll(ASTMethodMap.values());

    // replace method call to cloned method body
    while (!priorityQueue.isEmpty()) {
      ASTMethodNode top = priorityQueue.poll();
      // solve loop problem, self loop is already solved before
      // to break the loop, remove the out degree of the Node with most in degree and less complexity
      // also remove the Node from in degree of the invoked Node
      if (top.getOutdegreeMeth().size() != 0) {
//        System.err.println("loop found: top out degree is " + top.getOutdegreeMeth().size());
        for (String outNodeName : top.getOutdegreeMeth().keySet()) {
          List<ASTNode> outNode = top.getOutdegreeMeth().get(outNodeName);
          for (ASTNode n : outNode) {
            ASTNode dummyNode = new ASTNode(ASTDummyMethodFactory.create(EXTERNAL_METH_REF), n);
            n.addChildren(dummyNode);
          }
          ASTMethodNode outMethodNode = ASTMethodMap.get(outNodeName);
          outMethodNode.getIndegreeMeth().remove(top.getName());
//          top.getOutdegreeMeth().remove(outNodeName);
        }
      }
      for (String inNodeName : top.getIndegreeMeth().keySet()) {
        List<ASTNode> inNode = top.getIndegreeMeth().get(inNodeName);
        ASTNode currentMethodBody;
        if (top.isSignificant()) {
          currentMethodBody = ASTNodeUtils.getMethodBody(top);
        } else {
          currentMethodBody = top;
        }

        assert currentMethodBody != null;
//        ASTNode cloneBody = currentMethodBody.clone();
        for (ASTNode n : inNode) {
//          n.addChildren(cloneBody);
          n.addChildren(currentMethodBody);
        }
        // handle dependent of top
        ASTMethodNode dependentNode = ASTMethodMap.get(inNodeName);
        priorityQueue.remove(dependentNode);
        dependentNode.getOutdegreeMeth().remove(top.getName());
        priorityQueue.add(dependentNode);
      }
    }
    // attach method that has no in degree as the root functions and attach to class node
    astClassNode.getChildren().clear();
    for (ASTMethodNode astMethodNode : ASTMethodMap.values()) {
      if (astMethodNode.getIndegreeMeth().size() == 0) {
        // get the methods that are not invoked by any methods
        astClassNode.addChildren(astMethodNode);
      }
    }
    return astClassNode;
  }

}
