package query.algorithm;

import query.Util;

public class Algo_JMSmooth {

    public static double calculate(double tf, double len, double ttf) {
      double lambda = 0.7;

      double p1 = lambda * tf/len;

      double p2 = (1 - lambda) * ttf / Util.SUM_TTF;

      return Math.log(p1 + p2);
    }
}
