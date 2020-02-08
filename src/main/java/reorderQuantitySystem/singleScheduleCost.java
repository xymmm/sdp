package reorderQuantitySystem;

import java.util.Arrays;

import sQ.sdp.sQsolution;
import sS.sS;
import sdp.data.Instance;
import sdp.data.InstanceDouble;

public class singleScheduleCost {
	
	//consider sQ and sQt system altogether, classfied by a boolean variable 'timeDependent', which true for sQt
	
	//build one-dimension array Q[timeHorizon]
	//for sQ, Q[0] = ... = Q[T-1] = a single constant, and go through all possible Q to find an optimal one
	//for sQt, Q[t] is given by the generator

	//therefore, loop contains 't for Q[t], i for inventory, d for demand'

	//another code will be applied for the computation of sQ and sQt optimal quantity.

	public static double[][] singleScheduleCost (InstanceDouble instance, int[] Q, double[][] demandProbabilities) {

		/**create array for inventory levels**/
		double[] inventory = new double [instance.maxInventory - instance.minInventory + 1];
		for(int i=0;i<inventory.length;i++) {
			inventory[i] = i + instance.minInventory;
		}

		double[][] optimalCostByInventory = new double [inventory.length][instance.getStages()];

		double[][] costOrder = new double[inventory.length][instance.getStages()];
		double[][] costNoOrder = new double[inventory.length][instance.getStages()];

		/**totalCost[g][i][t] stores the values of costs for an inventory level at a stage with a given (as an input) quantity schedule**/
		int timeHorizon = instance.getStages();

		for(int t=instance.getStages()-1; t>=0; t--) {			
			for(int i=0; i<inventory.length;i++) {

				/** a = Q (given) **/
				double totalCostOrder = sS.computePurchasingCost(Q[t], instance.fixedOrderingCost, instance.unitCost);				 
				double scenarioProb = 0;
				for(int d=0;d<demandProbabilities[t].length;d++) { // Demand
					if((inventory[i] + Q[t] - d <= instance.maxInventory) && (inventory[i] + Q[t] - d >= instance.minInventory)) {
						totalCostOrder += demandProbabilities[t][d]*(
								sS.computeImmediateCost(
										inventory[i], 
										Q[t], 
										d, 
										instance.holdingCost, 
										instance.penaltyCost, 
										instance.fixedOrderingCost, 
										instance.unitCost)
								+ ((t==instance.getStages()-1) ? 0 : optimalCostByInventory[i+Q[t]-d][t+1]) 
								);
						scenarioProb += demandProbabilities[t][d];
					}
				}
				totalCostOrder /= scenarioProb;
				costOrder[i][t] = totalCostOrder;

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
								+ ((t==instance.getStages()-1) ? 0 : optimalCostByInventory[i-d][t+1]) 
								);
						scenarioProb += demandProbabilities[t][d];
					}
				}
				totalCostNoOrder /= scenarioProb;
				costNoOrder[i][t] = totalCostNoOrder;

				optimalCostByInventory[i][t] = Math.min(totalCostNoOrder, totalCostOrder);
			}//i
		}//t
		
		return optimalCostByInventory;
	}
	
	
	public static void main(String args[]) {
		
		//create instance
		double fixedOrderingCost = 10;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 5;
		double tail = 0.00000001;

		int minInventory = -100;
		int maxInventory = 100;
		int maxQuantity = 9;

		double stdParameter = 0.25;

		//int[] demandMean = {20, 40, 60, 40};
		double[] demandMean = {1,2,1.5,3};
		
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
		
		double demandProbabilities [][] = sS.computeDemandProbability(instance.demandMean, instance.maxDemand, instance.tail);//Poisson

		
		//sQt
		//int Qt[] = {8,0,9,0};
		//double[][] costsQt = singleScheduleCost(instance, Qt, demandProbabilities);
		//System.out.println("Optimal cost under (s,Qt) policy is: "+costsQt[instance.initialInventory - instance.minInventory][0]);

		//sQ
		System.out.println();
		int Q[] = {9,0,9,0};
		double[][] costsQ = singleScheduleCost(instance, Q, demandProbabilities);
		System.out.println("Optimal cost under (s,Q) policy is: "+costsQ[(int) (instance.initialInventory - instance.minInventory)][0]);
		
		/*
		for(int i=0; i<instance.maxInventory - instance.minInventory + 1; i++) {
			System.out.print((i+instance.minInventory)+" \t");
			for(int t=0; t<instance.getStages(); t++) {
				System.out.print(costsQ[i][t]+ "\t");
			}
			System.out.println();
		}*/
	}



}
