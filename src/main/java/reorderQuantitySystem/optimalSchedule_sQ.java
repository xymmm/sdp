package reorderQuantitySystem;

import java.util.Arrays;

import sQ.sdp.sQsolution;
import sS.sS;
import sdp.data.Instance;
import sdp.data.InstanceDouble;

public class optimalSchedule_sQ {
	
	public static sQsystemSolution optimalSchedule_sQ(InstanceDouble instance, boolean Normal) {		
		
		double[][] demandProbabilities = null;
		if(Normal == true) {
			demandProbabilities = sS.computeNormalDemandProbability(instance.demandMean, instance.stdParameter, instance.maxDemand, instance.tail); //normal
		}else {
			demandProbabilities = sS.computeDemandProbability(instance.demandMean, instance.maxDemand, instance.tail);//Poisson

		}		
		double[] bestCost = new double[instance.maxQuantity];
		int[][] bestSchedule = new int[instance.maxQuantity][instance.getStages()];
			
		for(int q=1; q<=instance.maxQuantity; q++) {
			int[][] schedule = scheduleGenerator_sQ.generateQ(instance, q);
			int N = (int) Math.pow(2, instance.getStages());  
			double[] cost = new double[N];
			
			for(int n=0; n<N; n++) {
				cost[n] = singleScheduleCost.singleScheduleCost(instance, schedule[n], demandProbabilities)[(int) (instance.initialInventory -instance.minInventory)][0];
			}
			bestCost[q-1] = sdp.util.globleMinimum.getGlobalMinimum(cost);
			bestSchedule[q-1] = schedule[sdp.util.globalMinimumIndex.getGlobalMinimumJavaIndex(cost)];
		}
		
		//System.out.println(Arrays.toString(bestCost));
		
		double optimalCost = sdp.util.globleMinimum.getGlobalMinimum(bestCost);
		int[] optimalSchedule = bestSchedule[sdp.util.globalMinimumIndex.getGlobalMinimumJavaIndex(bestCost)];
		
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

		//int[] demandMean = {200, 240, 260, 240};
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

		sQsystemSolution sQsolution = optimalSchedule_sQ(instance);
		
		System.out.println(sQsolution.optimalCost);
		System.out.println(Arrays.toString(sQsolution.optimalSchedule));

	}
	*/

}
