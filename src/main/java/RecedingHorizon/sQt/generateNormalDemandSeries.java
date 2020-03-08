package RecedingHorizon.sQt;

public class generateNormalDemandSeries {

	public static double[] futureDemandSegment(double[] demandNormal, int currentTimeIndex) {
		double[] demandSec = new double[demandNormal.length - currentTimeIndex];
		for(int t=0; t<demandSec.length;t++) {
			demandSec[t] = demandNormal[currentTimeIndex + t];
		}
		return demandSec;
	}

}
