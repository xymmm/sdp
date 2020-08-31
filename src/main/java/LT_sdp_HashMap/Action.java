/*package LT_sdp_HashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Action {
	
	* to generate feasible reorder actions: if current inventory +/- transshipment + Q<maxInventory, then feasible**
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
	
/*	
	public static void generateActions(List<int[]> ActionList, StateSpace stateSpace, State state, int maxQuantity) {
		//1. for a state, generate transshipments
		int[] transshipment = generateTransshipment(state);
		for(int t=0; t<transshipment.length ;t++) {
			int[] quantityA = generateReorder(stateSpace, state, transshipment[t], 1, maxQuantity);
			int[] quantityB = generateReorder(stateSpace, state, transshipment[t], 2, maxQuantity);
			for(int a=0; a<quantityA.length; a++) {
				for(int b=0; b<quantityB.length; b++) {
					int[] key = {transshipment[t], quantityA[a], quantityB[b]};
					ActionList.add(key);
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
	}
	
}*/
