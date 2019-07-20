package sdp.stage;

import sdp.dataProcess.Demand;

public interface ICostWithoutHistory {
	
	/**
	 * This interface accesses to the method that calculates the expected total cost of the last stage.
	 * 
	 * The input includes variables in the class Demand.
	 * 
	 * @param demand
	 * 
	 * @return
	 */
	public double [] calCostWithoutHistory(Demand demand);

}
