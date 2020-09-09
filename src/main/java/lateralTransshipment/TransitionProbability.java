package lateralTransshipment;

import org.apache.commons.math3.distribution.PoissonDistribution;

public class TransitionProbability {

	public static double computeTransitProbByDemand(LTinstance instance, State initialState, int[] action, int demand1, int demand2, int stageIndex) {
		PoissonDistribution dist1 = new PoissonDistribution(instance.demandMean1[stageIndex]);
		int maxDemand1 = dist1.inverseCumulativeProbability(1-instance.tail);
		
		PoissonDistribution dist2 = new PoissonDistribution(instance.demandMean1[stageIndex]);
		int maxDemand2 = dist1.inverseCumulativeProbability(1-instance.tail);
		
		double prob1 = dist1.probability(initialState.initialInventoryA - action[0] + action[1] - demand1)/dist1.cumulativeProbability(maxDemand1);
		double prob2 = dist2.probability(initialState.initialInventoryB + action[0] + action[2] - demand2)/dist2.cumulativeProbability(maxDemand2);	
		
		return prob1*prob2;
	}
	
	// double computeTransitProbByStates(State initialState, State finalState)
	// initial state is the state after you have received all orders and you have transshipped
	// just subtract initial and final inventory and then pmf
	
	// double computeTransitProb(int[] demand)
	// what is the probability that demand will be demand[0] in location 1 and demand[1] in location 2; just multiply poisson pmf
	
	public static double computeTransitProbByStates(LTinstance instance, State state1, State state2, int[] action, int stageIndex) {
		PoissonDistribution dist1 = new PoissonDistribution(instance.demandMean1[stageIndex]);
		int maxDemand1 = dist1.inverseCumulativeProbability(1-instance.tail);
		PoissonDistribution dist2 = new PoissonDistribution(instance.demandMean2[stageIndex]);
		int maxDemand2 = dist1.inverseCumulativeProbability(1-instance.tail);

		// compare with the max demand does not change the result, because infeasible actions, 
		// including infeasible transshipment and order quantities, are already excluded from the set.
		if((state1.initialInventoryA <= 0 )&&( state1.initialInventoryB<= 0)) {
			if(
					(state1.initialInventoryA + action[1] - state2.initialInventoryA <= maxDemand1) && (state1.initialInventoryA + action[1] - state2.initialInventoryA >= 0)&&
					(state1.initialInventoryB + action[2] - state2.initialInventoryB <= maxDemand2) && (state1.initialInventoryB + action[2] - state2.initialInventoryB >= 0)
					){
				return (dist1.probability(state1.initialInventoryA + action[1]- state2.initialInventoryA)/dist1.cumulativeProbability(maxDemand1))
						*(dist2.probability(state1.initialInventoryB + action[2] - state2.initialInventoryB)/dist2.cumulativeProbability(maxDemand2));
			}else {
				return 0;
			}		
		}else {
			if(
					(state1.initialInventoryA - action[0] + action[1] - state2.initialInventoryA <= maxDemand1)&&(state1.initialInventoryA - action[0] + action[1] - state2.initialInventoryA >= 0)&&
					(state1.initialInventoryB + action[0] + action[1] - state2.initialInventoryB <= maxDemand2)&&(state1.initialInventoryB + action[0] + action[2] - state2.initialInventoryB >= 0)
					) {
				return (dist1.probability(state1.initialInventoryA - action[0] + action[1] - state2.initialInventoryA )/dist1.cumulativeProbability(maxDemand1))
						*(dist2.probability(state1.initialInventoryB + action[0] + action[2] - state2.initialInventoryB)/dist2.cumulativeProbability(maxDemand2));
			}else {
				return 0;
			}
		}

	}
	

	public static int getMaxDemand(double tail, int mean) {
		PoissonDistribution dist = new PoissonDistribution(mean);
		return dist.inverseCumulativeProbability(1-tail);
	}
}
