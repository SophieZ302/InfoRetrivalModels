package query;

/**
 * A document type for calculating and recording term frequency, etc.
 */
public class Doc {

  private int length;
  private String docno;
  private double score;

  public Doc(String docno, int length) {
    this.length = length;
    this.docno = docno;
    this.score = 0;
  }

  public void setScore(double s) {
    this.score = s;
  }

  public double getScore() {
    return this.score;
  }

  public int getLen() {
    return length;
  }

  public String getDocno() {
    return docno;
  }
}
