package sdp.main;

import sdp.dataProcess.Data;
import sdp.dataProcess.Demand;
import sdp.stage.ICostOriginalInventory;
import sdp.stage.ICostWithHistory;
import sdp.stage.ICostWithoutHistory;
import sdp.stage.impl.CostOriginalInventory;
import sdp.stage.impl.CostWithHistory;
import sdp.stage.impl.CostWithoutHistory;

public class SDP {
	private Demand demand = new Demand();
	
	public static void main(String[] args) {
		SDP sdp = new SDP();
		int stateSpace = sdp.demand.getInventory().length;
		
		
		//last stage
		ICostWithoutHistory stageFour = new CostWithoutHistory();
		double f [] = stageFour.calCostWithoutHistory(sdp.demand);
		
		//stage 3 and 2
		ICostWithHistory stageThree = new CostWithHistory();
		double answer [][] = new double [2][stateSpace];
		for(int t = Data.stage-2; t>0; t--) {
			answer[t-1] = stageThree.calCostWithHistory(sdp.demand, f, t);
			f = answer [t-1];
		}
		
		//first stage
		ICostOriginalInventory stageOne = new CostOriginalInventory();
		f = stageOne.calCostOriginalInventory(sdp.demand, f);
		

		for(int i=0; i<f.length; i++) {
			System.out.println(f[i]);
		}
		

	}

}
