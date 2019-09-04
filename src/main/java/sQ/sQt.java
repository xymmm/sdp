package sQ;

import sQsimulation.sQsim;
import sQsimulation.sQsimInstance;
import sdp.SDP;
import sdp.data.Instance;
import umontreal.ssj.util.Chrono;

public class sQt {

	/***** Output methods *****/
	
	/** print reorder points s **/
	
	/** print Qt **/
	
	/** print optimal cost of each inventory level at each periods **/
	
	/** print optimal costs **/
	static void printOptimalCost(int inventoryIndex, Instance instance, sQtsolution sQtsolution) {
		//for(int i=0; i<sQtsolution.inventory.length;i++) {
			System.out.println("inventoryLevel = "+sQtsolution.inventory[inventoryIndex]);
			System.out.println("t = 0 ");
			for(int a=0; a<instance.maxQuantity+1; a++) {
				System.out.print(sQtsolution.totalCost[0][inventoryIndex][a]+" ");
			}System.out.println();
			System.out.println("t = 1 ");
			for(int a=0; a<instance.maxQuantity+1; a++) {
				System.out.print(sQtsolution.totalCost[1][inventoryIndex][a]+" ");
			}System.out.println();
			System.out.println("t = 2 ");
			for(int a=0; a<instance.maxQuantity+1; a++) {
				System.out.print(sQtsolution.totalCost[2][inventoryIndex][a]+" ");
			}System.out.println();
			System.out.println("t = 3 ");
			for(int a=0; a<instance.maxQuantity+1; a++) {
				System.out.print(sQtsolution.totalCost[3][inventoryIndex][a]+" ");
			}System.out.println();
		//}
	}
	
	/** print optimal costs **/
	static void printOptimalAction(int inventoryIndex, Instance instance, sQtsolution sQtsolution) {
		//for(int i=0; i<sQtsolution.inventory.length;i++) {
			System.out.println("inventoryLevel = "+sQtsolution.inventory[inventoryIndex]);
			System.out.println("t = 0 ");
			for(int a=0; a<instance.maxQuantity+1; a++) {
				System.out.print(sQtsolution.optimalAction[0][inventoryIndex][a]+" ");
			}System.out.println();
			System.out.println("t = 1 ");
			for(int a=0; a<instance.maxQuantity+1; a++) {
				System.out.print(sQtsolution.optimalAction[1][inventoryIndex][a]+" ");
			}System.out.println();
			System.out.println("t = 2 ");
			for(int a=0; a<instance.maxQuantity+1; a++) {
				System.out.print(sQtsolution.optimalAction[2][inventoryIndex][a]+" ");
			}System.out.println();
			System.out.println("t = 3 ");
			for(int a=0; a<instance.maxQuantity+1; a++) {
				System.out.print(sQtsolution.optimalAction[3][inventoryIndex][a]+" ");
			}System.out.println();
		//}
	}
	
	/** plot costs**/
	
	/***** main computation *****/
	public static sQtsolution solvesQt(Instance instance) {
		
		//working matrix
		int[] inventory = new int [instance.maxInventory - instance.minInventory + 1];
		for(int i=0;i<inventory.length;i++) {
			inventory[i] = i + instance.minInventory;
		}
		double demandProbabilities [][] = SDP.computeDemandProbability(instance.demandMean, instance.maxDemand, instance.tail);
		
		//different dimension index from sdp or sQ, as period takes the first.
		double totalCost[][][] = new double[instance.getStages()][inventory.length][instance.maxQuantity+1];
		boolean optimalAction[][][] = new boolean [instance.getStages()][inventory.length][instance.maxQuantity+1];

		for(int t=0; t<instance.getStages(); t++) {//periods
			for(int a=0; a<instance.maxQuantity+1;a++) {//a
				for(int i=0; i<inventory.length;i++) {//inventory
					/** a>0 **/
					double totalCostOrder = SDP.computePurchasingCost(a, instance.fixedOrderingCost, instance.unitCost);
					double scenarioProb = 0;
					for(int d=0; d<demandProbabilities[t].length;d++) {
						if((inventory[i] + a - d <= instance.maxInventory) && (inventory[i] + a - d >= instance.minInventory)) {
							totalCostOrder += demandProbabilities[t][d] * (
									SDP.computeImmediateCost(inventory[i], 
															 a, 
															 d, 
															 instance.holdingCost, 
															 instance.penaltyCost, 
															 instance.fixedOrderingCost, 
															 instance.unitCost)
									+ ((t==instance.getStages()-1) ? 0 : totalCost[t+1][i+a-d][a]) 
									);
							scenarioProb += demandProbabilities[t][d];
						}
					}
					totalCostOrder /= scenarioProb;
					
					/** a=0 **/
					double totalCostNoOrder = 0;
					scenarioProb = 0;
					for(int d=0;d<demandProbabilities[t].length;d++) { // Demand
						if((inventory[i] - d <= instance.maxInventory) && (inventory[i] - d >= instance.minInventory)) {
							totalCostNoOrder += demandProbabilities[t][d]*(
													SDP.computeImmediateCost(
															inventory[i], 
															0, 
															d, 
															instance.holdingCost, 
															instance.penaltyCost, 
															instance.fixedOrderingCost, 
															instance.unitCost)
													+ ((t==instance.getStages()-1) ? 0 : totalCost[t+1][i-d][a]) 
													);
							scenarioProb += demandProbabilities[t][d];
						}								
					}
					totalCostNoOrder /= scenarioProb;
					//System.out.println(totalCostNoOrder + " "+ totalCostOrder);
					totalCost[t][i][a] = Math.min(totalCostNoOrder, totalCostOrder);
					optimalAction[t][i][a] = (totalCostNoOrder <= totalCostOrder) ? false:true;
				}
			}
		}
		
		return new sQtsolution(totalCost, optimalAction, inventory);
	}
	
	public static void main(String[] args) {
		
		Chrono timer = new Chrono();

		/*** Problem instance ***/
		double fixedOrderingCost = 10;//100;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 10;
		int[] demandMean = {2,4,6,4};//{20,40,60,40};

		/** SDP boundary conditions **/
		double tail = 0.00000001;

		int minInventory = -20;//-500;
		int maxInventory = 20;//500;
		int maxQuantity = 4;//500;

		Instance instance = new Instance(
				fixedOrderingCost,
				unitCost,
				holdingCost,
				penaltyCost,
				demandMean,
				tail,
				minInventory,
				maxInventory,
				maxQuantity
				);

		/** Solve the classic instance **/
		sQtsolution sQtsolution = solvesQt(instance);
		
		printOptimalCost((-instance.minInventory), instance, sQtsolution);
		printOptimalAction((-instance.minInventory), instance, sQtsolution);
		
		/** simulations **/
		/*
		System.out.println();
		System.out.println("Simulations:");
		int[] reorderPoint = sQtsolution.getssQt(instance);
		int[] actionQuantity = sQtsolution.getQt(instance);
		sQsimInstance sQsystem = new sQsimInstance(
				fixedOrderingCost,
				unitCost,
				holdingCost,
				penaltyCost,
				demandMean,
				tail,
				minInventory,
				maxInventory,
				actionQuantity,
				reorderPoint
				);

		int count = 50000;
		sQsim.simulationsQinstanceRuns(sQsystem, count);

		sQsystem.statCost.setConfidenceIntervalStudent();
		System.out.println(sQsystem.statCost.report(0.9, 3));
		*/
		System.out.println("Total CPU time: "+timer.format());

		}//main
	
}//class
