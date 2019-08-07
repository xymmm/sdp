package sdp.util;

import org.apache.commons.math3.distribution.PoissonDistribution;

public class Demand {
   
   /** Assume that negative value in the support are not allowed **/
   public static double[] tabulateProbability(double meanDemand, double tail) {
      
      PoissonDistribution dist = new PoissonDistribution(meanDemand);
      
      int maxDemand = dist.inverseCumulativeProbability(1-tail);
      double[] demandProbabilities = new double[maxDemand + 1];
      
      for(int i=0;i<=maxDemand;i++) {
         demandProbabilities [i] = dist.probability(i)/dist.cumulativeProbability(maxDemand);
      }
      return demandProbabilities;
   }
} 
