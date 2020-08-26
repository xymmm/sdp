package LT_sdp_HashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class singleForwardRecursion {

	public static void main(String args[]) {
		/** input **/
		int[] demandMean = {1, 2, 3, 2};
		StateSpace stateSpace = new StateSpace(-6,6);
		int maxQuantity = 5;
		ImmediateCost costPara = new ImmediateCost(10,0,8,0,1,5);
		//HashMap<int[], Double> LT = new HashMap<int[], Double>();

		//State initialState = new State(0,0);
		double tail = 0.000001;

		List<int[]> allStates = StateSpace.generateStateSpace(stateSpace);

		List<int[]> optimalActions = new ArrayList<int[]>();
		double[][] optimalETC = new double[allStates.size()][demandMean.length];


		for(int l=0; l<allStates.size(); l++) {
			for(int currentStageIndex = 0; currentStageIndex < demandMean.length; currentStageIndex++) {
				State initialState = new State(allStates.get(l)[0], allStates.get(l)[1]);

				List<int[]> feasibleActions = new ArrayList<int[]>();
				Action.generateActions(feasibleActions, stateSpace, initialState, maxQuantity);
				double[] cost = new double[feasibleActions.size()];	//number of actions for current state

				for(int i=0; i<feasibleActions.size(); i++) {			

					for(int c=0; c<cost.length;c++) {
						Action action = new Action(feasibleActions.get(c)[0], feasibleActions.get(c)[1], feasibleActions.get(c)[2]);				
						cost[c] = ImmediateCost.computeTransshipmentCost(action, costPara) 
									+ ImmediateCost.computeReorderCost(action, costPara);
						//expected closing cost
						double tempCost = 0;
						double scenarioProb = 0;
						for(int level1 = initialState.i1; level1 >= stateSpace.minInventory; level1--) {
							for(int level2 = initialState.i2; level2 >= stateSpace.minInventory; level2--) {
								State newState = new State(level1, level2);
								tempCost = ImmediateCost.computeClosingCost (newState, costPara);
								double transitionProb = TransitionProbability.computeTransitProb
										(stateSpace, initialState, newState, action, demandMean[currentStageIndex], tail);
								int stateIndex = stateSpace.getStateIndex(stateSpace, newState);
								tempCost += transitionProb * ((currentStageIndex == 0)? (0): optimalETC[stateIndex][currentStageIndex-1]) ;
								scenarioProb += transitionProb;
							}
						}
						//update total cost = immediate cost + E[]
						cost[c] += tempCost/scenarioProb;//this is the expected cost for an action				
					}
				}
				//present
				//for(int i=0; i<cost.length; i++) {
				//System.out.println(i+"\t Action: "+Arrays.toString(feasibleActions.get(i)) + "\t"+cost[i]);
				//}	

				//find optimal
				optimalETC[l][currentStageIndex] = sdp.util.globleMinimum.getGlobalMinimum(cost);	
				//System.out.println("otpimal ETC: "+ optimalETC[l][currentStageIndex]);
				/*List<Integer> optimalIndex = new ArrayList<>();
				for(int i=0; i<cost.length; i++) {
					if(cost[i] == optimalETC[l][currentStageIndex]) {
						optimalIndex.add(i);
					}
				}
				for(int i=0; i<optimalIndex.size();i++) {
					optimalActions.add(feasibleActions.get(optimalIndex.get(i)));		
					System.out.println(Arrays.toString(optimalActions.get(i)));
				}*/

			}

			//print optimal overall

			System.out.println("state:"+allStates.get(l)[0]+"\t"+allStates.get(l)[1]+"\t"
					+Arrays.toString(optimalETC[l]));

		}



	}


}
