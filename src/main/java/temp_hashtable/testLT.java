package temp_hashtable;

public class testLT {
	
	public static int maxInventory;
	public static int minInventory;
	private static int stateSpaceSize = maxInventory - minInventory + 1;

	private static CostArray[] hashArray = new CostArray[stateSpaceSize];
	
	static class CostArray{
		int[] key;	//{initial state, final state}
		double data;
		public CostArray(double data, int[] key) {
			this.data = data;
			this.key = key;		
		}
	}
	
	static double hashCode(double cost1, double cost2, double cost0) {
		return cost0+cost1+cost2;
	}
	

	
	
}
