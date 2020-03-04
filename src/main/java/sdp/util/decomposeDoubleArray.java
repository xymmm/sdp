package sdp.util;

import java.util.Arrays;

public class decomposeDoubleArray {
	
	public static double[] decomposeArray(double[] array, int length) {
		double[] newArray = new double[length];
		int diff = array.length - length;
		for(int i=0; i<newArray.length; i++) {
			newArray[i] = array[diff+i];
		}
		return newArray;
	}
	
	public static void main(String args[]) {
		double[] array = {0,1,2,3,4,5,6,7};
		double[] newarray = decomposeArray(array, 3);
		System.out.println(Arrays.toString(newarray));
	}

}
