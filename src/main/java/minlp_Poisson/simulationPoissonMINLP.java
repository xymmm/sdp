package minlp_Poisson;

import java.util.ArrayList;
import java.util.List;

import minlp_Normal.simNormalInstance;
import reorderQuantitySystem.simInstance;
import umontreal.ssj.probdist.NormalDist;
import umontreal.ssj.probdist.PoissonDist;
import umontreal.ssj.randvar.PoissonGen;
import umontreal.ssj.randvar.RandomVariateGenInt;
import umontreal.ssj.rng.MRG32k3a;
import umontreal.ssj.util.Chrono;

public class simulationPoissonMINLP {
	
	/** check inventory and return action decision **/
	static int checkInventory(double reorderPoint, double inventoryLevel) {
		return (inventoryLevel<reorderPoint)? 1 : 0;
	}
	
	/** compute purchasing cost according to action decision **/
	static double computePurchasingCost(int actionDecision, int currentStageIndex, double[] schedule, double fixedCost, double unitCost) {
		return actionDecision*(
				fixedCost 
				+ unitCost*schedule[currentStageIndex]
				);
	}
	
	/** update inventory level**/
	static double updateInventoryLevel(double inventoryLevel, double inventoryAlteration) {
		return inventoryLevel + inventoryAlteration;
	}
	
	/** compute holding or penalty cost **/
	static double computeClosingCost(double inventoryLevel, double holdingCost, double penaltyCost) {
		if(inventoryLevel >= 0) {//return holding cost
			return inventoryLevel*holdingCost;
		}else {//return penalty cost
			return -inventoryLevel*penaltyCost;
		}
	}
	
	/** generate Poisson random number as demand **/
	static MRG32k3a randomStream = new MRG32k3a();	
	static {
		long seed[] = {1234,1234,1234,1234,1234,1234};
		randomStream.setSeed(seed);
	}	
	static double generatePoissonDemand(double inventoryLevel, int actionDecision, double[] demandMean, int currentStageIndex) {
		RandomVariateGenInt genDemand;		  
		genDemand = new PoissonGen(randomStream, new PoissonDist(demandMean[currentStageIndex])); 
		int demand = genDemand.nextInt();
		return (double)-demand;
	}

	
	//************************************************************************************************************************************
		public static double simPoisson(simPoissonInstance PoissonInstance, boolean print) {
			double cost = 0;
			int actionDecision;		
			int currentStageIndex = 0;
			
			List<Integer> xLabel = new ArrayList<>();//to print variation of inventory - x axis
			List<Double>  yLabel = new ArrayList<>();//to print variation of inventory - y axis
			double inventoryLevel = PoissonInstance.initialStock;
			
			do {
				xLabel.add(currentStageIndex);
				yLabel.add(inventoryLevel);
				
				if(print == true) System.out.println("At stage "+(currentStageIndex + 1));
				if(print == true) System.out.println("Current inventory level is "+inventoryLevel);
				
				//check inventory
				actionDecision = checkInventory(PoissonInstance.reorderPoints[currentStageIndex], inventoryLevel);
				
				if(print == true) System.out.println((actionDecision == 1) ? "Replenishment order placed with quantity " + PoissonInstance.schedule[currentStageIndex]:"No order placed. ");
				//if(currentStageIndex == 0) actionDecision = 0;
				
				//compute purchasing cost
				cost += computePurchasingCost(actionDecision, currentStageIndex, PoissonInstance.schedule, PoissonInstance.fixedCost, PoissonInstance.unitCost);
				
				//update inventory level
				inventoryLevel = updateInventoryLevel(inventoryLevel, actionDecision*PoissonInstance.schedule[currentStageIndex]);
				if(print == true) System.out.println("Updated inventory level is "+inventoryLevel);			
				if(actionDecision == 1) {
					xLabel.add(currentStageIndex);
					yLabel.add(inventoryLevel);
				}
				
				//generate, check and meet demand
				double demand = generatePoissonDemand(inventoryLevel, actionDecision, PoissonInstance.demandMean, currentStageIndex); 
				if(print == true) System.out.println("Demand in this stage is "+(-demand));
				
				//update inventory level
				inventoryLevel = updateInventoryLevel(inventoryLevel, demand);
				if(print == true) System.out.println("Inventory level after meeting demand is "+inventoryLevel);
				
				//5. compute closing cost
				cost += computeClosingCost(inventoryLevel, PoissonInstance.holdingCost, PoissonInstance.penaltyCost);
				if(print == true) System.out.println("Cumulative cost is "+cost);
				
				if(currentStageIndex == PoissonInstance.demandMean.length -1) {
					xLabel.add(currentStageIndex +1);
					yLabel.add(inventoryLevel);
				}
				
				currentStageIndex++;
				if(print == true) System.out.println();
			}while(currentStageIndex < PoissonInstance.demandMean.length);
			
			//System.out.println(xLabel);
			//System.out.println(yLabel);
			return cost;
		}
		
		/** multiple run times **/
		public static void simulationPoissonMINLPmultipleRuns(simPoissonInstance PoissonInstance, int count) {
			for(int i=0; i<count; i++) {
				PoissonInstance.statCost.add(simPoisson(PoissonInstance, false));
			}
		}

		public static void main(String[] args) {

			/** declare instance parameters ***/
			double fixedOrderingCost = 5;
			double unitCost = 0;
			double holdingCost = 1;
			double penaltyCost = 3;
			
			double initialStock = 0;

			double[] demandMean = {2, 1, 5, 3};
			
			//double[] schedule = {2.98, 3.53, 8.72, 5.00};
			//double[] reorderPoints = {1, -2, 4, 0};
			
			double[] schedule = {3, 3, 5, 4};
			double[] reorderPoints = {2, 1, 5, 1};

			simPoissonInstance PoissonInstance = new simPoissonInstance(
					demandMean, 
					fixedOrderingCost,
					unitCost,
					holdingCost, 
					penaltyCost, 
					initialStock, 
					schedule, 
					reorderPoints				
					);	
			
			Chrono timer = new Chrono();
			
			int count = 10000;
			simulationPoissonMINLPmultipleRuns(PoissonInstance, count);
			
			PoissonInstance.statCost.setConfidenceIntervalStudent();
			System.out.println(PoissonInstance.statCost.report(0.9, 3));
			System.out.println("Total CPU time: "+timer.format());

			System.out.println(PoissonInstance.statCost.average());
		}


}
