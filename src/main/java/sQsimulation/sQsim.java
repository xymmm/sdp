package sQsimulation;

import umontreal.ssj.probdist.PoissonDist;
import umontreal.ssj.randvar.PoissonGen;
import umontreal.ssj.randvar.RandomVariateGenInt;
import umontreal.ssj.rng.MRG32k3a;
import umontreal.ssj.rng.RandomStream;
import umontreal.ssj.util.Chrono;

public class sQsim {
	/**
	 * Simulating an (s,Q) inventory system consists of the following serial events in one period:
	 * 
	 * 1. At the beginning of the period, check inventory and  determine the action decision, reorder or not;
	 * 2. incur the purchasing cost based on the action decision;
	 * 3. order arrives, and update the inventory level (1);
	 * 4. generate, check and meet the demand;
	 * 5. update the inventory level (2), and incur holding or ordering cost
	 * 
	 * These events are realized by a series of corresponding methods as follows.
	 * **/


	/** 1. check inventory and return action decision **/
	static int checkInventory(int reorderPoint, int inventoryLevel) {
		//compare the current inventory level and reorder point, 1 represents reorder 
		return (inventoryLevel<reorderPoint)? 1 : 0;
	}
	
	/** 2. compute purchasing cost according to action decision **/
	static double computePurchasingCost(int actionDecision, int currentStageIndex, sQsimInstance sQsimInstance) {
		return actionDecision*(
				sQsimInstance.fixedOrderingCost 
				+ sQsimInstance.unitCost*sQsimInstance.getActionQuantity(currentStageIndex)
				);
	}
	
	/** 3(1) & 5(2) update inventory level**/
	static int updateInventoryLevel(int inventoryLevel, int inventoryAlteration) {
		return inventoryLevel + inventoryAlteration;
	}
	
	/** 6. generate Poisson random number as demand **/
	static int generateDemand(int inventoryLevel, int actionDecision, sQsimInstance sQsimInstance, int currentStageIndex) {
		RandomVariateGenInt genDemand;
		RandomStream streamDemand = new MRG32k3a();
		genDemand = new PoissonGen(streamDemand, new PoissonDist(sQsimInstance.demandMean[currentStageIndex]));
		int demand = genDemand.nextInt();

		while(checkDemand(inventoryLevel, actionDecision, sQsimInstance, demand, currentStageIndex) == false) {
			demand = genDemand.nextInt();
		}
		return -demand;
		/* without ssj:
		 * 
		 * int demand = getPoissonVariable(sQsimInstance.getDemandMean(currentStageIndex));
		 * while(check... == false){
		 * 		demand = getPoissonVariable(sQsimInstance.getDemandMean(currentStageIndex));}
		 * return -demand
		 * **/
		//return -getPoissonVariable(sQsimInstance.getDemandMean(currentStageIndex));
	}
	static boolean checkDemand(int inventoryLevel, int actionDecision, sQsimInstance sQsimInstance, int demand, int currentStageIndex) {
		if((inventoryLevel + actionDecision*sQsimInstance.getActionQuantity(currentStageIndex)-demand >= sQsimInstance.minInventory)
			&& (inventoryLevel + actionDecision*sQsimInstance.getActionQuantity(currentStageIndex)-demand <= sQsimInstance.maxInventory)
		  ){
			return true;
		}else {
			return false;
		}
	}
	/*
	private static int getPoissonVariable(double lamda) {
		int x = 0;
		double y = Math.random(), cdf = getPoissonProbability(x, lamda);
		while (cdf < y) {
			x++;
			cdf += getPoissonProbability(x, lamda);
		}
		return x;
	}
	private static double getPoissonProbability(int k, double lamda) {
		double c = Math.exp(-lamda), sum = 1;
		for (int i = 1; i <= k; i++) {
			sum *= lamda / i;
		}
		return sum * c;
	}
	*/
	
	/** 5. compute holding or penalty cost **/
	static double computeClosingCost(int inventoryLevel, sQsimInstance sQsimInstance) {
		if(inventoryLevel >= 0) {//return holding cost
			return inventoryLevel*sQsimInstance.holdingCost;
		}else {//return penalty cost
			return -inventoryLevel*sQsimInstance.penaltyCost;
		}
	}
	
	public static double simulatesQinstanceOneRun(sQsimInstance sQsimInstance) {

		int inventoryLevel = sQsimInstance.getInitialInventory();
		
		double cost = 0;
		int actionDecision;
		
		int currentStageIndex = 0;
		do {
			System.out.println("At stage "+(currentStageIndex + 1));
			System.out.println("Current inventory level is "+inventoryLevel);
			
			//1 & 2 check inventory
			actionDecision = checkInventory(sQsimInstance.reorderPoint[currentStageIndex], inventoryLevel);
			System.out.println((actionDecision == 1) ? "Replenishment order placed. ":"No order placed. ");
			
			//2. compute purchasing cost
			cost += computePurchasingCost(actionDecision, currentStageIndex, sQsimInstance);
			
			//3. update inventory level
			inventoryLevel = updateInventoryLevel(inventoryLevel, actionDecision*sQsimInstance.getActionQuantity(currentStageIndex));
			System.out.println("Updated inventory level is "+inventoryLevel);
			
			//4. generate, check and meet demand
			int demand = generateDemand(inventoryLevel, actionDecision, sQsimInstance, currentStageIndex); // as a negative
			System.out.println("Demand in this stage is "+(-demand));
			
			//update inventory level
			inventoryLevel = updateInventoryLevel(inventoryLevel, demand);
			System.out.println("Inventory level after meeting demand is "+inventoryLevel);
			
			//5. compute closing cost
			cost += computeClosingCost(inventoryLevel, sQsimInstance);
			System.out.println("Cumulative cost is "+cost);
			
			currentStageIndex++;
			System.out.println();
		}while(
				currentStageIndex < sQsimInstance.getStages()
				);
		System.out.println("=============================");
		return cost;
	}
	
	/** multiple run times **/
	public static void simulationsQinstanceRuns(sQsimInstance sQsimInstance, int count) {
		for(int i=0; i<count; i++) {
			sQsimInstance.statCost.add(simulatesQinstanceOneRun(sQsimInstance));
		}
	}
	
	/* print simulation results *
	static void printSimResults(double[] cost, int count) {
		System.out.println("count    total cost");
		System.out.println("-------------------");
		for(int c = 0; c<count; c++) {
			System.out.println((c+1)+"    "+cost[c]);
		}
	}
	
	static double getAverage(double[] cost) {
		double sum = 0;
		for(int i=0;i<cost.length;i++) {
			sum += cost[i];
		}
		return sum/cost.length;
	}
	*/

	public static void main(String[] args) {

		/** declare instance parameters **/
		double fixedOrderingCost = 100;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 10;
		int[] demandMean = {20,40,60,40};

		double tail = 0.00000001;

		int minInventory = -500;
		int maxInventory = 500;
		
		int[] reorderPoint = {13,33,54,24};
		int[] actionQuantity = {83, 83, 83, 83};

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
		
		Chrono timer = new Chrono();
		
		int count = 500;
		//sQsim.simulatesQinstanceOneRun(sQsystem);
		sQsim.simulationsQinstanceRuns(sQsystem, count);
		
		sQsystem.statCost.setConfidenceIntervalStudent();
		System.out.println(sQsystem.statCost.report(0.9, 3));
		System.out.println("Total CPU time: "+timer.format());

		/*
		//simulation times
		int count = 500;
		double[] cost = new double [count];

		for(int i = 0; i<count; i++) {
			System.out.println("========== Run time = "+(i+1)+" ==========");
			cost[i] = simulatesQinstanceOneRun(sQsystem);
		}
		
		printSimResults(cost, count);
		double average = getAverage(cost);
		System.out.println("Average cost of "+count+" times simulations is "+average);
		**/

	}

}
