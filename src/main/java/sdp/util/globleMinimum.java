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
		double[] arr = {33,25,69,54,24,30};
		System.out.println(getGlobalMinimum(arr));
	}
*/	

}
