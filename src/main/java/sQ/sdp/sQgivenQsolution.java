package sQ.sdp;

import sdp.data.Instance;

public class sQgivenQsolution {

	public double[][] costGivenQ;
	public  boolean[][] actionGivenQ;
	public double[][] costOrder;
	public double[][] costNoOrder;

	public sQgivenQsolution(double[][] costGivenQ, 
			boolean[][] actionGivenQ,
			double[][] costOrder,
			double[][] costNoOrder) {
		this.costGivenQ = costGivenQ;
		this.actionGivenQ = actionGivenQ;
		this.costOrder = costOrder;
		this.costNoOrder = costNoOrder;
	}
	
	public int[] getsGivenQ(Instance instance, sQgivenQsolution sQgivenQsolution) {
		int[] s = new int[instance.getStages()];
		for(int t=0;t<instance.getStages();t++) { // Time
			for(int i=0;i<(instance.maxInventory - instance.minInventory+1);i++) {  // Inventory   
				if(sQgivenQsolution.actionGivenQ[t][i] == false) {
					s[t] = i + instance.minInventory;
					break;
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
		int min = 0;
		for(int i=1; i<sQgivenQsolution.costGivenQ[currentStageIndex].length; i++) {
			if (sQgivenQsolution.costGivenQ[currentStageIndex][i]< sQgivenQsolution.costGivenQ[currentStageIndex][i-1]) {
				min = i+instance.minInventory;
			}
		}
		return min;
	}
	
}
