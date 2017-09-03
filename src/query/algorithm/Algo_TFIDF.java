package query.algorithm;

import query.Util;

public class Algo_TFIDF {

  public static double calculate(double tf, double len, double avgDocLen, double df){
    double okapi = Algo_OkapiTF.calculate(tf,len,avgDocLen);
    return okapi * Math.log(Util.totalDocumentNum/df);
  }
}
