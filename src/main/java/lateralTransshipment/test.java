package lateralTransshipment;

import org.apache.commons.math3.distribution.PoissonDistribution;

public class test {

	public static double[][][] generatePMFbyLoop(int[] demandMean1, int[] demandMean2, double tail){
		double[][][] pmf = null;

		for(int t=0; t<demandMean1.length; t++) {
			
			PoissonDistribution dist1 = new PoissonDistribution(demandMean1[t]);
			PoissonDistribution dist2 = new PoissonDistribution(demandMean2[t]);
			int maxDemand1 = dist1.inverseCumulativeProbability(1-tail);
			int maxDemand2 = dist2.inverseCumulativeProbability(1-tail);
						
			int f=0;
			do {
				for(int i=0; i<=maxDemand1; i++) {
					for(int j=0; j<=maxDemand2; j++) {
						pmf[t][f][0] = dist1.probability(i) * dist2.probability(j) 
										/ (dist1.cumulativeProbability(maxDemand1) * dist2.cumulativeProbability(maxDemand2));
						pmf[t][f][1] = i;
						pmf[t][f][2] = j;
						
						f++;
					}
				}
			}while(f<=maxDemand1 * maxDemand2);
			
		}

		
}
