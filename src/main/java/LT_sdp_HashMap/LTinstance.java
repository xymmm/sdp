package LT_sdp_HashMap;

public class LTinstance {
	
	public int[] demandMean;
	public StateSpace stateSpace;
	public int maxQuantity;
	public ImmediateCost costPara;
	public double tail;
	
	public LTinstance(int[] demandMean, StateSpace stateSpace, int maxQuantity, ImmediateCost costPara, double tail) {
		this.demandMean = demandMean;
		this.stateSpace = stateSpace;
		this.maxQuantity = maxQuantity;
		this.costPara = costPara;
		this.tail = tail;
	}
}
