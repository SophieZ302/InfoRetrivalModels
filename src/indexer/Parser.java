package indexer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Parse file into Jason Format using Jsoup, only caring about DOCNO and TXT,
 * convert to Json using Gson, then use HTTP put command to save on Elastic Search.
 */
public class Parser {

  private static double totalWordLength;
  private static double avgWordLength;
  private static final double totalDocumentNum = 84678;

  public static void main(String s[]) throws IOException {
    RestClient restClient = RestClient.builder(
        new HttpHost("localhost", 9200, "http")).build();

    Response response = restClient.performRequest("GET", "/",
        Collections.singletonMap("pretty", "true"));
    System.out.println(EntityUtils.toString(response.getEntity()));

    totalWordLength = 0;
    avgWordLength = 0;
    //loop through all files in collection
    File folder = new File("data/AP_DATA/ap89_collection");
    int len = folder.listFiles().length - 1;
    int count = 0;

    for (final File fileEntry : folder.listFiles()) {
      //System.out.println(fileEntry.getName());
      String path = "data/AP_DATA/ap89_collection/" + fileEntry.getName();
      readFile(path, restClient);
      count++;
      if (count > len) {
        break;
      }
    }

    avgWordLength = totalWordLength / totalDocumentNum;

    System.out.println("Average word length:" + avgWordLength);
    restClient.close();
  }

  /**
   * Read a file from its path and returns a DataModel.
   *
   * @param filePath String of file path
   * @return DataModel list of points
   */
  private static void readFile(String filePath, RestClient restClient) throws IOException {

    Document doc = Jsoup.parse(new File(filePath), "UTF-8");
    for (Element e : doc.getElementsByTag("doc")) {
      String docno = e.getElementsByTag("docno").text();
      String text = e.getElementsByTag("text").text();
      putCommand(docno, text, restClient);
    }
  }


  /**
   * Running http put command with given doc number and text
   *
   * @param docNum docno field
   * @param text test field
   * @param restClient a rest client
   * @throws IOException when rest client is not connecting
   */
  public static void putCommand(String docNum, String text, RestClient restClient)
      throws IOException {

    //0101-0001
    String documentID = docNum.substring(4);
    System.out.println(documentID);
    //index a document

    int length = text.split(" ").length;
    totalWordLength += length;

    Index index = new Index(docNum, text, length);
    GsonBuilder builder = new GsonBuilder();
    Gson gson = new Gson();
    String json = gson.toJson(index);
    //System.out.println(gson.toJson(index));

    HttpEntity entity = new NStringEntity(
        json, ContentType.APPLICATION_JSON);

    Response indexResponse = restClient.performRequest(
        "PUT",
        "/apdataset/document/" + documentID,
        Collections.<String, String>emptyMap(),
        entity);
  }


}
