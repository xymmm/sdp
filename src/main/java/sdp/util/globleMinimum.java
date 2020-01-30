package sdp.util;

public class globleMinimum {
	
	public static double getGlobalMinimum(double[] arr) {
		double min = arr[0];
		for(int i=1; i<arr.length; i++) {
			if((arr[i]<arr[i-1])&&(arr[i]<min)) {
				min = arr[i];
			}
		}
		return min;
	}
	
	/*
	public static void main(String args[]) {
		double[] arr = {1,2,3,5,4,2,6,7,7,3,2};
		System.out.println(getGlobalMinimum(arr));
	}
	*/

}
