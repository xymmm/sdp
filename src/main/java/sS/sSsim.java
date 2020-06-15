package sS;


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
	static MRG32k3a randomStream = new MRG32k3a();	
	static {
		long seed[] = {1234,1234,1234,1234,1234,1234};
		randomStream.setSeed(seed);
	}	
	static double generatePoissonDemand(int inventoryLevel, int actionDecision, int[] demandMean, int currentStageIndex) {
		RandomVariateGenInt genDemand;		  
		genDemand = new PoissonGen(randomStream, new PoissonDist(demandMean[currentStageIndex])); 
		int demand = genDemand.nextInt();
		return (double)-demand;
	}
	
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
			int demand = (int) generatePoissonDemand(inventoryLevel, actionDecision, sSsimInstance.demandMean, currentStageIndex); // as a negative
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
		double fixedOrderingCost = 5;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 3;
		int[] demandMean = {2, 1, 5, 3};

		int minInventory = -50;
		int maxInventory = 50;
		
		int[] reorderPoint = {0, -1, 3, 1};
		int[] actionS = {3, 2, 9, 4};

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
		//System.out.println("Total CPU time: "+timer.format());
		System.out.println("Average = " + sSsystem.statCost.average());
		
		
		//simulatesQinstanceOneRun(sSsystem, true);

	}

}
