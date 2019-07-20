package sdp.stage;

import sdp.dataProcess.Demand;

public interface ICostOriginalInventory {
	
	/**
	 * This is an interface that accesses to the method which calculates the expected cost for the first stage.
	 * 
	 * The input includes variables in class Demand and a 1-dimension array denoting the cumulative expected cost of previous stages (backwards).
	 * 
	 * @param demand
	 * @param preCost
	 * @return
	 */
	public double [] calCostOriginalInventory(Demand demand,double [] preCost);
}
