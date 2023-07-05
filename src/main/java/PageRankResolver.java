import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.alg.scoring.PageRank;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import utils.FileUtils;

public class PageRankResolver {
  private static final Logger logger = LogManager.getLogger(PageRankResolver.class);


  public static Map<String, Double> resolvePageRank(List<File> pathList) {
    CompilationUnit cu = null;
    HashMap<String, HashSet<String>> classToken = new HashMap<>();
    HashMap<String, HashSet<String>> classRelation = new HashMap<>();
    HashMap<String, String> classPath = new HashMap<>();
    for (File path : pathList) {
      String content = "";
      try {
        content = FileUtils.readFile(path.getAbsolutePath(), StandardCharsets.UTF_8);
      } catch (IOException e) {

      }
      try {
        cu = StaticJavaParser.parse(content);
        if (cu != null) {
          for (Node n : cu.getChildNodes()) {
            if (n instanceof ClassOrInterfaceDeclaration && !(n.getParentNode()
                .get() instanceof ClassOrInterfaceDeclaration)) {
              String name = ((ClassOrInterfaceDeclaration) n).getNameAsString();
              classPath.put(name, path.getAbsolutePath());
              HashSet<String> tokenSet = new HashSet<>();
              n.getTokenRange().get().forEach(t -> tokenSet.add(t.getText()));
              classToken.put(name, tokenSet);
            }
          }
        }
      } catch (Exception exception) {
        logger.error("error in " + path.getAbsolutePath() + " " + exception);
        String fileName = path.getName().replace(".java", "");
        classPath.put(fileName, path.getAbsolutePath());
        HashSet<String> tokenSet = new HashSet<>();
        tokenSet.addAll(Arrays.asList(content.split("[^a-zA-Z_0-9]+")));
        classToken.put(fileName, tokenSet);
      }
    }

    for (Entry<String, HashSet<String>> entry : classToken.entrySet()) {
      HashSet<String> intersectSet = new HashSet<>(classToken.keySet());
      intersectSet.retainAll(entry.getValue());
      classRelation.put(entry.getKey(), intersectSet);
//      for (String c : intersectSet) {
//        System.out.println(entry.getKey() + " -> " + c);
//      }
    }

    Graph<String, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
    for (Entry<String, HashSet<String>> entry : classRelation.entrySet()){
      String className = entry.getKey();
      for (String refClassName : entry.getValue()) {
        if (refClassName.equals(className)) {
          continue;
        }
        g.addVertex(className);
        g.addVertex(refClassName);
//        g.addEdge(className, refClassName);
        g.addEdge(refClassName, className); //reverse
      }
    }


    PageRank<String, DefaultEdge> pageRank = new PageRank<>(g);
    HashMap<String, Double> pageRankMap = new HashMap<>();
    for (String vertex : g.vertexSet()) {
      double pageRankVertexScore = pageRank.getVertexScore(vertex);
      String cl = vertex + "@" + classPath.get(vertex);
      pageRankMap.put(cl, pageRankVertexScore);
    }
    Map<String, Double> sortPageRankMap = pageRankMap.entrySet().stream()
        .sorted(Map.Entry.<String, Double>comparingByValue().reversed()).collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue,
            (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    return sortPageRankMap;
  }
}
