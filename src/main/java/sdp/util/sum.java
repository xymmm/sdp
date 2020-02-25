package sdp.util;

public class sum {
	
	public static double summation(double[] arr) {
		double sum = 0;
		for(int i=0; i<arr.length; i++) {
			sum += arr[i];
		}
		return sum;
	}

}
