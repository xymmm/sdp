package RecedingHorizon.sQt;

import java.util.Random;

import cern.colt.Arrays;

public class generateNormalDemandSeries {
	
	public static double[] generateNormalDemand(double[] demandMean, double stdParameter) {
		double[] demandNormal = new double[demandMean.length];
		
		//compute deviation
		double[] stdDemand = new double[demandMean.length];
		for(int t=0; t<demandMean.length; t++) {
			stdDemand[t] = stdParameter * demandMean[t];
			Random r = new Random();
			demandNormal[t] = Math.sqrt(stdDemand[t]) * r.nextGaussian() + demandMean[t];
		}
		
		return demandNormal;
	}
	
	public static double[] demandRecedingHorizon(double[] demandNormal, int currentTimeIndex) {
		double[] demandSec = new double[demandNormal.length - currentTimeIndex];
		for(int t=0; t<demandSec.length;t++) {
			demandSec[t] = demandNormal[currentTimeIndex + t];
		}
		return demandSec;
	}

	/*
	public static void main(String args[]) {
		double[] demandMean = {20,40,60,40};
		double stdParameter = 0.25;
		double[] demandNormal = generateNormalDemand(demandMean, stdParameter);
		System.out.println(Arrays.toString(demandNormal));
		System.out.println(Arrays.toString(demandRecedingHorizon(demandNormal, 2)));
	}*/
	
}
