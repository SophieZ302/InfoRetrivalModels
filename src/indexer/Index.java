package indexer;

/**
 * A node in elastic search.
 */
public class Index {

  public String docno;
  public String text;
  public Integer len;

  public Index(String num, String txt, Integer length) {
    this.docno = num;
    this.text = txt;
    this.len = length;
  }

}
