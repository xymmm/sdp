package sQ.simulation;

import umontreal.ssj.probdist.NormalDist;
import umontreal.ssj.rng.MRG32k3a;
import umontreal.ssj.util.Chrono;

public class sQsimNormal {
	
	/** 1. check inventory and return action decision **/
	static int checkInventory(double reorderPoint, double inventoryLevel) {
		//compare the current inventory level and reorder point, 1 represents reorder 
		return (inventoryLevel<reorderPoint)? 1 : 0;
	}
	
	/** 2. compute purchasing cost according to action decision **/
	static double computePurchasingCost(int actionDecision, int currentStageIndex, sQsimInstanceDouble sQsimInstanceDouble) {
		return actionDecision*(
				sQsimInstanceDouble.fixedOrderingCost 
				+ sQsimInstanceDouble.unitCost*sQsimInstanceDouble.getActionQuantity(currentStageIndex)
				);
	}
	
	/** 3(1) & 5(2) update inventory level**/
	static double updateInventoryLevel(double inventoryLevel, double inventoryAlteration) {
		return inventoryLevel + inventoryAlteration;
	}
	
	
	/** 6. generate Normal demand, double**/
	static MRG32k3a randomStream = new MRG32k3a();
	
	static double generateNormalDemand(double inventoryLevel, int actionDecision, sQsimInstanceDouble sQsimInstanceDouble, int currentStageIndex) {
		//int demand = (int) (randomno.nextGaussian()*sQsimInstance.demandMean[currentStageIndex]* sQsimInstance.coe + sQsimInstance.demandMean[currentStageIndex] );
		double refactor = 1-2*sQsimInstanceDouble.tail;//new mass
		double demand = NormalDist.inverseF(sQsimInstanceDouble.demandMean[currentStageIndex], 
				sQsimInstanceDouble.demandMean[currentStageIndex]*sQsimInstanceDouble.coe, randomStream.nextDouble()/refactor);
		return -demand;
	}
	
	/** 5. compute holding or penalty cost **/
	static double computeClosingCost(double inventoryLevel, sQsimInstanceDouble sQsimInstanceDouble) {
		if(inventoryLevel >= 0) {//return holding cost
			return inventoryLevel*sQsimInstanceDouble.holdingCost;
		}else {//return penalty cost
			return -inventoryLevel*sQsimInstanceDouble.penaltyCost;
		}
	}
	
	public static double sQsimNormal(sQsimInstanceDouble sQsimInstanceDouble, boolean print, boolean initialOrder) {

		double inventoryLevel = sQsimInstanceDouble.getInitialInventory();
		double cost = 0;
		int actionDecision;
		
		int currentStageIndex = 0;
		do {
			if(print == true) System.out.println("At stage "+(currentStageIndex + 1));
			if(print == true) System.out.println("Current inventory level is "+inventoryLevel);
			
			//1 & 2 check inventory, place orders
			actionDecision = checkInventory(sQsimInstanceDouble.reorderPoint[currentStageIndex], inventoryLevel);
			if(print == true) System.out.println((actionDecision == 1) ? "Replenishment order placed. ":"No order placed. ");
			if(initialOrder == false) {
				if(currentStageIndex == 0) actionDecision = 0;
			}
			
			//2. compute purchasing cost
			cost += computePurchasingCost(actionDecision, currentStageIndex, sQsimInstanceDouble);
			
			//3. update inventory level
			inventoryLevel = updateInventoryLevel(inventoryLevel, actionDecision*sQsimInstanceDouble.getActionQuantity(currentStageIndex));
			if(print == true) System.out.println("Updated inventory level is "+inventoryLevel);
			
			//4. generate, check and meet demand
			double demand = generateNormalDemand(inventoryLevel, actionDecision, sQsimInstanceDouble, currentStageIndex); // as a negative
			if(print == true) System.out.println("Demand in this stage is "+(-demand));
			
			//update inventory level
			inventoryLevel = updateInventoryLevel(inventoryLevel, demand);
			if(print == true) System.out.println("Inventory level after meeting demand is "+inventoryLevel);
			
			//5. compute closing cost
			cost += computeClosingCost(inventoryLevel, sQsimInstanceDouble);
			if(print == true) System.out.println("Cumulative cost is "+cost);
			
			currentStageIndex++;
			
			if(print == true) System.out.println();
		}while(
				currentStageIndex < sQsimInstanceDouble.getStages()
				);
		if(print == true) System.out.println("=============================");
		return cost;
	}
	
	/** multiple run times **/
	public static void sQsimNormalMultiRuns(sQsimInstanceDouble sQsimInstanceDouble, int count, boolean initialOrder) {
		for(int i=0; i<count; i++) {
			sQsimInstanceDouble.statCost.add(sQsimNormal(sQsimInstanceDouble,false,initialOrder));
		}
	}
	
	public static void main(String[] args) {
		
		double fixedOrderingCost = 100;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 10;
		
		double tail = 0.00000001;

		int minInventory = -500;
		int maxInventory = 500;
		double coe = 0.25;
		
		//instance 5
		
		double[] demandMean = {50,30,60,20,40,50};
		double[] reorderPoint = {49,22,60,12,35,41};
		double Q = 90.765;
		
		
		//instance 1
		/*
		double[] demandMean = {40,60,20,30};
		double[] reorderPoint = {30,55,17,19};
		double Q = 157.27;
		*/
		
		//instance classic
		/*
		double[] demandMean = {20,40,60,40};
		double[] reorderPoint = {15,30,57,30};
		double Q = 168.56;
		*/
		
		double[] actionQuantity = new double[reorderPoint.length];
		for(int t=0; t<actionQuantity.length;t++) {
			actionQuantity[t] = Q;
		}
		
		sQsimInstanceDouble sQsystem = new sQsimInstanceDouble(
				fixedOrderingCost,
				unitCost,
				holdingCost,
				penaltyCost,
				demandMean,
				tail,
				minInventory,
				maxInventory,
				actionQuantity,
				reorderPoint,
				coe);
		
		Chrono timer = new Chrono();
		
		int count = 50000;
		
		sQsimNormal.sQsimNormalMultiRuns(sQsystem, count, true);
		
		sQsystem.statCost.setConfidenceIntervalStudent();
		System.out.println(sQsystem.statCost.report(0.9, 3));
		System.out.println("Total CPU time: "+timer.format());

		
	}
	

}
