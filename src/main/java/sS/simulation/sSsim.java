package sS.simulation;


import umontreal.ssj.probdist.PoissonDist;
import umontreal.ssj.randvar.PoissonGen;
import umontreal.ssj.randvar.RandomVariateGenInt;
import umontreal.ssj.rng.MRG32k3a;
import umontreal.ssj.rng.RandomStream;
import umontreal.ssj.util.Chrono;

public class sSsim {
	
	/** 1. check inventory and return action decision **/
	static int checkInventory(int[] reorderPoint, int inventoryLevel, int currentStageIndex) {
		//compare the current inventory level and reorder point, 1 represents reorder 
		return (inventoryLevel<reorderPoint[currentStageIndex])? 1 : 0;
	}
	
	/** 2. compute purchasing cost according to action decision **/
	static double computePurchasingCost(int inventoryLevel, int actionDecision, int currentStageIndex, sSsimInstance sSsimInstance) {
		return actionDecision*(
				sSsimInstance.fixedOrderingCost 
				+ sSsimInstance.unitCost*sSsimInstance.getActionQuantity(currentStageIndex, inventoryLevel) //quantity is computed
				);
	}
	
	/** 3(1) update inventory level - replenishment arrives **/
	static int orderArrive(sSsimInstance sSsimInstance, int currentStageIndex) {
		return sSsimInstance.actionS[currentStageIndex];
	}
	
	/** 5(2) update inventory level - meet demand**/
	static int meetDemand(int inventoryLevel, int demand) {
		return inventoryLevel + demand;
	}
	
	/** 5. generate Poisson random number as demand **/
	static int generateDemand(int inventoryLevel, int actionDecision, sSsimInstance sSsimInstance, int currentStageIndex) {
		/*
		int demand = getPoissonVariable(sSsimInstance.demandMean[currentStageIndex]);
		while(checkDemand(inventoryLevel, sSsimInstance, demand) == false) {
			demand = getPoissonVariable(sSsimInstance.demandMean[currentStageIndex]);
		}
		return -demand;
		*/
		  RandomVariateGenInt genDemand;
		  RandomStream streamDemand = new MRG32k3a();
		  genDemand = new PoissonGen(streamDemand, new PoissonDist(sSsimInstance.demandMean[currentStageIndex])); 
		  int demand = genDemand.nextInt();
		  
		  while(checkDemand(inventoryLevel, sSsimInstance, demand) == false) { 
			  demand = genDemand.nextInt(); 
		  } 
		  return -demand;
	}
	static boolean checkDemand(int inventoryLevel, sSsimInstance sSsimInstance, int demand) {
		if(inventoryLevel - demand >= sSsimInstance.minInventory) {
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
	
	
	/** 6. compute holding or penalty cost **/
	static double computeClosingCost(int inventoryLevel, sSsimInstance sSsimInstance) {
		if(inventoryLevel >= 0) {//return holding cost
			return inventoryLevel*sSsimInstance.holdingCost;
		}else {//return penalty cost
			return -inventoryLevel*sSsimInstance.penaltyCost;
		}
	}
	
	
	public static double simulatesQinstanceOneRun(sSsimInstance sSsimInstance, boolean print) {

		int inventoryLevel = sSsimInstance.getInitialInventory();
		double cost = 0;
		int actionDecision;
		
		int currentStageIndex = 0;
		do {
			if(print == true) System.out.println("At stage "+(currentStageIndex + 1));
			if(print == true) System.out.println("Current inventory level is "+inventoryLevel);
			
			//1 & 2 check inventory
			actionDecision = checkInventory(sSsimInstance.reorderPoint, inventoryLevel, currentStageIndex);
			if(print == true) System.out.println((actionDecision == 1) ? "Replenishment order placed. ":"No order placed. ");
			
			//2. compute purchasing cost
			cost += computePurchasingCost(inventoryLevel, actionDecision, currentStageIndex, sSsimInstance);
			
			//3. update inventory level
			if(actionDecision == 1) inventoryLevel = orderArrive(sSsimInstance, currentStageIndex);
			if(print == true) System.out.println("Updated inventory level is "+inventoryLevel);
			
			//4. generate, check and meet demand
			int demand = generateDemand(inventoryLevel, actionDecision, sSsimInstance, currentStageIndex); // as a negative
			if(print == true) System.out.println("Demand in this stage is "+(-demand));
			
			//update inventory level
			inventoryLevel = meetDemand(inventoryLevel, demand);
			if(print == true) System.out.println("Inventory level after meeting demand is "+inventoryLevel);
			
			//5. compute closing cost
			cost += computeClosingCost(inventoryLevel, sSsimInstance);
			if(print == true) System.out.println("Cumulative cost is "+cost);
			
			currentStageIndex++;
			if(print == true) System.out.println();
		}while(
				currentStageIndex < sSsimInstance.getStages()
				);
		//System.out.println(cost);
		if(print == true) System.out.println("=============================");
		return cost;
	}
	
	/** multiple run times **/
	public static void simulationsQinstanceRuns(sSsimInstance sSsimInstance, int count) {
		for(int i=0; i<count; i++) {
			sSsimInstance.statCost.add(simulatesQinstanceOneRun(sSsimInstance,false));
		}
	}
	
	public static void main(String[] args) {

		/** declare instance parameters **/
		double fixedOrderingCost = 100;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 10;
		int[] demandMean = {20,40,60,40};

		int minInventory = -500;
		int maxInventory = 500;
		
		int[] reorderPoint = {15, 28, 55, 28};
		int[] actionS = {67, 49, 109, 49};

		sSsimInstance sSsystem = new sSsimInstance(
				fixedOrderingCost,
				unitCost,
				holdingCost,
				penaltyCost,
				demandMean,
				minInventory,
				maxInventory,
				actionS,
				reorderPoint
				);	
		
		
		Chrono timer = new Chrono();
		
		int count = 50000;
		sSsim.simulationsQinstanceRuns(sSsystem, count);
		
		sSsystem.statCost.setConfidenceIntervalStudent();
		System.out.println(sSsystem.statCost.report(0.9, 3));
		System.out.println("Total CPU time: "+timer.format());
		
		
		//simulatesQinstanceOneRun(sSsystem, true);

	}

}
