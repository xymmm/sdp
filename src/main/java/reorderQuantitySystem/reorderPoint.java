package reorderQuantitySystem;

import java.util.Arrays;

import sS.sdp.sS;
import sdp.data.Instance;

public class reorderPoint {

	//TODO
	/*
	 * Given an optimal schedule,
	 * 
	 * if Q[t] != 0, compute reorder point
	 * else, reorder point does not exist, denoted by the max inventory level
	 * 
	 * To compute reorder points, 
	 * 1. substitute Q[t] to an inventory and compute the cost with and without this replenishment quantity -> cost[i][t]
	 * 2. for those t with Q[t]!=0, find the switch point , and choose the lower one as the reorder point
	 * */

	public static int[] computeReorderPoint(Instance instance, sQsystemSolution solution) {
		int[] reorderPoint = new int [instance.getStages()];

		/**create array for inventory levels**/
		int[] inventory = new int [instance.maxInventory - instance.minInventory + 1];
		for(int i=0;i<inventory.length;i++) {
			inventory[i] = i + instance.minInventory;
		}
		
		double[][] demandProbabilitiesInput = solution.demandProbabilities;

		double[][] optimalCostByInventory = new double [instance.getStages()][inventory.length];
		boolean[][] optimalActionByInventory= new boolean[instance.getStages()][inventory.length];

		double[][] costOrder = new double[instance.getStages()][inventory.length];
		double[][] costNoOrder = new double[instance.getStages()][inventory.length];


		int[] optimalSchedule = solution.optimalSchedule;

		for(int t=instance.getStages()-1;t>=0;t--) { // Time			   
			for(int i=0;i<inventory.length;i++) { // Inventory 
				/**reorder point exists**/
				if(optimalSchedule[t] != 0) {
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
									+ ((t==instance.getStages()-1) ? 0 : optimalCostByInventory[t+1][i+optimalSchedule[t]-d]) 
									);
							scenarioProb += demandProbabilitiesInput[t][d];
						}
					}
					totalCostOrder /= scenarioProb;
					costOrder[t][i] = totalCostOrder;

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
									+ ((t==instance.getStages()-1) ? 0 : optimalCostByInventory[t+1][i-d]) 
									);
							scenarioProb += demandProbabilitiesInput[t][d];
						}
					}
					totalCostNoOrder /= scenarioProb;
					costNoOrder[t][i] = totalCostNoOrder;

					optimalCostByInventory[t][i] = Math.min(totalCostNoOrder, totalCostOrder);
					optimalActionByInventory[t][i] = totalCostNoOrder < totalCostOrder ? false : true;
				}//if reorder point exists
				else {
					double totalCostNoOrder = 0;
					double scenarioProb = 0;
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
									+ ((t==instance.getStages()-1) ? 0 : optimalCostByInventory[t+1][i-d]) 
									);
							scenarioProb += demandProbabilitiesInput[t][d];
						}
					}
					totalCostNoOrder /= scenarioProb;
					costNoOrder[t][i] = totalCostNoOrder;

					optimalCostByInventory[t][i] = totalCostNoOrder;
					optimalActionByInventory[t][i] = false;
				}//reorder point does not exist
			}
		}
		
		/**if the optimal schedule does not contain a replenishment at period t**/
		for(int t=0; t<instance.getStages();t++) {
			if(solution.optimalSchedule[t] == 0) {
				reorderPoint[t] = instance.minInventory;
			}else {
				for(int i=0; i<inventory.length; i++) {  // Inventory   
					if(optimalActionByInventory[t][i] == false) {
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
				System.out.print(optimalActionByInventory[t][i]+ "\t");
			}
			System.out.println();
		}
		*/
		
		//print cost given a schedule
		System.out.println(optimalCostByInventory[0][instance.initialInventory - instance.minInventory]);
		return reorderPoint;
	}
	
	
	public static void main(String args[]) {
		double fixedOrderingCost = 10;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 5;

		double tail = 0.00000001;

		int minInventory = -50;
		int maxInventory = 50;
		int maxQuantity = 9;

		double stdParameter = 0.25;

		//int[] demandMean = {200, 240, 260, 240};
		int[] demandMean = {2,4,6,4};
		
		Instance instance = new Instance(
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

		sQsystemSolution sQtsolution = reorderQuantitySystem.optimalSchedule_sQt.optimalSchedule_sQt(instance);
		
		System.out.println(sQtsolution.optimalCost);
		System.out.println(Arrays.toString(sQtsolution.optimalSchedule));
		
		int[] reorderPoint = computeReorderPoint(instance, sQtsolution);
		System.out.println(Arrays.toString(reorderPoint));

	}



}
