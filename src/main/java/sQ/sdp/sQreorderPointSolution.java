package sQ.sdp;

import sdp.data.Instance;
import sdp.data.InstanceDouble;

public class sQreorderPointSolution {

	public int[] inventory;
	public double[][] costGivenQ;
	public  boolean[][] actionGivenQ;
	public double[][] costOrder;
	public double[][] costNoOrder;

	public sQreorderPointSolution(int[] inventory,
			double[][] costGivenQ, 
			boolean[][] actionGivenQ,
			double[][] costOrder,
			double[][] costNoOrder) {
		this.costGivenQ = costGivenQ;
		this.actionGivenQ = actionGivenQ;
		this.costOrder = costOrder;
		this.costNoOrder = costNoOrder;
		this.inventory = inventory;
	}
	
	public static int[] getsGivenQ(InstanceDouble instance, sQreorderPointSolution sQgivenQsolution) {
		int[] s = new int[instance.getStages()];
		for(int t=0;t<instance.getStages();t++) { // Time
			for(int i=-instance.minInventory-100;i<(instance.maxInventory - instance.minInventory+1);i++) {  // Inventory   
				if(sQgivenQsolution.actionGivenQ[t][i] == false) {
					s[t] = i + instance.minInventory;
					break;
				}
			}
		}
		return s;
	}

	public double getMinCost(Instance instance, sQreorderPointSolution sQgivenQsolution, int currentStageIndex) {
		double min = sQgivenQsolution.costGivenQ[currentStageIndex][0];
		for(int i=1; i<sQgivenQsolution.costGivenQ[currentStageIndex].length; i++) {
			if (sQgivenQsolution.costGivenQ[currentStageIndex][i]< min) {
				min = sQgivenQsolution.costGivenQ[currentStageIndex][i];
				//System.out.println(currentStageIndex + " "+(i+instance.minInventory));
			}
		}
		return min;
	}
	
	public int getMinCostIndex(Instance instance, sQreorderPointSolution sQgivenQsolution, int currentStageIndex) {
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
