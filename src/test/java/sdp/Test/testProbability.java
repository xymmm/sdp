package sdp.Test;

import java.text.DecimalFormat;

import org.apache.commons.math3.distribution.PoissonDistribution;

import sdp.dataProcess.Demand;

public class testProbability {
	
	   public static void testPoissonDistribution() {
		   
		   DecimalFormat df = new DecimalFormat("#.000000"); 

		       
		   PoissonDistribution dist = new PoissonDistribution(20);
		   
		   double ProbMin = dist.cumulativeProbability(0);
		   double ProbMax = dist.cumulativeProbability(250);
		   System.out.println(df.format(ProbMax - ProbMin));
		   }
		   
		   public static void main(String args[]) {
		      testPoissonDistribution();
		   }

}
