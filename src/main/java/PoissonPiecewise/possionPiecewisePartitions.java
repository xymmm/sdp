package PoissonPiecewise;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import org.apache.commons.math3.distribution.PoissonDistribution;

public class possionPiecewisePartitions {
	
	/**formulate 3D matrix for d_jt with p partitions**/
	public static double[][][] lamdaMatrix(double[] demandMean, int partitions, int nbSamples){
		double[][][] lamdaMatrix = new double [demandMean.length][demandMean.length][partitions];
		for(int t=0; t<demandMean.length; t++) {
			//System.out.println("t = "+t);
			for(int j=0; j<=t; j++){
				//System.out.println("j = "+j);
				int sumLamda = convolution(demandMean, j,t);
				lamdaMatrix[j][t] = conditionalExpectationGivenPartitions(sumLamda, nbSamples, partitions);
			}
		}
		return lamdaMatrix;
	}
	
	public static int convolution(double[] demandMean, int indexLower, int indexUpper) {
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
	
	//write to text
	public static void writeToText(double[][][] coefficients){
		FileWriter fw = null;
		try {
			File f = new File("E:\\classicInstance.txt");
			fw = new FileWriter(f, true);//true, continue to write
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(fw);
		pw.println(Arrays.deepToString(coefficients));
		pw.println();
		pw.flush();
		try {
			fw.flush();
			pw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static double[] getDemandMeanArray(double[] demandMeanInput, int indexStart) {
		double[] demandMean = new double[demandMeanInput.length - indexStart];
		for(int j=0; j<demandMean.length; j++) {
			demandMean[j] = demandMeanInput[indexStart + j];
		}
		return demandMean;
	}


	public static void main(String[] args) {
		double [] demandMeanInput = {5, 5, 5, 5};
		int nbSamples = 100000;
		int partitions = 4;
		//for(int d=0; d<demandMeanInput.length; d++) {
			for(int i=0; i<1; i++) {
				double[] demandMean = getDemandMeanArray(demandMeanInput, i);
				double[][][] coefficients = lamdaMatrix(demandMean, partitions, nbSamples);
				System.out.println(Arrays.deepToString(coefficients));	
				writeToText(coefficients);		
			}
		//}
		

		
	}
	
	
	
	/** Test E[d|\Omega_i] 
	 *  if included in .main, by //testExpectedValues();**/
	/**
	public static void testExpectedValues() {
	   int lamda = 14; 
	   int nbSamples = 100000;
	   int partitions = 5;
	   double[] targetEv = {8.99635, 11.918, 13.841, 15.8981, 19.4344};
	   double[] results = conditionalExpectationGivenPartitions(lamda, nbSamples, partitions);
	   System.out.println("Target: "+Arrays.toString(targetEv));
	   System.out.println("Result: "+Arrays.toString(results));
	}
	**/

}
