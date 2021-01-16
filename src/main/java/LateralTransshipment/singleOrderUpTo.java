package LateralTransshipment;

import sdp.data.InstanceDouble;

public class singleOrderUpTo {
	
	public static void main(String args[]) {
		
		double fixedOrderingCost = 20;
		double unitCost = 1;
		double holdingCost = 0.25;
		double penaltyCost = 5;

		double tail = 0.0001;

		int minInventory = -20;
		int maxInventory = 30;
		int maxQuantity = 50;

		double stdParameter = 0.25;

		//int[] demandMean = {2,4,6,4};
		double[] demandMean = {4,6,8,6};
		
		boolean Normal = false;
		boolean initialOrder = true;

		InstanceDouble instance = new InstanceDouble(fixedOrderingCost, unitCost, holdingCost, penaltyCost,
				demandMean, tail, minInventory, maxInventory, maxQuantity, stdParameter);

		sS.sSsolution solution = sS.sS.solveInstance(instance, initialOrder, Normal);	//with initial order 

		sS.sS.presentsSresults(solution, instance);
		

	}

}
