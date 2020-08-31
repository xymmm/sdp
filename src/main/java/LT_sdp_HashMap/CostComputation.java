package LT_sdp_HashMap;

public class CostComputation {

	
	public static double computeClosingCost (State state, LTinstance instance) {
		return ((state.i1 >= 0) ? (instance.h*state.i1) : (-instance.b * state.i1)) 
				+ ((state.i2 >= 0) ? (instance.h*state.i2) : (-instance.b * state.i2));
	}
	
	public static double computeTransshipmentCost(int[] action, LTinstance instance) {
		return ((action[0] > 0) ? (instance.R + instance.v * Math.abs(action[0])) : 0);
	}
	
	public static double computeReorderCost(int[] action, LTinstance instance) {
		return(
				((action[1]>0) ? (instance.K + instance.z*action[1]) : 0)
					+ ((action[2]>0)?(instance.K + instance.z*action[2]):0)
					);
	}

}
