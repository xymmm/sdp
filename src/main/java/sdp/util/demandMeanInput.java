package sdp.util;

import java.util.Arrays;

public class demandMeanInput {

	
	public static double[] createDemandMeanInputRow(double[] demandMean, int timePeriodIndex){
		double[] demandMeanRow = new double[demandMean.length - timePeriodIndex];
		for(int i=0; i<demandMeanRow.length; i++) {
			demandMeanRow[i] = demandMean[i+timePeriodIndex];
		}
		return demandMeanRow;
	}
	
	public static double[][] createDemandMeanInput(double[] demandMean){
		double[][] demandMeanInput = new double[demandMean.length][];
		for(int t=0; t<demandMean.length; t++) {
			demandMeanInput[t] = createDemandMeanInputRow(demandMean, t);
		}
		return demandMeanInput;
	}
	
	//int version
	public static int[] createDemandMeanInputRowInt(int[] demandMean, int timePeriodIndex){
		int[] demandMeanRow = new int[demandMean.length - timePeriodIndex];
		for(int i=0; i<demandMeanRow.length; i++) {
			demandMeanRow[i] = demandMean[i+timePeriodIndex];
		}
		return demandMeanRow;
	}
	
	public static int[][] createDemandMeanInputInt(int[] demandMean){
		int[][] demandMeanInput = new int[demandMean.length][];
		for(int t=0; t<demandMean.length; t++) {
			demandMeanInput[t] = createDemandMeanInputRowInt(demandMean, t);
		}
		return demandMeanInput;
	}
	
	public static void main(String[] args) {
		double[] arr= {1,2,3,3,4};
		double[] row = createDemandMeanInputRow(arr,3);
		System.out.println(Arrays.toString(row));
	}
}
