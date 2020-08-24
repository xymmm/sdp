package LT_sdp_HashMap;

public class State {

	public int[] state;
	
	public State(int[] state) {
		this.state = state;
	}
	
	/** return a state given inventory levels i1 and i2 **/
	public static int[] createState(StateSpace stateSpace, int i1, int i2) {
		return stateSpace.generateStateSpace().get((i1-stateSpace.minInventory)*stateSpace.getStateSpaceSize() + i2-1);
	}
}
