package reorderQuantitySystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sS.sdp.sS;
import sdp.data.Instance;

public class optimalSchedule_sQt {
	
	public static sQsystemSolution optimalSchedule_sQt(Instance instance) {
		
		//double demandProbabilities [][] = sS.computeNormalDemandProbability(instance.demandMean, instance.stdParameter, instance.maxDemand, instance.tail); //normal
		double demandProbabilities [][] = sS.computeDemandProbability(instance.demandMean, instance.maxDemand, instance.tail);//Poisson

		double costPrevious;
		double costCurrent;
		double costLater;
		
		int maxG = (int) Math.pow(10, instance.getStages());
		
		List<Double> LocalMinCostsList = new ArrayList<>();
		List<Integer> minGsList		   = new ArrayList<>();
		
		long startTime = System.currentTimeMillis();
		
		//SPECIAL CASE: g = 0,1,2
		costPrevious = singleScheduleCost.singleScheduleCost(instance, 
															 scheduleGenerator_sQt.sQtschedule(instance.getStages(), 0), 
															 demandProbabilities)[instance.initialInventory -instance.minInventory][0];
		costCurrent  = singleScheduleCost.singleScheduleCost(instance, 
				 											 scheduleGenerator_sQt.sQtschedule(instance.getStages(), 1), 
				 											 demandProbabilities)[instance.initialInventory -instance.minInventory][0];
		costLater    = singleScheduleCost.singleScheduleCost(instance, 
				 											 scheduleGenerator_sQt.sQtschedule(instance.getStages(), 2), 
				 											 demandProbabilities)[instance.initialInventory -instance.minInventory][0];
		if((costPrevious > costCurrent)&&(costCurrent < costLater)) {
			LocalMinCostsList.add(costCurrent);
			minGsList.add(1);
		}
		int g = 3;
		do {
			costPrevious = costCurrent;
			costCurrent = costLater;
			costLater = singleScheduleCost.singleScheduleCost(instance, 
					 										  scheduleGenerator_sQt.sQtschedule(instance.getStages(), g), 
					 										  demandProbabilities)[instance.initialInventory -instance.minInventory][0];
			if((costPrevious > costCurrent)&&(costCurrent < costLater)) {
				LocalMinCostsList.add(costCurrent);
				minGsList.add(g-1);
			}
			if(g%10000 == 0) System.out.println("Computation completed for generator = "+g);
			g = g+1;
		}while(g < maxG);
				
		double[] LocalMinCosts = new double[LocalMinCostsList.size()];
		int[] minGs	  = new int[minGsList.size()];
		for(int i=0; i<LocalMinCosts.length; i++) {
			LocalMinCosts[i] = (double) LocalMinCostsList.get(i);
			minGs[i] = (int) minGsList.get(i);
		}
		
		double optimalCost = LocalMinCosts[0];
		int optimalScheduleGenerator = minGs[0];
		for(int i=1; i<LocalMinCosts.length; i++) {
			if((LocalMinCosts[i] < optimalCost)&&(LocalMinCosts[i] < LocalMinCosts[i-1])) {
				optimalCost = LocalMinCosts[i];
				optimalScheduleGenerator = minGs[i];
			}
		}		
		int[] optimalSchedule = scheduleGenerator_sQt.sQtschedule(instance.getStages(), optimalScheduleGenerator);
		
		return new sQsystemSolution(optimalSchedule, optimalCost, demandProbabilities);		
	}
	
/*	
	public static void main(String args[]) {
		double fixedOrderingCost = 10;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 5;

		double tail = 0.00000001;

		int minInventory = -50;
		int maxInventory = 50;
		int maxQuantity = 9;

		double stdParameter = 0.25;

		//int[] demandMean = {20, 40, 60, 40};
		int[] demandMean = {2,4,6,4};
		
		Instance instance = new Instance(
				fixedOrderingCost,
				unitCost,
				holdingCost,
				penaltyCost,
				demandMean,
				tail,
				minInventory,
				maxInventory,
				maxQuantity,
				stdParameter
				);

		sQsystemSolution sQsolution = optimalSchedule_sQt(instance);
		
		System.out.println(sQsolution.optimalCost);
		System.out.println(Arrays.toString(sQsolution.optimalSchedule));

	}
*/

}
