package query;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

/**
 * Utility class for constants, and reusable functions.
 */
public class Util {

  public static final double SUM_TTF = 20970193;
  public static final double VocabSize = 178081;
  public static final double totalDocumentNum = 84678;
  public static final double AVG_DOCLEN = 441;

  public static Map<String, Integer> docLenMap = new HashMap<>();



  public static double getTTF(String word, RestClient restClient) throws IOException {
    HttpEntity entity = new NStringEntity(
        "{\n"
            + " \"size\" : 1,\n"
            + "  \"script_fields\" : {\n"
            + "        \"ttf\" : {\n"
            + "            \"script\" : {\n"
            + "                \"lang\": \"groovy\",\n"
            + "                \"inline\": \"_index['text'][/" + word + "/].ttf()\"\n"
            + "            }\n"
            + "        }\n"
            + "    }"
            + "}", ContentType.APPLICATION_JSON);

    //get term frequency,  hit total = document frequency = df

    Response response1 = restClient.performRequest("GET",
        "/apdataset/document/_search", Collections.<String, String>emptyMap(),
        entity);

    String str = EntityUtils.toString(response1.getEntity());
    JsonParser parser = new JsonParser();
    JsonObject object = parser.parse(str).getAsJsonObject();

    Double e = object.getAsJsonObject("hits").getAsJsonArray("hits").
        get(0).getAsJsonObject().getAsJsonObject("fields").getAsJsonArray("ttf").get(0)
        .getAsDouble();

    //System.out.println(e.toString());

    return e;
  }

  /**
   * Class for finding tf for a word, returns a list of documents and tf frequency in Json.
   */
  public static String getTF(String word, RestClient restClient) throws IOException {
    HttpEntity entity = new NStringEntity(
        "{\n"
            + " \"size\" : 10000,\n"
            + " \"query\": {\n"
            + "           \"match\": {\n"
            + "            \"text\": \"" + word + "\"\n"
            + "           }\n"
            + "  },\n"
            + "  \"script_fields\" : {\n"
            + "        \"tf\" : {\n"
            + "            \"script\" : {\n"
            + "                \"lang\": \"groovy\",\n"
            + "                \"inline\": \"_index['text'][/" + word + "/].tf()\"\n"
            + "            }\n"
            + "        }\n"
            + "    }"
            + "}", ContentType.APPLICATION_JSON);

    //get term frequency,  hit total = document frequency = df
    Response response1 = restClient.performRequest("GET",
        "/apdataset/document/_search", Collections.<String, String>emptyMap(),
        entity);

    return EntityUtils.toString(response1.getEntity());
  }

}
