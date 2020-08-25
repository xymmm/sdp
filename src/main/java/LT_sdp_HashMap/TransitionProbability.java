package LT_sdp_HashMap;

import java.util.Arrays;

import org.apache.commons.math3.distribution.PoissonDistribution;

public class TransitionProbability {
	
	public static double computeTransitProb(StateSpace stateSpace, State state1, State state2, Action action, int demandMean, double tail) {
		//double[] tabulateProbs = sdp.util.Demand.tabulateProbability(meanDemand, tail);
		//System.out.println(Arrays.toString(tabulateProbs));
		PoissonDistribution dist = new PoissonDistribution(demandMean);
		if((state1.i1 <= 0 )&&( state1.i2<=0)) {
			if(
					(state1.i1 + action.quantityA - state2.i1-1 >= stateSpace.minInventory)&&(state1.i1 + action.quantityA - state2.i1-1 <= stateSpace.maxInventory)&&
					(state1.i2 + action.quantityB - state2.i2-1 >= stateSpace.minInventory)&&(state1.i2 + action.quantityB - state2.i2-1 <= stateSpace.maxInventory)
					){
				return dist.probability(state1.i1 + action.quantityA - state2.i1)*dist.probability(state1.i2 + action.quantityB - state2.i2);
			}else {
				return 0;
			}			
		}else {
			if(
					(state1.i1 - action.transshipment + action.quantityA - state2.i1 >= stateSpace.minInventory)&&(state1.i1 - action.transshipment + action.quantityA - state2.i1 <= stateSpace.maxInventory)&&
					(state1.i2 + action.transshipment + action.quantityB - state2.i2 >= stateSpace.minInventory)&&(state1.i2 + action.transshipment + action.quantityB - state2.i2 <= stateSpace.maxInventory)
					) {
				return dist.probability(state1.i1 - action.transshipment + action.quantityA - state2.i1 )*dist.probability(state1.i2 + action.transshipment + action.quantityB - state2.i2);
			}else {
				return 0;
			}
			
		}
	}
}
