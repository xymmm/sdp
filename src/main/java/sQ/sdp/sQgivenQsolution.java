package sQ.sdp;

import sdp.data.Instance;

public class sQgivenQsolution {

	public double[][] costGivenQ;
	public  boolean[][] actionGivenQ;

	public sQgivenQsolution(double[][] costGivenQ, 
			boolean[][] actionGivenQ) {
		this.costGivenQ = costGivenQ;
		this.actionGivenQ = actionGivenQ;
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
