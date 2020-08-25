package LT_sdp_HashMap;

import java.util.Arrays;

import org.apache.commons.math3.distribution.PoissonDistribution;

public class TransitionProbability {

	public static double computeTransitProb(StateSpace stateSpace, State state1, State state2, Action action, int demandMean, double tail) {
		PoissonDistribution dist = new PoissonDistribution(demandMean);
		int maxDemand = dist.inverseCumulativeProbability(1-tail);

		// compare with the max demand does not change the result, because infeasible actions, 
		// including infeasible transshipment and order quantities, are already excluded from the set.
		if((state1.i1 <= 0 )&&( state1.i2 <= 0)) {
			if(
					(state1.i1 + action.quantityA - state2.i1 <= maxDemand) && (state1.i1 + action.quantityA - state2.i1 >= 0)&&
					(state1.i2 + action.quantityB - state2.i2 <= maxDemand) && (state1.i2 + action.quantityB - state2.i2 >= 0)
					){
				return (dist.probability(state1.i1 + action.quantityA - state2.i1)/dist.cumulativeProbability(maxDemand))
						*(dist.probability(state1.i2 + action.quantityB - state2.i2)/dist.cumulativeProbability(maxDemand));
			}else {
				return 0;
			}			

		}else {
			if(
					(state1.i1 - action.transshipment + action.quantityA - state2.i1 <= maxDemand)&&(state1.i1 - action.transshipment + action.quantityA - state2.i1 >= 0)&&
					(state1.i2 + action.transshipment + action.quantityB - state2.i2 <= maxDemand)&&(state1.i2 + action.transshipment + action.quantityB - state2.i2 >= 0)
					) {
				return (dist.probability(state1.i1 - action.transshipment + action.quantityA - state2.i1 )/dist.cumulativeProbability(maxDemand))
						*(dist.probability(state1.i2 + action.transshipment + action.quantityB - state2.i2)/dist.cumulativeProbability(maxDemand));
			}else {
				return 0;
			}

		}


	}
}
