package query.algorithm;

import query.Util;

/**
 * The query.algorithm.Algo_BM25 class calculates the ranking using OKAPI query.algorithm.Algo_BM25 formula
 */
public class Algo_BM25 {
  public static double calculate(double tf, double len, double avgDocLen, double df){

    double k1 = 1.2;
    double k2 = 0;
    double b = 0.75;

    double p1 = Math.log(Util.totalDocumentNum + 0.5) / (df + 0.5);
    double p2 = (tf + (k1 * tf))/ (tf + (k1 * ((1 - b) + (b * len / avgDocLen))));
    double p3 = (tf + (k2 * tf))/ (tf + k2);

    return  p1 * p2 * p3;
  }
}
