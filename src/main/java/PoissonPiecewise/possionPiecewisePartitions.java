package PoissonPiecewise;

import java.util.Arrays;
import org.apache.commons.math3.distribution.PoissonDistribution;

public class possionPiecewisePartitions {
	
	/**formulate 3D matrix for d_jt with p partitions**/
	public static double[][][] lamdaMatrix(int[] demandMean, int partitions, int binCounts){
		double[][][] lamdaMatrix = new double [demandMean.length][demandMean.length][partitions];
		for(int t=0; t<demandMean.length; t++) {
			for(int j=0; j<=t; j++){
				int sumLamda = convolution(demandMean, j,t);
				lamdaMatrix[j][t] = conditionalExpectationGivenPartitions(sumLamda, binCounts, partitions);
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
	public static double[] computeProb(double partitions) {
		double[] prob = new double[(int) partitions];
		for(int i=0; i<prob.length; i++) {
			prob[i] = 1/partitions;
		}
		return prob;
	}
	/**to compute accumulative probability of subregions**/
	public static double[] computeAccumulativeProb (double partitions, double[] prob) {
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
	public static double[] conditionalExpectationGivenPartitions(int lamda, int binCounts, double partitions) {
		double[] prob = computeProb(partitions);
		PoissonDistribution PoissonDist = new PoissonDistribution(lamda);
		int[] samples = PoissonDist.sample(binCounts);
		Arrays.sort(samples);
		double[] exp = new double [(int) partitions];
		int conditionalExpectationIndex = 1;
		double probabilityMass = 0;
		for(int i=0; i<binCounts; i++) {
			if((probabilityMass<1)&&(probabilityMass<prob[conditionalExpectationIndex])) {
				exp[conditionalExpectationIndex] += samples[i]/binCounts;
				probabilityMass += 1/binCounts;
			}else {
				exp[conditionalExpectationIndex] /= prob[conditionalExpectationIndex];
				probabilityMass = 0;
				conditionalExpectationIndex ++;
			}
		}
		exp[conditionalExpectationIndex] /= prob[conditionalExpectationIndex];

		return exp;
	}
	
	

	public static void main(String[] args) {
		/*
		double[] exp = matrixSampling(20, 100000, 5);
		for(int i=0; i<5; i++) {
			System.out.println(exp[i]);
		}*/
		
		int[] demandMean = {2,4,6,4};
		int c = convolution(demandMean, 1, 3);
		System.out.println(c);
		
	}
	
	
	

}
