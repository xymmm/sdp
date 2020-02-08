package reorderQuantitySystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sdp.data.Instance;
import sdp.data.InstanceDouble;

public class scheduleGenerator_sQ {
		
	/**convert a decimal number to binary number**/
    private static String toBinary(int num, int digits) {
        int value = 1 << digits | num;
        String bs = Integer.toBinaryString(value); 				//0x20 | the length of String
        return  bs.substring(1);
    }
    /**convert binary number as an integer array**/
    private static int[] toIntArray(String binaryNum) {
    	char[] arrChar = binaryNum.toCharArray(); 				// Display the contents of the char array.
    	int[] schedule = new int[binaryNum.length()];
    	for(int i = 0; i < binaryNum.length(); i++){
    		schedule[i] = arrChar[i] - '0';
    	}    	
    	return schedule;
    }
    
    /**generate all schedule for a single given Q**/
    public static int[][] generateQ(InstanceDouble instance, int Q){
    	int N = (int) Math.pow(2, instance.getStages());   	
    	int[][] schedule = new int[N][instance.getStages()];  	
    	for(int i=0; i<N; i++) {
    		schedule[i] = toIntArray(toBinary(i,instance.getStages()));
    		for(int t=0; t<instance.getStages(); t++){
    			schedule[i][t] *= Q;
    		}
    	}
    	return schedule;
    }
    
    /*
    public static void main(String args[]) {
    	int number = 1023;
    	System.out.println(toBinary(number,10));
    	System.out.println(Arrays.toString(toIntArray(toBinary(number,10))));
    }
    */
}
