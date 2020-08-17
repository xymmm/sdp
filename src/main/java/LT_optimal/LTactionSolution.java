package LT_optimal;

public class LTactionSolution {
	
	public int LT;
	public int Q1;
	public int Q2;
	public double minCost;
	
	public LTactionSolution(double minCost, int LT, int Q1, int Q2) {
		this.LT = LT;
		this.Q1 = Q1;
		this.Q2 = Q2;
		this.minCost = minCost;
	}
	
	public static LTactionSolution actionsIndex(double[][][] totalCost) {
		double minCost = totalCost[0][0][0];
		int LTindex = 0;
		int Q1index = 0;
		int Q2index = 0;
		for(int l=0; l<totalCost.length; l++) {
			for(int q1=0; q1<totalCost[0].length; q1++) {
				for(int q2=0; q2<totalCost[0][0].length; q2++) {
					if(totalCost[l][q1][q2]<minCost) {
						minCost = totalCost[l][q1][q2];
						LTindex = l; Q1index = q1; Q2index = q2;
					}
				}
			}
		}		
		return new LTactionSolution(minCost, LTindex, Q1index, Q2index);
	}

}
