package sdp.util;

public class globalMinimumIndex {

	public static int getGlobalMinimumJavaIndex(double[] arr) {
		double min = arr[0];
		int index = 0;
		for(int i=1; i<arr.length; i++) {
			if(arr[i]<min) {
				min = arr[i];
				index = i;
				break;
			}
			break;
		}
		return index;
	}
	

	public static void main(String args[]) {
		double[] arr = {4,2,3,5,4,2,6,7,7,3,2};
		System.out.println(sdp.util.globleMinimum.getGlobalMinimum(arr));
		System.out.println(getGlobalMinimumJavaIndex(arr));
	}


	
}
