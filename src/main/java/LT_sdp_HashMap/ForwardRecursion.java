package LT_sdp_HashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ForwardRecursion {

	public static LToptimalSolution singlePeriodOneState(LTinstance instance, int currentStageIndex) {
		/** input *
		int[] demandMean = {2, 4, 6, 4};
		StateSpace stateSpace = new StateSpace(-10,10);
		int maxQuantity = 10;
		ImmediateCost costPara = new ImmediateCost(10,0,15,0,1,7);
		//HashMap<int[], Double> LT = new HashMap<int[], Double>();
		
		State initialState = new State(0,0);
		double tail = 0.000001;*/
					
		List<int[]> optimalActions = new ArrayList<int[]>();
		double optimalETC;
		
		
		List<int[]> feasibleActions = new ArrayList<int[]>();
		Action.generateActions(feasibleActions, instance.stateSpace, instance.initialState, instance.maxQuantity);
		double[] cost = new double[feasibleActions.size()];	//number of actions for current state

		for(int i=0; i<feasibleActions.size(); i++) {			
			
			for(int c=0; c<cost.length;c++) {
				Action action = new Action(feasibleActions.get(c)[0], feasibleActions.get(c)[1], feasibleActions.get(c)[2]);				
				cost[c] = ImmediateCost.computeTransshipmentCost(action, instance.costPara) 
							+ ImmediateCost.computeReorderCost(action, instance.costPara);
				//expected closing cost
				double tempCost = 0;
				for(int level1 = instance.initialState.i1; level1 >= instance.stateSpace.minInventory; level1--) {
					for(int level2 = instance.initialState.i2; level2 >= instance.stateSpace.minInventory; level2--) {
						State newState = new State(level1, level2);
						double transitionProb = TransitionProbability.computeTransitProb
										(instance.stateSpace, instance.initialState, newState, action, instance.demandMean[currentStageIndex], instance.tail);
						tempCost += transitionProb * ImmediateCost.computeClosingCost (newState, instance.costPara);
					}
				}
				//update total cost = immediate cost + E[]
				cost[c] += tempCost;//this is the expected cost for an action				
			}
		}
		//present
		for(int i=0; i<cost.length; i++) {
			System.out.println(i+"\t Action: "+Arrays.toString(feasibleActions.get(i)) + "\t"+cost[i]);
		}	
		
		//find optimal
		optimalETC = sdp.util.globleMinimum.getGlobalMinimum(cost);	System.out.println("otpimal ETC: "+ optimalETC);
		List<Integer> optimalIndex = new ArrayList<>();
		for(int i=0; i<cost.length; i++) {
			if(cost[i] == optimalETC) {
				optimalIndex.add(i);
			}
		}
		for(int i=0; i<optimalIndex.size();i++) {
			optimalActions.add(feasibleActions.get(optimalIndex.get(i)));		
			System.out.println(Arrays.toString(optimalActions.get(i)));
		}

		return new LToptimalSolution(optimalETC, optimalActions);
	}
	

}
