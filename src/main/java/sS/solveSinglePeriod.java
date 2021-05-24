package sS;

import sdp.data.InstanceDouble;

public class solveSinglePeriod {
	
	public static double solveSinglePeriodsS(double fixedOrderingCost, double unitCost, double holdingCost, double penaltyCost, 
					double tail, int minInventory, int maxInventory, int maxQuantity, double stdParameter, 
					double[] demandMean,
					int currentTimeIdx, int inventoryLevel) {
		InstanceDouble instance = new InstanceDouble(fixedOrderingCost, unitCost, holdingCost, penaltyCost,
				demandMean, tail, minInventory, maxInventory, maxQuantity, stdParameter);
		sSsolution solution = sS.solveInstance(instance, true, false);	
		int i = inventoryLevel - minInventory;
		double orderQuantity = solution.optimalAction[i][currentTimeIdx];
		
		return orderQuantity;
	}
	
	



	




}
