package LT_sdp_HashMap;

public class State {

	public int[] state;
	public int	 maxInventory;
	public int	 minInventory;
	
	public State(int[] state, int maxInventory, int minInventory) {
		this.state = state;
		this.maxInventory = maxInventory;
		this.minInventory = minInventory;
	}
	
	/** state space **/
	public static int[] StateSpace(int maxInventory, int minInventory) {
		int[] inventory = new int[maxInventory - minInventory +1];
		for(int i=0; i<inventory.length; i++) {
			inventory[i] = i + minInventory;
		}
		return inventory;
	}
	
	/** create a state (inventory level of two locations) **/
	public static int[] createState(int[] inventory, int iterator1, int iterator2) {
		return new int[] {inventory[iterator2], inventory[iterator2]};
	}
}
