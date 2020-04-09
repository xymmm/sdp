package RS;

import java.util.ArrayList;
import java.util.List;

import minlp_Poisson.simPoissonInstance;
import umontreal.ssj.probdist.NormalDist;
import umontreal.ssj.probdist.PoissonDist;
import umontreal.ssj.randvar.PoissonGen;
import umontreal.ssj.randvar.RandomVariateGenInt;
import umontreal.ssj.rng.MRG32k3a;
import umontreal.ssj.util.Chrono;

public class RSsimulation {
	
	public static class simulationRSmilp {
		
		
		/** compute purchasing cost according to action decision **/
	static double computePurchasingCost(double purchase, int currentStageIndex, double[] schedule, double fixedCost, double unitCost) {
		return purchase*(
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
	
	/** generate Normal random number as demand **/
	static MRG32k3a randomStream = new MRG32k3a();	
	static {
	   long seed[] = {1234,1234,1234,1234,1234,1234};
	   randomStream.setSeed(seed);
	}	
	static double generateNormalDemand(double[] demandMean, double stdParameter, int currentStageIndex) {
		double demand = NormalDist.inverseF(demandMean[currentStageIndex], 
				demandMean[currentStageIndex]*stdParameter, randomStream.nextDouble());
		return -Math.round(demand);
	}

	
	//************************************************************************************************************************************
		public static double simNormalRS(RSmilpSimInstance RSinstance, boolean print) {
			double cost = 0;		
			int currentStageIndex = 0;
			
			List<Integer> xLabel = new ArrayList<>();//to print variation of inventory - x axis
			List<Double>  yLabel = new ArrayList<>();//to print variation of inventory - y axis
			
			double inventoryLevel = RSinstance.initialStock;
			
			do {
				xLabel.add(currentStageIndex);
				yLabel.add(inventoryLevel);
				
				if(print == true) System.out.println("At stage "+(currentStageIndex + 1));
				if(print == true) System.out.println("Current inventory level is "+inventoryLevel);
								
				if(print == true) System.out.println((RSinstance.purchase[currentStageIndex] > 0) ? "Order-up-to Level = " + RSinstance.uptoLevel[currentStageIndex]:"No order placed. ");
				//if(currentStageIndex == 0) actionDecision = 0;
				
				//compute purchasing cost
				cost += computePurchasingCost(RSinstance.purchase[currentStageIndex], currentStageIndex, RSinstance.uptoLevel, RSinstance.fixedCost, RSinstance.unitCost);
				
				//update inventory level
				inventoryLevel = updateInventoryLevel(inventoryLevel, RSinstance.purchase[currentStageIndex]*RSinstance.uptoLevel[currentStageIndex]);
				if(print == true) System.out.println("Updated inventory level is "+inventoryLevel);			
				if(RSinstance.purchase[currentStageIndex] > 0) {
					xLabel.add(currentStageIndex);
					yLabel.add(inventoryLevel);
				}
				
				//generate, check and meet demand
				double demand = generateNormalDemand(RSinstance.demandMean, RSinstance.stdParameter, currentStageIndex); 
				if(print == true) System.out.println("Demand in this stage is "+(-demand));
				
				//update inventory level
				inventoryLevel = updateInventoryLevel(inventoryLevel, demand);
				if(print == true) System.out.println("Inventory level after meeting demand is "+inventoryLevel);
				
				//5. compute closing cost
				cost += computeClosingCost(inventoryLevel, RSinstance.holdingCost, RSinstance.penaltyCost);
				if(print == true) System.out.println("Cumulative cost is "+cost);
				
				if(currentStageIndex == RSinstance.demandMean.length -1) {
					xLabel.add(currentStageIndex +1);
					yLabel.add(inventoryLevel);
				}
				
				currentStageIndex++;
				if(print == true) System.out.println();
			}while(currentStageIndex < RSinstance.demandMean.length);
			
			//System.out.println(xLabel);
			//System.out.println(yLabel);
			return cost;
		}
		
		/** multiple run times **/
		public static void simulationNormalRSmultipleRuns(RSmilpSimInstance RSinstance, int count) {
			for(int i=0; i<count; i++) {
				RSinstance.statCost.add(simNormalRS(RSinstance, false));
			}
		}

		public static void main(String[] args) {

			/** declare instance parameters ***/
			double fixedOrderingCost = 100;
			double penaltyCost = 10;
			
			double unitCost = 0;
			double holdingCost = 1;
			
			double initialStock = 0;

			double[] demandMean = {20,40,60,40};
			double stdParameter = 0.25;
			
			double[] S = {73.249, 93.249, 121.364, 61.364}; 
			//double[] S = {73.215, 53.341, 120.832, 60.959};
			double[] purchase = {1, 0, 1, 0};

			RSmilpSimInstance RSinstance = new RSmilpSimInstance(
					demandMean, 
					stdParameter,
					fixedOrderingCost,
					unitCost,
					holdingCost, 
					penaltyCost, 
					initialStock, 
					S, 
					purchase				
					);	
			
			Chrono timer = new Chrono();
			
			int count = 6000000;
			simulationNormalRSmultipleRuns(RSinstance, count);
			
			RSinstance.statCost.setConfidenceIntervalStudent();
			System.out.println(RSinstance.statCost.report(0.9, 3));
			System.out.println("Total CPU time: "+timer.format());

				System.out.println(RSinstance.statCost.average());
			}
	}

}
