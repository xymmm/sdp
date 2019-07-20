package sdp.stage;

import sdp.dataProcess.Demand;

public interface ICostWithHistory {
	
	/**
	 * 
	 * This interface accesses to the method that calculates the expected total cost of stage 2, ..., N-1. 
	 * 
	 * The input includes variables in the class Demand, 
	 * the cumulative expected total cost of previous stages (backwards), 
	 * and the index of the current stage.
	 * 
	 * @param demand
	 * @param preCost
	 * @param currentStage
	 * @return
	 */
	public double [] calCostWithHistory(Demand demand, double [] preCost, int currentStage);

}
