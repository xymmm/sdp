package reorderQuantitySystem;

import java.util.Arrays;

import sS.sS;
import sdp.data.InstanceDouble;

public class reorderPoint {

	/*
	 * Given an optimal schedule,
	 * 
	 * if Q[t] != 0, compute reorder point
	 * else, reorder point does not exist, denoted by the min inventory level
	 * 
	 * To compute reorder points, 
	 * 1. substitute Q[t] to an inventory and compute the cost with and without this replenishment quantity -> cost[i][t]
	 * 2. for those t with Q[t]!=0, find the switch point , and choose the lower one as the reorder point
	 * */

	public static int[] computeReorderPoint(InstanceDouble instance, sQsystemSolution solution) {
		int[] reorderPoint = new int [instance.getStages()];

		/**create array for inventory levels**/
		int[] inventory = new int [instance.maxInventory - instance.minInventory + 1];
		for(int i=0;i<inventory.length;i++) {
			inventory[i] = i + instance.minInventory;
		}
		
		double[][] demandProbabilitiesInput = solution.demandProbabilities;

		double[][] optimalCostByInventory = new double [inventory.length][instance.getStages()];
		boolean[][] optimalActionByInventory= new boolean[inventory.length][instance.getStages()];

		double[][] costOrder = new double[inventory.length][instance.getStages()];
		double[][] costNoOrder = new double[inventory.length][instance.getStages()];


		int[] optimalSchedule = solution.optimalSchedule;

		for(int t=instance.getStages()-1;t>=0;t--) { // Time			   
			for(int i=0;i<inventory.length;i++) { // Inventory 
					/** a = Q (given) **/
					double totalCostOrder = sS.computePurchasingCost(optimalSchedule[t], instance.fixedOrderingCost, instance.unitCost);				 
					double scenarioProb = 0;
					for(int d=0;d<demandProbabilitiesInput[t].length;d++) { // Demand
						if((inventory[i] + optimalSchedule[t] - d <= instance.maxInventory) && (inventory[i] + optimalSchedule[t] - d >= instance.minInventory)) {
							totalCostOrder += demandProbabilitiesInput[t][d]*(
									sS.computeImmediateCost(
											inventory[i], 
											optimalSchedule[t], 
											d, 
											instance.holdingCost, 
											instance.penaltyCost, 
											instance.fixedOrderingCost, 
											instance.unitCost)
									+ ((t==instance.getStages()-1) ? 0 : optimalCostByInventory[i+optimalSchedule[t]-d][t+1]) 
									);
							scenarioProb += demandProbabilitiesInput[t][d];
						}
					}
					totalCostOrder /= scenarioProb;
					costOrder[i][t] = totalCostOrder;

					/** a = 0**/
					double totalCostNoOrder = 0;
					scenarioProb = 0;
					for(int d=0;d<demandProbabilitiesInput[t].length;d++) { // Demand
						if((inventory[i] - d <= instance.maxInventory) && (inventory[i] - d >= instance.minInventory)) {
							totalCostNoOrder += demandProbabilitiesInput[t][d]*(
									sS.computeImmediateCost(
											inventory[i], 
											0, 
											d, 
											instance.holdingCost, 
											instance.penaltyCost, 
											instance.fixedOrderingCost, 
											instance.unitCost)
									+ ((t==instance.getStages()-1) ? 0 : optimalCostByInventory[i-d][t+1]) 
									);
							scenarioProb += demandProbabilitiesInput[t][d];
						}
					}
					totalCostNoOrder /= scenarioProb;
					costNoOrder[i][t] = totalCostNoOrder;

					optimalCostByInventory[i][t] = Math.min(totalCostNoOrder, totalCostOrder);
					optimalActionByInventory[i][t] = totalCostNoOrder < totalCostOrder ? false : true;
				
			}
		}
		
		/**if the optimal schedule does not contain a replenishment at period t**/
		for(int t=0; t<instance.getStages();t++) {
			if(solution.optimalSchedule[t] == 0) {
				reorderPoint[t] = instance.minInventory;
			}else {
				for(int i=50; i<inventory.length; i++) {  // Inventory   
					if(optimalActionByInventory[i][t] == false) {
						reorderPoint[t] = i + instance.minInventory;
						break;
					}
				}

			}
		}
		
		/*print all actions by inventory level and time period
		System.out.println();
		for(int i=0; i<inventory.length; i++) {
			System.out.print((i+instance.minInventory)+" \t");
			for(int t=0; t<instance.getStages(); t++) {
				System.out.print(optimalActionByInventory[i][t]+ "\t");
			}
			System.out.println();
		}
		
		
		//print cost by inventory level and time period
		System.out.println();
		for(int i=0; i<inventory.length; i++) {
			System.out.print((i+instance.minInventory)+" \t");
			for(int t=0; t<instance.getStages(); t++) {
				System.out.print(optimalCostByInventory[i][t]+ "\t");
			}
			System.out.println();
		}*/
		
		
		//print cost given a schedule
		//System.out.println("cost when computing reorder points is: "+optimalCostByInventory[(int) (instance.initialInventory - instance.minInventory)][0]);
		return reorderPoint;
	}
	
	
	public static void main(String args[]) {
		double fixedOrderingCost = 500;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 20;

		double tail = 0.00000001;

		int minInventory = -1500;
		int maxInventory = 1500;
		int maxQuantity = 800;

		double stdParameter = 0.25;

		//int[] demandMean = {200, 240, 260, 240};
		double[] demandMean = {76,27};
		
		InstanceDouble instance = new InstanceDouble(
				fixedOrderingCost,
				unitCost,
				holdingCost,
				penaltyCost,
				demandMean,
				tail,
				minInventory,
				maxInventory,
				maxQuantity,
				stdParameter
				);

		boolean Normal = false;
		sQsystemSolution sQsolution = reorderQuantitySystem.optimalSchedule_sQ.optimalSchedule_sQ(instance, Normal);
		
		System.out.println(sQsolution.optimalCost);
		System.out.println(Arrays.toString(sQsolution.optimalSchedule));
		
		int[] reorderPoint = computeReorderPoint(instance, sQsolution);
		System.out.println(Arrays.toString(reorderPoint));

	}
	

}
