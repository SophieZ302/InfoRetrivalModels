package query.algorithm;

/**
 * Calculating the okapi tf.
 */
public class Algo_OkapiTF {
  public static double calculate(double tf, double len, double avgDocLen){
    double denom = tf + 0.5 + 1.5 * (len/avgDocLen);
    return tf/denom;
  }

}
