package sdp.util;

import org.apache.commons.math3.distribution.PoissonDistribution;

public class Demand {
   
   /** Assume that negative value in the support are not allowed **/
   public static double[] tabulateProbability(double meanDemand, int maxDemand, double tail) {
      double[] demandProbabilities = new double[maxDemand + 1];
      PoissonDistribution dist = new PoissonDistribution(meanDemand);
      for(int i=0;i<=maxDemand;i++) {
         demandProbabilities [i] = dist.probability(i);
         if(demandProbabilities [i]<tail) {
            demandProbabilities[i] = 0;
         }
      }
      return demandProbabilities;
   }
} 
