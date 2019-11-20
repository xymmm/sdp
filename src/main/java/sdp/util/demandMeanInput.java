package sdp.util;

public class demandMeanInput {

	
	public static int[] createDemandMeanInputRow(int[] demandMean, int timePeriodIndex){
		int[] demandMeanRow = new int[demandMean.length - timePeriodIndex];
		for(int i=0; i<demandMeanRow.length; i++) {
			demandMeanRow[i] = demandMean[i+timePeriodIndex];
		}
		return demandMeanRow;
	}
	
	public static int[][] createDemandMeanInput(int[] demandMean){
		int[][] demandMeanInput = new int[demandMean.length][];
		for(int t=0; t<demandMean.length; t++) {
			demandMeanInput[t] = createDemandMeanInputRow(demandMean, t);
		}
		return demandMeanInput;
	}
}
