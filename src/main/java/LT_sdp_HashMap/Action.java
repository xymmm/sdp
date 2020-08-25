package LT_sdp_HashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Action {
	
	public int transshipment;
	public int quantityA;
	public int quantityB;
	
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
	
	/** to generate feasible reorder actions: if current inventory +/- transshipment + Q<maxInventory, then feasible**/
	public static int[] generateReorder(StateSpace stateSpace, State state, int transshipment, int location, int maxQuantity) {//location = 1 or 2
		if(location == 1) {
			if(stateSpace.maxInventory - (state.i1 - transshipment) > 0) {
				int[] Quantity = new int[stateSpace.maxInventory - (state.i1 - transshipment) +1];
				for(int q=0; q<Quantity.length;q++) {
					if(q<=maxQuantity) {Quantity[q] = q;}
				}	
				return Quantity;
			}else {
				int[] Quantity = {0};
				return Quantity;
			}
		}else {
			if(stateSpace.maxInventory - (state.i2 + transshipment) > 0) {
				int[] Quantity = new int[stateSpace.maxInventory - (state.i2 + transshipment) +1];
				for(int q=0; q<Quantity.length;q++) {
					if(q<=maxQuantity) {Quantity[q] = q;}
				}
				return Quantity;
			}else {
				int[] Quantity = {0};
				return Quantity;
			}
		}
	}
	
	//add state-action pairs to HashMap
	public static void addKeys(List<int[]> keyList, StateSpace stateSpace, State state, int maxQuantity) {
		//1. for a state, generate transshipments
		int[] transshipment = generateTransshipment(state);
		for(int t=0; t<transshipment.length ;t++) {
			int[] quantityA = generateReorder(stateSpace, state, transshipment[t], 1, maxQuantity);
			int[] quantityB = generateReorder(stateSpace, state, transshipment[t], 2, maxQuantity);
			for(int a=0; a<quantityA.length; a++) {
				for(int b=0; b<quantityB.length; b++) {
					int[] key = {state.i1, state.i2, transshipment[t], quantityA[a], quantityB[b]};
					keyList.add(key);
				}
			}
		}
	}
	
	/**test*
	public static void main(String args[]) {
		int maxQuantity = 10;
		StateSpace stateSpace = new StateSpace(-10, 10);
		State state = new State (2,1);
		System.out.println("feasible transshipment quantity = "+Arrays.toString(generateTransshipment(state)));
		//System.out.println(Arrays.toString(generateReorder(stateSpace, state, 1, 1)));
		List<int[]> keyList = new ArrayList<int[]>();
		addKeys(keyList, stateSpace, state, maxQuantity);
		for(int i=0; i<keyList.size();i++) {
			System.out.println("feasible keys:"+Arrays.toString(keyList.get(i)));
		}
	}*/
	
}
