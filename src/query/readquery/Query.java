package query.readquery;

/**
 * A query.readquery.Query class has a query number, query string and a list of documents.
 */
public class Query {
  private int number;
  private String str;

  public Query(int number, String str){
    this.number = number;
    this.str = str;
  }

  public int getNumber() {
    return number;
  }

  public String getStr() {
    return str;
  }

  public void setNumber(int number) {
    this.number = number;
  }

  @Override
  public String toString(){
    return number + str;
  }
}
