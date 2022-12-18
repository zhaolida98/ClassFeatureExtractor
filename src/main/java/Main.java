import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.JClass;
import model.ListenerReturn;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

public class Main {

  private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

  public static void main(String[] args) throws IOException {
    String filePath = "/home/nryet/JavaCloneExplore/java-antler-parser/src/main/java/test.java";
//    InputStream inputStream = Files.newInputStream(Paths.get(filePath));
    ClassContentAnalyser methodFinder = new ClassContentAnalyser();
    List<JClass> classList = methodFinder.resolveClassList(filePath);
    System.out.println(GSON.toJson(classList));

//    for (String k : constructors.keySet()) {
//      System.out.println(k + "\n" + constructors.get(k));
//    }
//
//    for (String k : methods.keySet()) {
//      System.out.println(k + "\n" + methods.get(k));
//    }


//    Java8Lexer lexer = new Java8Lexer(CharStreams.fromStream(inputStream));
//    Java8Parser parser = new Java8Parser(new CommonTokenStream(lexer));
//    parser.setBuildParseTree(true);
//    ParseTree parseTree = parser.compilationUnit();
//    System.out.println(parseTree.toStringTree(parser));

// If `compilationUnit` didn't produce any errors, you know the file is
// syntactically correct, now just dump the tokens to the STDOUT
//    lexer.reset();
//    CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
//    commonTokenStream.fill();

//    for (Token t : commonTokenStream.getTokens()) {
//      System.out.printf(
//          "%d:%d:%d:%s:'%s'%n",
//          t.getLine(), t.getCharPositionInLine(), t.getCharPositionInLine()+t.getText().length(),
//          Java8Lexer.VOCABULARY.getSymbolicName(t.getType()), t.getText()
//      );
//      System.out.println(t);
//    }
  }
}