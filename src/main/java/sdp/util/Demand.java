package sdp.util;

import org.apache.commons.math3.distribution.PoissonDistribution;

public class Demand {
   
   /** Assume that negative value in the support are not allowed **/
   public static double[] tabulateProbability(double meanDemand, double tail) {
      
      PoissonDistribution dist = new PoissonDistribution(meanDemand);
      
      int maxDemand = dist.inverseCumulativeProbability(1-tail);
      double[] demandProbabilities = new double[maxDemand + 1];
      
      //double normalizationFactor = dist.inverseCumulativeProbability(tail) - dist.probability(0);
      
      for(int i=0;i<=maxDemand;i++) {
         demandProbabilities [i] = dist.probability(i)/dist.cumulativeProbability(maxDemand);
      }
      return demandProbabilities;
   }
   
   public static void main(String args[]) {
	   
	   double[] demandProbabilities = new double[250 + 1];
	   double tail = 0.9;
	   
	   PoissonDistribution dist = new PoissonDistribution(50);
	   
	   int prob = dist.inverseCumulativeProbability(0.9);
	   System.out.println(prob);
	   
	   
   }
} 
