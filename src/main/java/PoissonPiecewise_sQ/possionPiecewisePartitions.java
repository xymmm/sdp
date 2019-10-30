package PoissonPiecewise_sQ;

import java.util.Arrays;
import org.apache.commons.math3.distribution.PoissonDistribution;

public class possionPiecewisePartitions {
	
	/**formulate 3D matrix for d_jt with p partitions**/
	public static double[][][] lamdaMatrix(int[] demandMean, int partitions, int nbSamples){
		double[][][] lamdaMatrix = new double [demandMean.length][demandMean.length][partitions];
		for(int t=0; t<demandMean.length; t++) {
			for(int j=0; j<=t; j++){
				int sumLamda = convolution(demandMean, j,t);
				lamdaMatrix[j][t] = conditionalExpectationGivenPartitions(sumLamda, nbSamples, partitions);
			}
		}
		return lamdaMatrix;
	}
	
	public static int convolution(int[] demandMean, int indexLower, int indexUpper) {
		int sum = 0;
		for(int i=indexLower; i<=indexUpper; i++) {
			sum += demandMean[i];
		}
		return sum;
	}
	
	/** to compute probability of subregions**/
	public static double[] computeProb(int partitions) {
		double[] prob = new double[(int) partitions];
		for(int i=0; i<prob.length; i++) {
			prob[i] = 1.0/partitions;
		}
		return prob;
	}
	/**to compute accumulative probability of subregions**/
	public static double[] computeCumulativeProb (double partitions, double[] prob) {
		double[] accumulativeProb = new double[(int) partitions];
		accumulativeProb[0] = prob[0];
		int count = 1;
		do {
			accumulativeProb[count] = accumulativeProb[count-1] + prob[count];
			count++;
		}while(count < accumulativeProb.length);
		return accumulativeProb;
	}
	
	/** to compute expectations of subregions**/
	public static double[] conditionalExpectationGivenPartitions(int lamda, int nbSamples, int partitions) {
		double[] prob = computeProb(partitions);
		PoissonDistribution PoissonDist = new PoissonDistribution(lamda);
		int[] realisations = PoissonDist.sample(nbSamples);
		Arrays.sort(realisations);
		double[] exp = new double [partitions];
		int conditionalExpectationIndex = 0;
		double probabilityMass = 0;
		for(int i=0; i<nbSamples; i++) {
			if((probabilityMass < 1) && (probabilityMass<prob[conditionalExpectationIndex])) {
				exp[conditionalExpectationIndex] += realisations[i]*1.0/nbSamples;
				probabilityMass += 1.0/nbSamples;
			}else {
				exp[conditionalExpectationIndex] /= prob[conditionalExpectationIndex];
				probabilityMass = 0;
				conditionalExpectationIndex++;
			}
		}
		exp[conditionalExpectationIndex] /= prob[conditionalExpectationIndex];

		return exp;
	}
	
	

	public static void main(String[] args) {
	   
	   //testExpectedValues();
		
		int[] demandMean = {40};
		int nbSamples = 100000;
        int partitions = 5;
		double[][][] coefficients = lamdaMatrix(demandMean, partitions, nbSamples);
		System.out.println(Arrays.deepToString(coefficients));		
	}
	
	
	
	/** Test E[d|\Omega_i] **/
	public static void testExpectedValues() {
	   int lamda = 14; 
	   int nbSamples = 100000;
	   int partitions = 5;
	   double[] targetEv = {8.99635, 11.918, 13.841, 15.8981, 19.4344};
	   double[] results = conditionalExpectationGivenPartitions(lamda, nbSamples, partitions);
	   System.out.println("Target: "+Arrays.toString(targetEv));
	   System.out.println("Result: "+Arrays.toString(results));
	}
	

}
