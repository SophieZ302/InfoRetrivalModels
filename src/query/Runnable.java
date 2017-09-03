package query;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import query.algorithm.Algorithm;
import query.readquery.Query;
import query.readquery.QueryParser;

/**
 * To output score in sorted order in a txt file.
 */
public class Runnable {

  public static void main(String[] s) throws IOException {
    //Algorithm algorithm = Algorithm.JMSmooth;
    //Algorithm algorithm = Algorithm.Laplace;
    //Algorithm algorithm = Algorithm.BM25;
    //Algorithm algorithm = Algorithm.TFIDF;
    Algorithm algorithm = Algorithm.Okapi;


    PrintWriter out = new PrintWriter(new FileWriter(algorithm.toString() + ".txt"));
    QueryParser q = new QueryParser("data/AP_DATA/query_desc.51-100.short.txt");
    System.out.println(q.getQueryList().size() + " queries total");
    RestClient restClient = RestClient.builder(
        new HttpHost("localhost", 9200, "http")).build();
    int rankSize = 1000;

    for (Query query : q.getQueryList()) {
      System.out.println(query.toString());
      switch (algorithm) {
        case JMSmooth:
          out.print(GetScoreJM.run(query, restClient, algorithm, rankSize));
          break;
        case Laplace:
          out.print(GetScoreLaplace.run(query, restClient, algorithm, rankSize));
          break;
        case TFIDF:
          out.print(GetScore.run(query, restClient, algorithm, rankSize));
          break;
        default:
          out.print(GetScore.run(query, restClient, algorithm, rankSize));
      }
    }

    restClient.close();
    out.close();
  }
}
