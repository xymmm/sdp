package sQ;

import sdp.data.Instance;

public class sQtsolution {

	public int[] inventory;
	public double totalCost[][][];
	public boolean optimalAction[][][];
	
	public sQtsolution(double[][][] totalCost, 
			boolean[][][] optimalAction, 
			int[] inventory) {
		this.totalCost = totalCost;
		this.optimalAction = optimalAction;
		this.inventory = inventory;
	}
	
	//TODO
	public int[] getQt(Instance instance) {
		int[] Qt = new int[instance.getStages()];
		return Qt;
	}
	//TODO
	public int[] getssQt(Instance instance) {
		int[] st = new int[instance.getStages()];
		return st;
	}
	
}
