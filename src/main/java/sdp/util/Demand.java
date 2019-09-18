package sdp.util;

import org.apache.commons.math3.distribution.NormalDistribution;
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
   
   public static double[] normalProbability(int meanDemand, double stdPara, double tail) {
	   
	   double stdDemand = meanDemand * stdPara;
	   
	   NormalDistribution dist = new NormalDistribution(meanDemand, stdDemand);
	   
	   int maxDemand = (int) Math.floor(dist.inverseCumulativeProbability(1-tail));
	   double[] demandProbabilities = new double[maxDemand + 1];
	   
	   for(int i=0; i<=maxDemand;i++) {
		   demandProbabilities [i] = (dist.probability(i-0.5, i+0.5))/(dist.cumulativeProbability((double)maxDemand) - dist.cumulativeProbability(-0.5));
	   }
	   
	   return demandProbabilities;
   }
   
} 
