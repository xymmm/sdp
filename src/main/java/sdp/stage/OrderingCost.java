package sdp.stage;

import sdp.dataProcess.Data;


public abstract class OrderingCost {
	
	
	/**
	 * This method calculates the ordering cost to each quantity value.
	 * 
	 * @param maxQuantity
	 * @return
	 */
	public double [] calOderingCost(int maxQuantity) {
		double[] oc = new double[maxQuantity+1];
		for(int i=0;i<oc.length;i++) {
			oc[i] = Data.fixCost + Data.unitCost*i;	
		}
		return oc;
	}
	
	/**
	 * This method searches and returns the minimum value of a 1-dimension double array.
	 * @param arr
	 * @return
	 */
	public double getMinimum(double[] arr) {
		double min = arr[0];
		for(int i=1;i<arr.length;i++) {
			if(arr[i]<min) {
				min = arr[i];
			}
		}
		return min;
	}
	
}
