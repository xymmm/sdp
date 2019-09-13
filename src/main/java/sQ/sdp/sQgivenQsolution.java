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

	
}
