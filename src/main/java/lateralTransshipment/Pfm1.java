package lateralTransshipment;

import java.util.Arrays;
import java.util.stream.IntStream;

import org.apache.commons.math3.distribution.PoissonDistribution;

public class Pfm1 {
	/************************************ generate demand pairs and probabilities (pmf) ***********************************************************/
	public static double[][][] generatePMF(int[] demandMean1, int[] demandMean2, double tail){
		double[][][] pmf = new double[demandMean1.length][][];
		
		IntStream.range(0,demandMean1.length).forEach(t->{

			PoissonDistribution dist1 = new PoissonDistribution(demandMean1[t]);
			int maxDemand1 = dist1.inverseCumulativeProbability(1-tail);
			
			PoissonDistribution dist2 = new PoissonDistribution(demandMean2[t]);
			int maxDemand2 = dist2.inverseCumulativeProbability(1-tail);
			
			double[][] curPmf = new double[maxDemand1*maxDemand2][3];
			
			IntStream.range(0,maxDemand1).forEach(demand1 -> {
				IntStream.range(0,maxDemand2).forEach(demand2 -> {
					double p = dist1.probability(demand1)*dist2.probability(demand2)/
							(dist1.cumulativeProbability(maxDemand1)*dist2.cumulativeProbability(maxDemand2));
					curPmf[demand1*maxDemand2+demand2] =new double[]{p,demand1,demand2};
				});
			});

			pmf[t] = curPmf;
		});
		
		return pmf;
	}
	
	public static void main(String[] args) {
		int[] demandMean1 = {2,4};
		int[] demandMean2 = {6, 4};
		double tail = 0.000000000001;
		
		double[][][] pmf = generatePMF(demandMean1, demandMean2, tail);
		
		Arrays.stream(pmf).forEach(towArr -> Arrays.stream(towArr).forEach(oneArr -> {
			System.out.println(oneArr[0] + " | " + oneArr[1] + " | " + oneArr[2] );
		}));
				
	}
}
