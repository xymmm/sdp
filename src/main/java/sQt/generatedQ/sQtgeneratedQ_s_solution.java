package sQt.generatedQ;

import sQ.sdp.sQgivenQsolution;
import sdp.data.Instance;

public class sQtgeneratedQ_s_solution {
	
	public double[][] costGivenQ;
	public  boolean[][] actionGivenQ;
	public double[][] costOrder;
	public double[][] costNoOrder;
	public long timeConsumed_sQtst;

	public sQtgeneratedQ_s_solution(double[][] costGivenQ, 
			boolean[][] actionGivenQ,
			double[][] costOrder,
			double[][] costNoOrder,
			long timeConsumed_sQtst) {
		this.costGivenQ = costGivenQ;
		this.actionGivenQ = actionGivenQ;
		this.costOrder = costOrder;
		this.costNoOrder = costNoOrder;
		this.timeConsumed_sQtst = timeConsumed_sQtst;
	}
	
	public int[] getsGivenQ(Instance instance, sQtgeneratedQ_s_solution sQtgeneratedQ_s_solution) {
		int[] s = new int[instance.getStages()];
		for(int t=0;t<instance.getStages();t++) { // Time
			for(int i=(instance.initialInventory - instance.minInventory);i<(instance.maxInventory - instance.minInventory+1);i++) {  // Inventory   
				if(sQtgeneratedQ_s_solution.actionGivenQ[t][i] == false) {
					s[t] = i + instance.minInventory;
					break;
				}else {
				   s[t] = instance.minInventory;
				}
			}
		}
		return s;
	}

	public double getMinCost(Instance instance, sQgivenQsolution sQgivenQsolution, int currentStageIndex) {
		double min = sQgivenQsolution.costGivenQ[currentStageIndex][0];
		for(int i=1; i<sQgivenQsolution.costGivenQ[currentStageIndex].length; i++) {
			if (sQgivenQsolution.costGivenQ[currentStageIndex][i]< min) {
				min = sQgivenQsolution.costGivenQ[currentStageIndex][i];
				//System.out.println(currentStageIndex + " "+(i+instance.minInventory));
			}
		}
		return min;
	}
	
	public int getMinCostIndex(Instance instance, sQgivenQsolution sQgivenQsolution, int currentStageIndex) {
		double min = sQgivenQsolution.costGivenQ[currentStageIndex][0];
		int index = 0;
		for(int i=1; i<sQgivenQsolution.costGivenQ[currentStageIndex].length; i++) {
			if (sQgivenQsolution.costGivenQ[currentStageIndex][i]< min) {
				min = sQgivenQsolution.costGivenQ[currentStageIndex][i];
				index = i+instance.minInventory;
			}
		}
		return index;
	}

}
