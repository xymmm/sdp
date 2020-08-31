package LT_sdp_HashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ForwardRecursion {

	public static void main(String[] args) {
		/** input **/
		int[] demandMean = {2, 4, 6, 4};
		int maxInventory = 10;
		int minInventory = -10;
		int maxQuantity = 10;
		double tail = 0.0000000001;
		double K = 10; double z = 0;
		double R = 15; double v = 0;
		double h = 1;  double b = 7;
		LTinstance instance = new LTinstance(demandMean, demandMean, maxInventory, minInventory, maxQuantity, K, z, R, v, h, b, tail);
		
		State initialState = new State(0,0);
			
		int Stage = demandMean.length;
		
		List<int[]> optimalActions = new ArrayList<int[]>();
		double[] optimalETC = new double[Stage];
		
		
		List<int[]> feasibleActions = new ArrayList<int[]>();
		feasibleActions = State.generateFeasibleActions(initialState, instance);
		double[] cost = new double[feasibleActions.size()];	//number of actions for current state

		for(int t=0; t<Stage;t++) {						
			for(int a=0; a<feasibleActions.size(); a++) {
				int[] action = feasibleActions.get(a);			
				Set<State> futureStates = new HashSet<State>();
				
				cost[a] = BuildHashTable.computeCurrentStageETC(initialState, action, instance, t, futureStates).currentStageETC;				
				//expected future cost
			
			}
		}
		//present
		for(int i=0; i<cost.length; i++) {
			System.out.println(i+"\t Action: "+Arrays.toString(feasibleActions.get(i)) + "\t"+cost[i]);
		}		
		optimalETC[0] = sdp.util.globleMinimum.getGlobalMinimum(cost);	System.out.println("otpimal ETC: "+ optimalETC[0]);
		List<Integer> optimalIndex = new ArrayList<>();
		for(int i=0; i<cost.length; i++) {
			if(cost[i] == optimalETC[0]) {
				optimalIndex.add(i);
			}
		}
		for(int i=0; i<optimalIndex.size();i++) {
			optimalActions.add(feasibleActions.get(optimalIndex.get(i)));		
			System.out.println(Arrays.toString(optimalActions.get(i)));
		}


	}
	

}
