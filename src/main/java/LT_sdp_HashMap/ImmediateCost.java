package LT_sdp_HashMap;

public class ImmediateCost {

	public double K;
	public double z;
	public double R;
	public double v;
	public double h;
	public double b;
	
	public ImmediateCost(double K, double z, double R, double v, double h, double b) {
		this.K = K;
		this.z = z;
		this.R = R;
		this.v = v;
		this.h = h;
		this.b = b;
	}
	
	public static double computeClosingCost (State state, ImmediateCost costPara) {
		return ((state.i1 >= 0) ? (costPara.h*state.i1) : (-costPara.b * state.i1)) 
				+ ((state.i2 >= 0) ? (costPara.h*state.i2) : (-costPara.b * state.i2));
	}
	
	public static double computeTransshipmentCost(Action action, ImmediateCost costPara) {
		return ((action.transshipment > 0) ? (costPara.R + costPara.v * Math.abs(action.transshipment)) : 0);
	}
	
	public static double computeReorderCost(Action action, ImmediateCost costPara) {
		return(
				((action.quantityA>0) ? (costPara.K + costPara.z*action.quantityA) : 0)
					+ ((action.quantityB>0)?(costPara.K + costPara.z*action.quantityB):0)
					);
	}

}
