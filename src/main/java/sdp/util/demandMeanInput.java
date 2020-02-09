package sdp.util;

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
}
