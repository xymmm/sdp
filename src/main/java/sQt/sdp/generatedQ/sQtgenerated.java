package sQt.sdp.generatedQ;

//Q \in {0,1,2,3,4,5,6,7,8,9}
import sdp.data.InstanceDouble;

public class sQtgenerated {

	
	/** create an array, where each entry is a duoble number ranged in [0, 10^T-1].**/
	public static double[] createGenerator(InstanceDouble instance) {
		//generator = 0-9999
		double generator[] = new double[(int) (Math.pow(10, instance.getStages()))];
		for(int i=0; i<generator.length;i++) {
			generator[i] = i;
		}
		return generator;
	}
	
	/**summation of entries*10^t of an array before a given index**/
	public static double sumWithPower(double[] arr, int stopIndex) {//befor stopIndex, not include it
		double sum = 0;
		for(int i=0;i<stopIndex;i++) {
			sum = sum + arr[i]*Math.pow(10, i);
		}
		return sum;
	}
	/**reverse an array**/
	public static double[] reverseArray(double[] arr) {
		double[] arrNew = new double[arr.length];
		for(int i=0; i<arr.length;i++) {
			arrNew[i] = arr[arr.length-1-i];
		}
		return arrNew;
	}
	
	/**
	 * Given a value of generator ranged in [0,10^T-1], a series of Qt is created by modular operation.
	 * The assignment starts with the last period, and is completed backwards.
	 */
	public static double[] generateQ(int timeHorizon, double generator) {
		double[] Q = new double [timeHorizon];
		double[] divisor = new double [timeHorizon];
		for(int t=0; t<timeHorizon;t++) {
			divisor[t] = Math.pow(10, t+1);
		}
		for(int t=0; t<timeHorizon;t++) {	
			Q[t] = (generator % divisor[t] - ((t==0)? 0: sumWithPower(Q,t)))/Math.pow(10, t);
		}
		return reverseArray(Q);
	}
	
	public static sQtgeneratedSolution sQtgeneratedSingle() {
		return new sQtgeneratedSolution();
	}
	
	public static sQtgeneratedSolution sQtgeneratedAll(InstanceDouble instance) {


		return new sQtgeneratedSolution();
	}
	
	public static double[] sQtgeneratedReorderPoints(int timeHorizon, sQtgeneratedSolution sQtgeneratedSolution) {
		double[] reorderPoints = new double[timeHorizon];
		return reorderPoints;
	}
	
}
