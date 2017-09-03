package query.readquery;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 *  Read in query document and stores queries into objects.
 */
public class QueryParser {

  private List<Query> queryList;

  public QueryParser(String filePath) {
    queryList = new LinkedList<>();
    setUpQueryList(filePath);
  }

  private void setUpQueryList(String filePath) {
    try {
      Scanner sc = new Scanner(new FileInputStream(filePath));

      while (sc.hasNext()) {
        String s = sc.nextLine().replace(".", " ");
        int num = Integer.parseInt(s.substring(0, 3).replace(" ", ""));
        String query = s.substring(4);

        Query q = new Query(num, query);
        queryList.add(q);
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  public List<Query> getQueryList() {
    return queryList;
  }

  public static void main(String[] s) {
    QueryParser q = new QueryParser("data/AP_DATA/query_desc.51-100.short.txt");
    for (Query qr : q.getQueryList()) {
      System.out.println(qr.toString());
    }
  }
}
