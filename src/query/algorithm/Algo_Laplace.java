package query.algorithm;

import query.Util;

/**
 * Created by qiuyuanzhang on 5/25/17.
 */
public class Algo_Laplace {

  public static double calculate(double tf, double len) {
    return Math.log((tf+1)/(len + Util.VocabSize));
  }
}
