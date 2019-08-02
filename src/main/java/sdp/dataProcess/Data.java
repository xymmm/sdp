package sdp.dataProcess;


public class Data {
	/**
	 * This class contains all input data for the problem including:
	 */
	
	// the mean of demand distribution: demandMean
	public static int[] demandMean = {20,40,60,40};
	// the standard deviation coefficient 
	//public static double stdCoefficient = 0.25;
	public static double tail = 0.0001;
	
	
	

	// the maximum value of demand
	public static int maxDemand = 200;
	
	// the maximum storage of the inventory
	public static int maxInventory = 250;
	
	// the maximum value of an replenishment order
	public static int maxQuantity = 250;
	
	// the number of stages
	public static int stage = 4;
	
	/*
	public static int maxDemand = 2;
	public static int maxInventory = 3;
	public static int maxQuantity = 4;
	public static int stage = 3;
	*/
	
	
	// unit costs
	public static double fixCost = 100;
	public static double unitCost = 0;
	public static double holdingCost = 1;
	public static double penaltyCost = 10;

	/*
	public static double fixCost = 3;
	public static double unitCost = 2;
	public static double holdingCost = 1;
	public static double penaltyCost = 2;
	*/
}
