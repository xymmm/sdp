package LT_simulation;

import java.util.Arrays;

import umontreal.ssj.probdist.PoissonDist;
import umontreal.ssj.randvar.PoissonGen;
import umontreal.ssj.randvar.RandomVariateGenInt;
import umontreal.ssj.rng.MRG32k3a;

public class LT_optimal_simulation {

	static int checkReorder(int inventoryLevel, int locationIndex, int currentStageIndex, LT2locationsSimInstance simInstance) {
		//compare the current inventory level and reorder point, 1 represents reorder 
		return (inventoryLevel<simInstance.s[currentStageIndex][locationIndex])? 1 : 0;
	}

	//TODO:transshipment cost - condition to transship?

	static double computePurchasingCost(int inventoryLevel, int locationIndex, int actionDecision, int currentStageIndex, LT2locationsSimInstance simInstance) {
		return actionDecision*(
				simInstance.K 
				+ simInstance.z*simInstance.S[currentStageIndex][locationIndex] //quantity is computed
				);
	}

	static int[] updateTransshipment(int[] inventoryLevel, int currentStageIndex, LT2locationsSimInstance simInstance) {
		inventoryLevel[0] -= simInstance.transshipment[currentStageIndex];
		inventoryLevel[1] += simInstance.transshipment[currentStageIndex];
		return inventoryLevel;
	}

	
	static int[] updateOrder(int[] inventoryLevel, int currentStageIndex, LT2locationsSimInstance simInstance) {
		for(int i=0; i<2; i++) {
			inventoryLevel[i] = simInstance.S[currentStageIndex][i];
		}
		return inventoryLevel;
	}

	
	static int meetDemand(int inventoryLevel, int demand) {
		return inventoryLevel + demand;
	}
	static MRG32k3a randomStream = new MRG32k3a();	
	static {
		long seed[] = {1234,1234,1234,1234,1234,1234};
		randomStream.setSeed(seed);
	}	
	static double generatePoissonDemand(int inventoryLevel, int currentStageIndex, LT2locationsSimInstance simInstance) {
		RandomVariateGenInt genDemand;		  
		genDemand = new PoissonGen(randomStream, new PoissonDist(simInstance.demandMean[currentStageIndex])); 
		int demand = genDemand.nextInt();
		return (double)-demand;
	}
	
	static double computeClosingCost(int inventoryLevel, LT2locationsSimInstance simInstance) {
		if(inventoryLevel >= 0) {//return holding cost
			return inventoryLevel*simInstance.h;
		}else {//return penalty cost
			return -inventoryLevel*simInstance.b;
		}
	}
	
	
	public static double LToptimalSimulation_single(LT2locationsSimInstance simInstance, boolean print) {
		int[] inventoryLevel = simInstance.getInitialInventory();
		double costs[] = new double[2];
		double totalCost = 0;
		int[] actionDecisions = new int[2];
		
		int currentStageIndex = 0;
		do {
			if(print == true) System.out.println("At stage "+(currentStageIndex + 1));
			if(print == true) System.out.println("Current inventory levels are "+Arrays.toString(inventoryLevel));
			
			//check if transship
			for(int i=0; i<2; i++) {
				//actionDecisions[i] = 
			}
			
			//check if reorder
			for(int i=0; i<2; i++) {
				actionDecisions[i] = checkReorder(inventoryLevel[i], i, currentStageIndex, simInstance);
				costs[i] += computePurchasingCost(inventoryLevel[i], i, actionDecisions[i], currentStageIndex, simInstance);
			}
			inventoryLevel = updateOrder(inventoryLevel, currentStageIndex, simInstance);

			//generate and meet demand
			int[] demand = new int[2];
			for(int i=0; i<2; i++) {
				demand[i] = (int) generatePoissonDemand(inventoryLevel[i], currentStageIndex, simInstance);
				inventoryLevel[i] = meetDemand(inventoryLevel[i], demand[i]);
				costs[i] += computeClosingCost(inventoryLevel[i], simInstance);
			}
		}while(currentStageIndex < simInstance.demandMean.length);
		
		totalCost = costs[0] + costs[1];
		
		return totalCost;
	}

}
