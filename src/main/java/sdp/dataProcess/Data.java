package sdp.dataProcess;


public class Data {
	/**
	 * This class contains all input data for the problem including:
	 */
	
	// the mean of demand distribution: demandMean
	public static int[] demandMean = {2,4,6,4};
	// the standard deviation coefficient 
	public static double stdCoefficient = 0.25;

	// the maximum value of demand
	public static int maxDemand = 15;
	
	// the maximum storage of the inventory
	public static int maxInventory = 15;
	
	// the maximum value of an replenishment order
	public static int maxQuantity = 15;
	
	// the number of stages
	public static int stage = 4;
	
	// unit costs
	public static double fixCost = 10;
	public static double unitCost = 0;
	public static double holdingCost = 1;
	public static double penaltyCost = 5;

}
