package reorderQuantitySystem;

import java.util.Arrays;

import sS.sS;
import sdp.data.InstanceDouble;

public class reorderPoint_differene {

	/**
	 * compute the cost of difference with no initial reorder
	 * the reorder point is the first inventory level with the cost smaller than the ordering cost
	 * 
	 * the cost is with no initial order in the first period
	 **/

	public static int computeReorderPointByDifference(InstanceDouble instance, sQsystemSolution solution, int[] Q) {
		int reorderPoint=0;

		/**create array for inventory levels**/
		int[] inventory = new int [instance.maxInventory - instance.minInventory + 1];
		for(int i=0;i<inventory.length;i++) {
			inventory[i] = i + instance.minInventory;
		}

		double[][] demandProbabilities = solution.demandProbabilities;
		double[][] optimalCostByInventory = new double [instance.getStages()][inventory.length];
		
		double[][] costOrder = new double[instance.getStages()][inventory.length];
		double[][] costNoOrder = new double[instance.getStages()][inventory.length];

		for(int t=instance.getStages()-1; t>=0; t--) {
			for(int i=0; i<inventory.length;i++) {

				/** a = Q (given) **/
				double totalCostOrder = sS.computePurchasingCost(Q[t], instance.fixedOrderingCost, instance.unitCost);				 
				double scenarioProb = 0;
				for(int d=0;d<demandProbabilities[t].length;d++) { // Demand

					int a=(t==0)?0:Q[t];

					if((inventory[i] + a - d <= instance.maxInventory) && (inventory[i] + a - d >= instance.minInventory)) {
						totalCostOrder += demandProbabilities[t][d]*(
								sS.computeImmediateCost(
										inventory[i], 
										a, 
										d, 
										instance.holdingCost, 
										instance.penaltyCost, 
										instance.fixedOrderingCost, 
										instance.unitCost)
								+ ((t==instance.getStages()-1) ? 0 : optimalCostByInventory[t+1][i+a-d]) 
								);
						scenarioProb += demandProbabilities[t][d];
					}
				}
				totalCostOrder /= scenarioProb;
				costOrder[t][i] = totalCostOrder;

				/** a = 0**/
				double totalCostNoOrder = 0;
				scenarioProb = 0;
				for(int d=0;d<demandProbabilities[t].length;d++) { // Demand
					if((inventory[i] - d <= instance.maxInventory) && (inventory[i] - d >= instance.minInventory)) {
						totalCostNoOrder += demandProbabilities[t][d]*(
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
						scenarioProb += demandProbabilities[t][d];
					}
				}
				totalCostNoOrder /= scenarioProb;
				costNoOrder[t][i] = totalCostNoOrder;
				
				optimalCostByInventory[t][i] = Math.min(totalCostNoOrder, totalCostOrder);

			}//i
		}//t
		
		for(int i=0; i<inventory.length; i++) {
			System.out.println((i+instance.minInventory)+"\t"+optimalCostByInventory[0][i]);
		}

		int differenceLength = instance.maxInventory - instance.minInventory + 1 - Q[0] ;
		double[] costDifference = new double[differenceLength];

		double targetCost = instance.fixedOrderingCost + Q[0] * instance.unitCost;

		for(int f=0; f<costDifference.length;f++) {
			costDifference[f] = optimalCostByInventory[0][f] - optimalCostByInventory[0][f+Q[0]];
			System.out.println((f+instance.minInventory)+"\t"+costDifference[f]);
		}

		for(int f=0; f<costDifference.length;f++) {
			if(costDifference[f] < targetCost) {
				reorderPoint = f+instance.minInventory;
				break;
			}
		}
		

		return reorderPoint;
	}

	public static void main(String[] args) {

		double fixedCost = 5;
		double holdingCost = 1;
		double penaltyCost = 3;
		double unitCost = 0;

		double tail = 0.00000001;

		int minInventory = -50;
		int maxInventory = 50;
		int maxQuantity = 9;

		double stdParameter = 0.25;

		double[] demandMean = {2,1,5,3};

		InstanceDouble instance = new InstanceDouble(
				fixedCost,
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
		sQsystemSolution sQtsolution = reorderQuantitySystem.optimalSchedule_sQt.optimalSchedule_sQt(instance, false);
		int[] Q = sQtsolution.optimalSchedule;


		double[][] demandMeanInput = sdp.util.demandMeanInput.createDemandMeanInput(demandMean);
		int[][] Qinput = sdp.util.demandMeanInput.createDemandMeanInputInt(Q);
		int[] reorderPoints = new int[instance.demandMean.length];

		for(int t=0; t<demandMean.length; t++) {
			System.out.println("t="+(t+1));
			if(Q[t] == 0) {
				reorderPoints[t] = instance.minInventory;
			}else {
				InstanceDouble instancePeriod = new InstanceDouble(
						fixedCost,
						unitCost,
						holdingCost,
						penaltyCost,
						demandMeanInput[t],
						tail,
						minInventory,
						maxInventory,
						maxQuantity,
						stdParameter
						);
				reorderPoints[t] = computeReorderPointByDifference(instancePeriod, sQtsolution, Qinput[t]);
			}


		}




		System.out.println(sQtsolution.optimalCost);
		System.out.println("Q "+Arrays.toString(sQtsolution.optimalSchedule));
		System.out.println("reorder point "+Arrays.toString(reorderPoints));


	}
}
