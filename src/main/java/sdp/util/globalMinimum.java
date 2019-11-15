package sdp.util;

public class globalMinimum {

	public static int getGlobalMinimumJavaIndex(double[] arr) {
		double min = arr[0];
		int index = 0;
		for(int i=1; i<arr.length; i++) {
			if((arr[i]<arr[i-1])&&(arr[i]<min)) {
				min = arr[i];
				index = i;
			}
		}
		return index;
	}
	
}
