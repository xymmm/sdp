package LT_sdp_HashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StateSpace {
	
	public int maxInventory;
	public int minInventory;
	
	public StateSpace(int minInventory, int maxInventory) {
		this.maxInventory = maxInventory;
		this.minInventory = minInventory;
	}
		
	public int getStateSpaceSize() {
		return maxInventory - minInventory +1;
	}
	
	public static List<int[]> generateStateSpace(StateSpace stateSpace){
		List<int[]> allStates = new ArrayList<int[]>();
		for(int i= stateSpace.minInventory; i<=stateSpace.maxInventory; i++) {
			for(int j= stateSpace.minInventory; j<=stateSpace.maxInventory; j++) {
				int[] state = {i, j};
				allStates.add(state);
			}
		};
		return allStates;
	}
	
	/*
	public static int getStateIndex(StateSpace stateSpace, State state) {
		return (state.i1 - stateSpace.minInventory)*stateSpace.getStateSpaceSize() + state.i2 - stateSpace.minInventory;
	}
	*/

	/**test state space*
	public static void main(String args[]) {
		StateSpace statespace = new StateSpace(-3,5);
		List<int[]> allStates = generateStateSpace(statespace);
		System.out.println(Arrays.deepToString(allStates.toArray()));
		int[] a = allStates.get(35);
		System.out.println(Arrays.toString(a));
		State state = new State(-3,-3);
		System.out.println(getStateIndex(statespace, state));
	}*/
}
