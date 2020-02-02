package reorderQuantitySystem;

import java.util.Arrays;

import sQ.sdp.sQsolution;
import sS.sS;
import sdp.data.Instance;

public class singleScheduleCost {
	
	//consider sQ and sQt system altogether, classfied by a boolean variable 'timeDependent', which true for sQt
	
	//build one-dimension array Q[timeHorizon]
	//for sQ, Q[0] = ... = Q[T-1] = a single constant, and go through all possible Q to find an optimal one
	//for sQt, Q[t] is given by the generator
	
	//therefore, loop contains 't for Q[t], i for inventory, d for demand'
	
	//another code will be applied for the computation of sQ and sQt optimal quantity.
	
	public static double[][] singleScheduleCost (Instance instance, int[] Q, double[][] demandProbabilities) {
		
		/**create array for inventory levels**/
		int[] inventory = new int [instance.maxInventory - instance.minInventory + 1];
		for(int i=0;i<inventory.length;i++) {
			inventory[i] = i + instance.minInventory;
		}
		
		/**totalCost[g][i][t] stores the values of costs for an inventory level at a stage with a given (as an input) quantity schedule**/
		int timeHorizon = instance.getStages();
		double totalCost[][] = new double[inventory.length][timeHorizon];		
		for(int t=instance.getStages()-1; t>=0; t--) {			
			for(int i=0; i<inventory.length;i++) {
				//minCost[i] = totalCost[i][0];
				totalCost[i][t] = sS.computePurchasingCost(Q[t], instance.fixedOrderingCost, instance.unitCost);
				
				double scenarioProb = 0;
				for(int d=0; d<demandProbabilities[t].length;d++) {
					if((inventory[i] + Q[t] - d <= instance.maxInventory) && (inventory[i] + Q[t] - d >= instance.minInventory)) {
						totalCost[i][t] += demandProbabilities[t][d]*(
								sS.computeImmediateCost(
										inventory[i], 
										Q[t], 
										d, 
										instance.holdingCost, 
										instance.penaltyCost, 
										instance.fixedOrderingCost, 
										instance.unitCost)
								+((t==instance.getStages()-1) ? 0 : totalCost[i+Q[t]-d][t+1])
								);
						scenarioProb += demandProbabilities[t][d];
					}//if
				}//d
				totalCost[i][t] /= scenarioProb;					
			}//i
		}//t
		
		return totalCost;
	}
	
	/*
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
		
		double demandProbabilities [][] = sS.computeDemandProbability(instance.demandMean, instance.maxDemand, instance.tail);//Poisson

		
		//sQt
		int Qt[] = {8,0,9,0};
		double[][] costsQt = singleScheduleCost(instance, Qt, demandProbabilities);
		System.out.println("Optimal cost under (s,Qt) policy is: "+costsQt[instance.initialInventory - instance.minInventory][0]);

		//sQ
		System.out.println();
		int Q[] = {9,0,9,0};
		double[][] costsQ = singleScheduleCost(instance, Q, demandProbabilities);
		System.out.println("Optimal cost under (s,Q) policy is: "+costsQ[instance.initialInventory - instance.minInventory][0]);
		
	}
*/


}
