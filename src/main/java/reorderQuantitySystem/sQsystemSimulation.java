package reorderQuantitySystem;

import java.util.Random;

import org.apache.commons.math3.distribution.NormalDistribution;

import sQ.simulation.sQsimInstanceDouble;
import sQ.simulation.sQsimPoisson;
import umontreal.ssj.probdist.PoissonDist;
import umontreal.ssj.randvar.PoissonGen;
import umontreal.ssj.randvar.RandomVariateGenInt;
import umontreal.ssj.rng.MRG32k3a;
import umontreal.ssj.rng.RandomStream;
import umontreal.ssj.util.Chrono;

public class sQsystemSimulation {

	//*************************************************************************************************************************
	/** check inventory and return action decision **/
	static int checkInventory(double reorderPoint, double inventoryLevel) {
		return (inventoryLevel<reorderPoint)? 1 : 0;
	}
	
	/** compute purchasing cost according to action decision **/
	static double computePurchasingCost(int actionDecision, int currentStageIndex, simInstance instance, int[] orderQuantity) {
		return actionDecision*(
				instance.fixedOrderingCost 
				+ instance.unitCost*orderQuantity[currentStageIndex]
				);
	}
	
	/** update inventory level**/
	static int updateInventoryLevel(int inventoryLevel, int inventoryAlteration) {
		return inventoryLevel + inventoryAlteration;
	}
	
	/** generate Poisson random number as demand **/
	static MRG32k3a randomStream = new MRG32k3a();	
	static {
		long seed[] = {1234,1234,1234,1234,1234,1234};
		randomStream.setSeed(seed);
	}	
	static int generateDemand(double inventoryLevel, int actionDecision, simInstance instance, int currentStageIndex) {
		RandomVariateGenInt genDemand;		  
		genDemand = new PoissonGen(randomStream, new PoissonDist(instance.demandMean[currentStageIndex])); 
		int demand = genDemand.nextInt();
		return -demand;
	}
	
	
	/** compute holding or penalty cost **/
	static double computeClosingCost(double inventoryLevel, simInstance instance) {
		if(inventoryLevel >= 0) {//return holding cost
			return inventoryLevel*instance.holdingCost;
		}else {//return penalty cost
			return -inventoryLevel*instance.penaltyCost;
		}
	}
//************************************************************************************************************************************
	
	
	public static double sQsimPoisson(simInstance instance, boolean print) {
		int inventoryLevel = instance.initialInventory;
		double cost = 0;
		int actionDecision;		
		int currentStageIndex = 0;
		do {
			if(print == true) System.out.println("At stage "+(currentStageIndex + 1));
			if(print == true) System.out.println("Current inventory level is "+inventoryLevel);
			
			//check inventory
			actionDecision = checkInventory(instance.reorderPoint[currentStageIndex], inventoryLevel);
			if(print == true) System.out.println((actionDecision == 1) ? "Replenishment order placed with quantity " + instance.orderQuantity[currentStageIndex]:"No order placed. ");
			//if(currentStageIndex == 0) actionDecision = 0;
			
			//compute purchasing cost
			cost += computePurchasingCost(actionDecision, currentStageIndex, instance, instance.orderQuantity);
			
			//update inventory level
			inventoryLevel = updateInventoryLevel(inventoryLevel, actionDecision*instance.orderQuantity[currentStageIndex]);
			if(print == true) System.out.println("Updated inventory level is "+inventoryLevel);
			
			//generate, check and meet demand
			int demand = generateDemand(inventoryLevel, actionDecision, instance, currentStageIndex); // as a negative
			if(print == true) System.out.println("Demand in this stage is "+(-demand));
			
			//update inventory level
			inventoryLevel = updateInventoryLevel(inventoryLevel, demand);
			if(print == true) System.out.println("Inventory level after meeting demand is "+inventoryLevel);
			
			//5. compute closing cost
			cost += computeClosingCost(inventoryLevel, instance);
			if(print == true) System.out.println("Cumulative cost is "+cost);
			
			currentStageIndex++;
			if(print == true) System.out.println();
		}while(
				currentStageIndex < instance.getStages()
				);
		if(print == true) System.out.println("=============================");
		return cost;
	}
	
	/** multiple run times **/
	public static void sQsimPoissonMultiRuns(simInstance sQsystem1, int count) {
		for(int i=0; i<count; i++) {
			sQsystem1.statCost.add(sQsimPoisson(sQsystem1, false));
		}
	}
	
	
	
	public static void main(String[] args) {

		/** declare instance parameters **/
		double fixedOrderingCost = 10;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 5;

		double tail = 0.00000001;

		int minInventory = -100;
		int maxInventory = 100;
		double coe = 0.25;
		int[] demandMean = {2,4,6,4};
		int[] reorderPoint = {5, -100, 7, -100};

		int[] orderQuantity = {9, 0, 9, 0};

		simInstance sQsystem = new simInstance(
				fixedOrderingCost,
				unitCost,
				holdingCost,
				penaltyCost,
				demandMean,
				tail,
				minInventory,
				maxInventory,
				coe,
				reorderPoint,
				orderQuantity				
				);	
		
		Chrono timer = new Chrono();
		
		int count = 50000;
		sQsimPoissonMultiRuns(sQsystem, count);
		
		sQsystem.statCost.setConfidenceIntervalStudent();
		System.out.println(sQsystem.statCost.report(0.9, 3));
		System.out.println("Total CPU time: "+timer.format());

		System.out.println(sQsystem.statCost.average());
	}

}
