package LT_sdp_HashMap;

import java.util.Arrays;

public class Action {
	
	public int transshipment;
	public int quantityA;
	public int quantityB;
	public int maxQuantity;
	
	public Action(int transshipment, int quantityA, int quantityB) {
		this.transshipment = transshipment;
		this.quantityA = quantityA;
		this.quantityB = quantityB;
	}
	
	
	/** for a given state of two inventory levels, generate all feasible actions**/
	public static int[] generateTransshipment(State state){
		//int[] feasibleTransshipment = null;
		if((state.i1 <=0)&&(state.i2 <= 0)) {//case 4
			return new int[] {0};
		}else if((state.i1 <=0)&&(state.i2 > 0)) {//case 3
			int[] feasibleTransshipment = new int[state.i2+1];
			for(int i=0; i<feasibleTransshipment.length;i++) {
				feasibleTransshipment[i] = -state.i2 + i;
			}
			return feasibleTransshipment;
		}else if((state.i1 > 0)&&(state.i2 <= 0)) {//case 2
			int[] feasibleTransshipment = new int[state.i1+1];
			for(int i=0; i<feasibleTransshipment.length; i++) {
				feasibleTransshipment[i] = i;
			}
			return feasibleTransshipment;
		}else {
			int[] feasibleTransshipment = new int[state.i1 + state.i2 +1];
			for(int i=0; i<feasibleTransshipment.length; i++) {
				feasibleTransshipment[i] = -state.i2 + i;
			}
			return feasibleTransshipment;
		}
	}
	
	/**test*
	public static void main(String args[]) {
		State state = new State (2,1);
		System.out.println(Arrays.toString(generateTransshipment(state)));
	}*/
	
	//add state-action pairs to HashMap
}
