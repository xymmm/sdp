package LT_sdp_HashMap;

import java.util.Arrays;

public class State {

	public int i1;
	public int i2;
	
	public State(int i1, int i2) {
		this.i1 = i1;
		this.i2 = i2;
	}
	
	/** return *a* state for given inventory levels i1 and i2 As int[]**/	//maybe not used
	public static int[] createState(StateSpace stateSpace, State state) {
		return stateSpace.generateStateSpace(stateSpace).get((state.i1-stateSpace.minInventory)*stateSpace.getStateSpaceSize() + state.i2-1);
	}
	
	public static void printState(State state) {
		System.out.println("state = <"+state.i1+", "+state.i2+">.");
	}
	
	/**test*
	public static void main(String args[]) {
		int i1 = 0;
		int i2 = 5;
		State state = new State(i1, i2);
		printState(state);		
	}*/
}
