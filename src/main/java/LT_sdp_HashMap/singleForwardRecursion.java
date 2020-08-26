package LT_sdp_HashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class singleForwardRecursion {

	public static void main(String[] args) {
		/** input **/
		int[] demandMean = {1, 2, 3, 2};
		StateSpace stateSpace = new StateSpace(-6,6);
		int maxQuantity = 5;
		ImmediateCost costPara = new ImmediateCost(15,0,10,0,1,5);
		//HashMap<int[], Double> LT = new HashMap<int[], Double>();

		//State initialState = new State(0,0);
		double tail = 0.000001;
		int[] maxDemand = new int[demandMean.length];
		for(int d=0; d<demandMean.length; d++) {
			maxDemand[d] = TransitionProbability.getMaxDemand(tail, demandMean[d]);
		}

		List<int[]> allStates = StateSpace.generateStateSpace(stateSpace);

		int[][] optimalTransshipment = new int[allStates.size()][demandMean.length];
		int[][] optimalQA = new int[allStates.size()][demandMean.length];
		int[][] optimalQB = new int[allStates.size()][demandMean.length];
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
						
						//for(int level1 = initialState.i1; level1 >= stateSpace.minInventory; level1--) {
							//for(int level2 = initialState.i2; level2 >= stateSpace.minInventory; level2--) {
						for(int d1=0; d1<=maxDemand[currentStageIndex]; d1++) {
							for(int d2 = 0; d2<=maxDemand[currentStageIndex]; d2++) {
								if(
									(initialState.i1-action.transshipment + action.quantityA-d1 <= stateSpace.maxInventory)
								  &&(initialState.i1-action.transshipment + action.quantityA-d1 >= stateSpace.minInventory)
								  &&(initialState.i2+action.transshipment + action.quantityB-d2 <= stateSpace.maxInventory)
								  &&(initialState.i2+action.transshipment + action.quantityB-d2 >= stateSpace.minInventory)
										) {
								State newState = new State(initialState.i1-action.transshipment + action.quantityA-d1, 
																initialState.i2+action.transshipment + action.quantityB-d2);
								double transitionProb = TransitionProbability.computeTransitProb
										(stateSpace, initialState, newState, action, demandMean[currentStageIndex], tail);
								int stateIndex = stateSpace.getStateIndex(stateSpace, newState);
								tempCost += transitionProb * (
												ImmediateCost.computeClosingCost (newState, costPara) + 
												((currentStageIndex == 0)? (0): optimalETC[stateIndex][currentStageIndex-1])
												) ;
								scenarioProb += transitionProb;
							}
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
				for(int c=0; c<cost.length; c++) {
					if(optimalETC[l][currentStageIndex] == cost[c]) {
						optimalTransshipment[l][currentStageIndex] = feasibleActions.get(c)[0];
						optimalQA[l][currentStageIndex] = feasibleActions.get(c)[1];
						optimalQB[l][currentStageIndex] = feasibleActions.get(c)[2];
					}
				}
				
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
					+ Arrays.toString(optimalETC[l])+"\t"
					+ Arrays.toString(optimalTransshipment[l]) + "\t"
					+ Arrays.toString(optimalQA[l])+"\t"
					+ Arrays.toString(optimalQB[l]));

		}



	}


}
