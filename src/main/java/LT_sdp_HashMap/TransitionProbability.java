package LT_sdp_HashMap;

public class TransitionProbability {
	
	public static double computeTransitProb(State state1, State state2, Action action, int meanDemand, double tail) {
		double[] tabulateProbs = sdp.util.Demand.tabulateProbability(meanDemand, tail);
		if((state1.i1 <= 0 )&&( state1.i2<=0)) {
			return tabulateProbs[state1.i1 + action.quantityA - state2.i1-1]*tabulateProbs[state1.i2 + action.quantityB - state2.i2 -1];
		}else {
			return tabulateProbs[state1.i1 - action.transshipment + action.quantityA - state2.i1-1]*tabulateProbs[state1.i2 + action.transshipment + action.quantityB - state2.i2 -1];
		}
	}
}
