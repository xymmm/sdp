package sQ.sdp;

import sdp.data.Instance;
import sdp.data.InstanceDouble;

public class sQsolution {

	public double[] schedule;
	public double optimalCost;
	public double[][] demandProbabilities;
	//public int opt_a;

	public sQsolution(
			double[] schedule,
			double optimalCost, 
			double[][] demandProbabilities) {
		this.schedule = schedule;
		this.optimalCost = optimalCost;
		this.demandProbabilities = demandProbabilities;
	}

		
}
