package LT_sdp_HashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ForwardRecursion {

	public static void main(String[] args) {
		/** input **/
		int[] demandMean = {2, 4, 6, 4};
		StateSpace stateSpace = new StateSpace(-10,10);
		int maxQuantity = 10;
		ImmediateCost costPara = new ImmediateCost(10,0,15,0,1,7);
		HashMap<int[], Double> LT = new HashMap<int[], Double>();
		
		State initialState = new State(0,0);
		double tail = 0.000001;
			
		int Stage = demandMean.length;
		
		int[] optimalTransshipment = new int[Stage];
		int[] optimalQuantityA = new int[Stage];
		int[] optimalQuantityB = new int[Stage];
		
	
		/******************** stage 1 *************************/
		//generate states
		List<int[]> allStates = StateSpace.generateStateSpace(stateSpace);
		double[] ETC = new double [allStates.size()];
		
		//for(int i=0; i<allStates.size();i++) {System.out.println(Arrays.toString(allStates.get(i)));}		
		for(int i=0; i<allStates.size(); i++) {
			
			List<int[]> keyList = new ArrayList<int[]>();
			State state = new State(allStates.get(i)[0], allStates.get(i)[1]);
			Action.addKeys(keyList, stateSpace, state, maxQuantity);
			//if(i==100) {System.out.println("state: "+state.i1+" "+state.i2); for(int j=0; j<keyList.size(); j++) {System.out.println(Arrays.toString(keyList.get(j)));}}
			
			
			double[] cost = new double[keyList.size()];	//number of actions for current state
			for(int c=0; c<cost.length;c++) {
				Action action = new Action(keyList.get(c)[2], keyList.get(c)[3], keyList.get(c)[4]);
				//if(i==100) {System.out.println("Action: "+ keyList.get(c)[2]+" "+keyList.get(c)[3]+" "+keyList.get(c)[4]);}
				
				//double transitionProb = TransitionProbability.computeTransitProb(stateSpace, initialState, state, action, demandMean[0], tail);
				//if(i==100) {System.out.println(transitionProb);}
				
				cost[c] = ImmediateCost.computeTransshipmentCost(action, costPara) 
							+ ImmediateCost.computeReorderCost(action, costPara)
							+ ImmediateCost.computeClosingCost(state, costPara);
				int[] key = new int[] {state.i1, state.i2, action.transshipment, action.quantityA, action.quantityB};
				LT.put(key, cost[c]);
				//if(i==100) {System.out.println(Arrays.toString(key)+"\t"+cost[c]);}
			}

			
		}
		/*
		for(int i=0; i<ETC.length; i++) {
			System.out.println(allStates.get(i)[0]+" " + allStates.get(i)[1]+"\t"+ETC[i]);
		}
		System.out.println(ETC.length);
		*/
		//for(int[] key: LT.keySet()){System.out.println(Arrays.toString(key) + "\t " + LT.get(key));} 

	}
	

}
