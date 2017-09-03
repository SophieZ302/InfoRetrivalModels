package query;

import static query.Util.getTF;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import query.algorithm.Algo_BM25;
import query.algorithm.Algo_OkapiTF;
import query.algorithm.Algo_TFIDF;
import query.algorithm.Algorithm;
import query.readquery.Query;

/**
 * Class to create a map of doc and score for a single query.
 */
public class GetScore {

  private Map<String, Double> scoreMap;
  private Map<Double, String> resultMap;
  private Map<String, Double> ttfMap;
  private Query query;
  private Algorithm algorithm;

  public GetScore(Query aQuery, Algorithm algo) {
    this.algorithm = algo;
    scoreMap = new HashMap<>();
    resultMap = new TreeMap<>();
    ttfMap = new HashMap<>();
    this.query = aQuery;
  }


  public static void main(String[] s) throws IOException {
    RestClient restClient = RestClient.builder(
        new HttpHost("localhost", 9200, "http")).build();

    //query.readquery.Query query = new query.readquery.Query(58, "alleg corrupt offici");

    Query query = new Query(58, "alleg corrupt jurisdict government public taken");

    System.out.println(GetScore.run(query, restClient, Algorithm.Laplace, 100));
    restClient.close();
  }

  public static String run(Query q, RestClient restClient, Algorithm algorithm, int querySize) throws IOException {
    Query query = new Query(q.getNumber(), q.getStr());
    GetScore newQuery = new GetScore(query, algorithm);

    Map<Double, String> resultMap = newQuery.getResultMap(restClient);
    StringBuilder result = new StringBuilder();
    int count = 1;
    for (Double d : resultMap.keySet()) {
      String newResult =
          q.getNumber() + " Q0 AP89" + resultMap.get(d) + " " + count + " " + (-1 * d) + " " + "Exp"
              + "\n";
      result = result.append(newResult);
      count++;
      if (count > querySize) {
        break;
      }
    }
    //System.out.println(result.toString());
    return result.toString();
  }


  public Map<Double, String> getResultMap(RestClient restClient) throws IOException {
    produceScoreMap(restClient);

    for (Entry<String, Double> entry : scoreMap.entrySet()) {
      resultMap.put((-1 * entry.getValue()), entry.getKey());
    }
    return resultMap;
  }


  //for every word in the query, find the document contain it
  //    for each document, calculate a score
  public Map<String, Double> produceScoreMap(RestClient restClient) throws IOException {

    for (String word : query.getStr().split(" ")) {
      //System.out.println(word);
      if (word.length() == 0) {
        continue;
      }
      scoreEveryDocument(word, restClient);
    }

    return scoreMap;
  }

  private void scoreEveryDocument(String word, RestClient restClient) throws IOException {
    String str = getTF(word, restClient);

    JsonParser parser = new JsonParser();
    JsonObject object = parser.parse(str).getAsJsonObject();

    int documentFrequency = object.getAsJsonObject("hits").get("total").getAsInt();
    //System.out.println("df:" + documentFrequency);

    JsonArray listOfDoc = object.getAsJsonObject("hits").getAsJsonArray("hits");

    for (JsonElement i : listOfDoc) {
      String key = i.getAsJsonObject().get("_id").getAsString();
      int tf = i.getAsJsonObject().getAsJsonObject("fields").get("tf").getAsInt();

      //put docno and docLen into map
      int docLen;
      if (!Util.docLenMap.containsKey(key)) {
        docLen = getDocLength(key, restClient);
        Util.docLenMap.put(key, docLen);
      } else {
        docLen = Util.docLenMap.get(key);
      }
      double score;
      switch (this.algorithm) {
        case Okapi:
          score = Algo_OkapiTF.calculate(tf, docLen, Util.AVG_DOCLEN);
          break;
        case TFIDF:
          score = Algo_TFIDF.calculate(tf, docLen, Util.AVG_DOCLEN, documentFrequency);
          break;
        case BM25:
          score = Algo_BM25.calculate(tf, docLen, Util.AVG_DOCLEN, documentFrequency);
          break;

        default:
          score = Algo_BM25.calculate(tf, docLen, Util.AVG_DOCLEN, documentFrequency);
      }

      if (!scoreMap.containsKey(key)) {
        scoreMap.put(key, score);
      } else {
        double scoreTotal = scoreMap.get(key) + score;
        scoreMap.put(key, scoreTotal);
      }
      //System.out.print("total:" + scoreMap.get(key) +"\n");
    }
  }

  public static int getDocLength(String docno, RestClient restClient) throws IOException {

    Response response1 = restClient.performRequest("GET",
        "apdataset/document/" + docno + "/");

    String str = EntityUtils.toString(response1.getEntity());
    JsonParser parser = new JsonParser();
    JsonObject object = parser.parse(str).getAsJsonObject();

    return object.getAsJsonObject("_source").get("len").getAsInt();
  }

}
