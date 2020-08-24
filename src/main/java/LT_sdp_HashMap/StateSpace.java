package LT_sdp_HashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StateSpace {
	
	public int maxInventory;
	public int minInventory;
	
	public StateSpace(int maxInventory, int minInventory) {
		this.maxInventory = maxInventory;
		this.minInventory = minInventory;
	}
		
	public int getStateSpaceSize() {
		return maxInventory - minInventory +1;
	}
	
	public List<int[]> generateStateSpace(){
		List<int[]> allStates = new ArrayList<int[]>();
		int inventoryB = minInventory;
		for(int i= minInventory; i<=maxInventory; i++) {
			for(int j= minInventory; j<=maxInventory; j++) {
				int[] state = {i, j};
				allStates.add(state);
			}
		};
		return allStates;
	}

	/**test state space**/
	public static void main(String args[]) {
		StateSpace statespace = new StateSpace(5,-3);
		List<int[]> allStates = statespace.generateStateSpace();
		System.out.println(Arrays.deepToString(allStates.toArray()));
		int[] a = allStates.get(35);
		System.out.println(Arrays.toString(a));
	}
}
