package utils;

import static utils.Constant.EXTERNAL_METH_REF;
import static utils.Constant.INSIGNIFICANT_METH_REF;
import static utils.Constant.SELF_LOOP_REF;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.visitor.GenericVisitor;
import com.github.javaparser.ast.visitor.VoidVisitor;

public class ASTDummyMethodFactory {


  public static Node create(String type) {
    switch (type) {
      case EXTERNAL_METH_REF:
        return new ExternalMethodNode();
      case SELF_LOOP_REF:
        return new SelfLoopNode();
      case INSIGNIFICANT_METH_REF:
        return new InsignificantMethodNode();
      default:
        return null;
    }
  }
}

class ExternalMethodNode extends Node {

  public ExternalMethodNode() {
    super(null);
  }


  @Override
  public <R, A> R accept(GenericVisitor<R, A> v, A arg) {
    return null;
  }

  @Override
  public <A> void accept(VoidVisitor<A> v, A arg) {

  }
}

class SelfLoopNode extends Node {

  public SelfLoopNode() {
    super(null);
  }


  @Override
  public <R, A> R accept(GenericVisitor<R, A> v, A arg) {
    return null;
  }

  @Override
  public <A> void accept(VoidVisitor<A> v, A arg) {

  }
}

class InsignificantMethodNode extends Node {

  public InsignificantMethodNode() {
    super(null);
  }


  @Override
  public <R, A> R accept(GenericVisitor<R, A> v, A arg) {
    return null;
  }

  @Override
  public <A> void accept(VoidVisitor<A> v, A arg) {

  }
}