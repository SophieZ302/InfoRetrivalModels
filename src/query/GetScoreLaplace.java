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
import query.algorithm.Algo_Laplace;
import query.algorithm.Algorithm;
import query.readquery.Query;

/**
 * Class to create a map of doc and score for a single query.
 */
public class GetScoreLaplace {

  private Map<String, Doc> scoreMap;
  private Map<Double, String> resultMap;
  private Map<String, Double> ttfMap;
  private Query query;
  private Algorithm algorithm;

  public GetScoreLaplace(Query aQuery, Algorithm algo) {
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
    System.out.println(GetScoreLaplace.run(query, restClient, Algorithm.Laplace, 100));
    restClient.close();
  }

  public static String run(Query q, RestClient restClient, Algorithm algorithm, int querySize)
      throws IOException {
    Query query = new Query(q.getNumber(), q.getStr());
    GetScoreLaplace newQuery = new GetScoreLaplace(query, algorithm);
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
    return result.toString();
  }


  /**
   * Convert score map into inverse ordered tree map, descending score value.
   */
  public Map<Double, String> getResultMap(RestClient restClient) throws IOException {
    produceScoreMap(restClient);
    for (Entry<String, Doc> entry : scoreMap.entrySet()) {
      resultMap.put((-1 * entry.getValue().getScore()), entry.getKey());
    }
    return resultMap;
  }

  /**
   * Produce a map of [doc num, score].
   */
  private Map<String, Doc> produceScoreMap(RestClient restClient) throws IOException {
    //initialize score Map, get all document with query words in it.
    initScore(restClient);

    for (String word : query.getStr().split(" ")) {
      if (word.length() != 0) {
        scoreEveryDocument(word, restClient);
      }
    }
    return scoreMap;
  }

  /**
   * Find all document with query words using query search match.
   */
  private void initScore(RestClient restClient) throws IOException {
    HttpEntity entity = new NStringEntity(
        "{\n"
            + " \"size\" : 10000,\n"
            + " \"_source\" : \"len\","
            + " \"query\": {\n"
            + "           \"match\": {\n"
            + "            \"text\": \"" + query.toString() + "\"\n"
            + "           }\n"
            + "  }\n"
            + "}", ContentType.APPLICATION_JSON);

    Response response = restClient.performRequest("GET",
        "/apdataset/document/_search", Collections.<String, String>emptyMap(),
        entity);
    String str = EntityUtils.toString(response.getEntity());
    JsonParser parser = new JsonParser();
    JsonObject object = parser.parse(str).getAsJsonObject();
    JsonArray listOfDoc = object.getAsJsonObject("hits").getAsJsonArray("hits");

    for (JsonElement i : listOfDoc) {
      String key = i.getAsJsonObject().get("_id").getAsString();
      int len = i.getAsJsonObject().get("_source").getAsJsonObject().get("len").getAsInt();
      Doc newDoc = new Doc(key, len);
      scoreMap.put(key, newDoc);
    }
    //System.out.println(scoreMap.size());
  }


  private void scoreEveryDocument(String word, RestClient restClient) throws IOException {
    Map<String, Double> halfScoreMap = scoreTerms(word, restClient);

    for (String docno : scoreMap.keySet()) {

      //search document for term, if there is a term frequency , calculate score

      double score = 0;
      if (halfScoreMap.containsKey(docno)) {
        score = halfScoreMap.get(docno);
      } else {
        //zero term frequency
        score = Algo_Laplace.calculate(0, scoreMap.get(docno).getLen());
      }
      double scoreTotal = scoreMap.get(docno).getScore() + score;
      scoreMap.get(docno).setScore(scoreTotal);
    }
  }

  /**
   * Score every document for a term, store their value in a table [doc num, score].
   */
  private Map<String, Double> scoreTerms(String word, RestClient restClient) throws IOException {
    Map<String, Double> halfSoreMap = new HashMap<>();
    String str = getTF(word, restClient);
    JsonParser parser = new JsonParser();
    JsonObject object = parser.parse(str).getAsJsonObject();
    JsonArray listOfDoc = object.getAsJsonObject("hits").getAsJsonArray("hits");

    for (JsonElement i : listOfDoc) {
      String key = i.getAsJsonObject().get("_id").getAsString();
      int tf = i.getAsJsonObject().getAsJsonObject("fields").getAsJsonArray("tf").get(0).getAsInt();
      //System.out.println(tf);
      if (scoreMap.containsKey(key)) {
        double score = Algo_Laplace.calculate(tf, scoreMap.get(key).getLen());
        halfSoreMap.put(key, score);
        //System.out.print("docno:" + key + " score:" + score + halfSoreMap.toString());
      }
    }
    return halfSoreMap;
  }


}
