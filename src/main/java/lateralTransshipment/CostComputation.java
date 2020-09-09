package lateralTransshipment;

public class CostComputation {

	
	public static double computeClosingCost (State state, LTinstance instance) {
		return ((state.initialInventoryA >= 0) ? (instance.h*state.initialInventoryA) : (-instance.b * state.initialInventoryA)) 
				+ ((state.initialInventoryB >= 0) ? (instance.h*state.initialInventoryB) : (-instance.b * state.initialInventoryB));
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
