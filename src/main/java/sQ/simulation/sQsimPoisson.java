package sQ.simulation;

import java.util.Random;

import org.apache.commons.math3.distribution.NormalDistribution;

import umontreal.ssj.probdist.NormalDist;
import umontreal.ssj.probdist.PoissonDist;
import umontreal.ssj.randvar.PoissonGen;
import umontreal.ssj.randvar.RandomVariateGenInt;
import umontreal.ssj.rng.MRG32k3a;
import umontreal.ssj.rng.RandomStream;
import umontreal.ssj.util.Chrono;

public class sQsimPoisson {
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
	static double computePurchasingCost(int actionDecision, int currentStageIndex, sQsimInstanceInt sQsimInstance) {
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
	static MRG32k3a randomStream = new MRG32k3a();
	
	static {
	   long seed[] = {1234,1234,1234,1234,1234,1234};
	   randomStream.setSeed(seed);
	}
	
	static int generateDemand(int inventoryLevel, int actionDecision, sQsimInstanceInt sQsimInstance, int currentStageIndex) {
		RandomVariateGenInt genDemand;
		  
		genDemand = new PoissonGen(randomStream, new PoissonDist(sQsimInstance.demandMean[currentStageIndex])); 
		int demand = genDemand.nextInt();

		return -demand;
	}
	
	/** 5. compute holding or penalty cost **/
	static double computeClosingCost(int inventoryLevel, sQsimInstanceInt sQsimInstance) {
		if(inventoryLevel >= 0) {//return holding cost
			return inventoryLevel*sQsimInstance.holdingCost;
		}else {//return penalty cost
			return -inventoryLevel*sQsimInstance.penaltyCost;
		}
	}
	
	public static double sQsimPoisson(sQsimInstanceInt sQsimInstance, boolean print) {

		int inventoryLevel = sQsimInstance.getInitialInventory();
		double cost = 0;
		int actionDecision;
		
		int currentStageIndex = 0;
		do {
			if(print == true) System.out.println("At stage "+(currentStageIndex + 1));
			if(print == true) System.out.println("Current inventory level is "+inventoryLevel);
			
			//1 & 2 check inventory
			if(sQsimInstance.getActionQuantity(currentStageIndex) == 0) {
				actionDecision = 0;
			}else {
				actionDecision = checkInventory(sQsimInstance.reorderPoint[currentStageIndex], inventoryLevel);
			}System.out.println(actionDecision);
			if(print == true) System.out.println((actionDecision == 1) ? "Replenishment order placed. ":"No order placed. ");
			//if(currentStageIndex == 0) actionDecision =0;
			
			//2. compute purchasing cost
			cost += computePurchasingCost(actionDecision, currentStageIndex, sQsimInstance);
			
			//3. update inventory level
			inventoryLevel = updateInventoryLevel(inventoryLevel, actionDecision*sQsimInstance.getActionQuantity(currentStageIndex));
			if(print == true) System.out.println("Updated inventory level is "+inventoryLevel);
			
			//4. generate, check and meet demand
			int demand = generateDemand(inventoryLevel, actionDecision, sQsimInstance, currentStageIndex); // as a negative
			if(print == true) System.out.println("Demand in this stage is "+(-demand));
			
			//update inventory level
			inventoryLevel = updateInventoryLevel(inventoryLevel, demand);
			if(print == true) System.out.println("Inventory level after meeting demand is "+inventoryLevel);
			
			//5. compute closing cost
			cost += computeClosingCost(inventoryLevel, sQsimInstance);
			if(print == true) System.out.println("Cumulative cost is "+cost);
			
			currentStageIndex++;
			if(print == true) System.out.println();
		}while(
				currentStageIndex < sQsimInstance.getStages()
				);
		if(print == true) System.out.println("=============================");
		return cost;
	}
	
	/** multiple run times **/
	public static void sQsimPoissonMultiRuns(sQsimInstanceInt sQsimInstance, int count) {
		for(int i=0; i<count; i++) {
			sQsimInstance.statCost.add(sQsimPoisson(sQsimInstance,true));
		}
	}

	public static void main(String[] args) {

		/** declare instance parameters **/
		double fixedOrderingCost = 10;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 5;

		double tail = 0.00000001;

		int minInventory = -50;
		int maxInventory = 50;
		double coe = 0.25;
		
		int[] demandMean = {2,4,6,4};
		int[] reorderPoint = {-15, -5, -6, -19};
		/*
		int Q = 82;
		int[] actionQuantity = new int[reorderPoint.length];
		for(int t=0; t<actionQuantity.length;t++) {
			actionQuantity[t] = Q;
		}*/
		int[] actionQuantity = {8,0,9,0};

		sQsimInstanceInt sQsystem1 = new sQsimInstanceInt(
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
				coe
				);	
		
		Chrono timer = new Chrono();
		
		int count = 2;
		sQsimPoisson.sQsimPoissonMultiRuns(sQsystem1, count);
		
		sQsystem1.statCost.setConfidenceIntervalStudent();
		System.out.println(sQsystem1.statCost.report(0.9, 3));
		System.out.println("Total CPU time: "+timer.format());

	}

}
