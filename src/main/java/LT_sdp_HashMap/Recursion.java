package LT_sdp_HashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Recursion {

	public static LToptimalSolution ForwardRecursion (LTinstance instance, int currentStageIndex, double[][] optimalETC){
		
		System.out.println("stage = "+ (currentStageIndex+1));
		
		/** input *
		int[] demandMean = {1, 1, 3, 1};
		StateSpace stateSpace = new StateSpace(-6,6);
		int maxQuantity = 5;
		ImmediateCost costPara = new ImmediateCost(15,0,10,0,1,5);
		//HashMap<int[], Double> LT = new HashMap<int[], Double>();

		//State initialState = new State(0,0);
		double tail = 0.000001;*/
		//int[] maxDemand = new int[instance.demandMean.length];
		//for(int d=0; d<instance.demandMean.length; d++) {
			int maxDemand = TransitionProbability.getMaxDemand(instance.tail, instance.demandMean[currentStageIndex]);
		//}

		List<int[]> allStates = StateSpace.generateStateSpace(instance.stateSpace);

		int[] optimalTransshipment = new int[allStates.size()];
		int[] optimalQA = new int[allStates.size()];
		int[] optimalQB = new int[allStates.size()];
		//double[][] optimalETC = new double[allStates.size()][instance.demandMean.length];


		for(int l=0; l<allStates.size(); l++) {
			//for(int currentStageIndex = 0; currentStageIndex < instance.demandMean.length; currentStageIndex++) {
				State initialState = new State(allStates.get(l)[0], allStates.get(l)[1]);
				System.out.println("state: "+allStates.get(l)[0]+" "+ allStates.get(l)[1]);

				List<int[]> feasibleActions = new ArrayList<int[]>();
				Action.generateActions(feasibleActions, instance.stateSpace, initialState, instance.maxQuantity);
				double[] cost = new double[feasibleActions.size()];	//number of actions for current state

				for(int i=0; i<feasibleActions.size(); i++) {			

					for(int c=0; c<cost.length;c++) {
						Action action = new Action(feasibleActions.get(c)[0], feasibleActions.get(c)[1], feasibleActions.get(c)[2]);				
						cost[c] = CostComputation.computeTransshipmentCost(action, instance.costPara) 
								+ CostComputation.computeReorderCost(action, instance.costPara);
						//expected closing cost
						double tempCost = 0;
						double scenarioProb = 0;

						//for(int level1 = initialState.i1; level1 >= stateSpace.minInventory; level1--) {
						//for(int level2 = initialState.i2; level2 >= stateSpace.minInventory; level2--) {
						for(int d1=0; d1<=maxDemand; d1++) {
							for(int d2 = 0; d2<=maxDemand; d2++) {
								if(
										(initialState.i1-action.transshipment + action.quantityA-d1 <= instance.maxInventory)
										&&(initialState.i1-action.transshipment + action.quantityA-d1 >= instance.minInventory)
										&&(initialState.i2+action.transshipment + action.quantityB-d2 <= instance.maxInventory)
										&&(initialState.i2+action.transshipment + action.quantityB-d2 >= instance.minInventory)
										) {
									State newState = new State(initialState.i1-action.transshipment + action.quantityA-d1, 
																initialState.i2+action.transshipment + action.quantityB-d2);
									double transitionProb = TransitionProbability.computeTransitProb
											(instance.stateSpace, initialState, newState, action, instance.demandMean[currentStageIndex], instance.tail);
									//int stateIndex = stateSpace.getStateIndex(stateSpace, newState);
									int stateIndex = instance.stateSpace.getStateIndex(instance.stateSpace, initialState);
									cost[c] += transitionProb * CostComputation.computeClosingCost (newState, instance.costPara);
									scenarioProb += transitionProb;
								}
							}
						}
						//update total cost = immediate cost + E[]
						cost[c] = cost[c]/scenarioProb;//this is the expected cost for an action	
						
						//recursion!!
						//+((currentStageIndex == instance.demandMean.length-1)? (0): optimalETC[stateIndex][currentStageIndex-1]);

						
					}
				}
				//find optimal
				optimalETC[l][currentStageIndex] = sdp.util.globleMinimum.getGlobalMinimum(cost);	
				for(int c=0; c<cost.length; c++) {
					if(optimalETC[l][currentStageIndex] == cost[c]) {
						optimalTransshipment[l] = feasibleActions.get(c)[0];
						optimalQA[l] = feasibleActions.get(c)[1];
						optimalQB[l] = feasibleActions.get(c)[2];
					}
				}
			}

			//print optimal overall
			/*
			System.out.println("state:"+allStates.get(l)[0]+"\t"+allStates.get(l)[1]+"\t"
					+ Arrays.toString(optimalETC[l])+"\t"
					+ Arrays.toString(optimalTransshipment[l]) + "\t"
					+ Arrays.toString(optimalQA[l])+"\t"
					+ Arrays.toString(optimalQB[l]));
	*/
		//}
		
		return new LToptimalSolution(optimalETC, optimalTransshipment, optimalQA, optimalQB);
	}
	
	
	
	public static void main(String args[]) {
		int[] demandMean = {2};
		StateSpace stateSpace = new StateSpace(-5,5);
		int maxQuantity = 5;
		CostComputation costPara = new CostComputation(15,0,10,0,1,5);
		//HashMap<int[], Double> LT = new HashMap<int[], Double>();

		//State initialState = new State(0,0);
		double tail = 0.000001;
		
		LTinstance instance = new LTinstance(demandMean, stateSpace, maxQuantity, costPara, tail);
		
		List<int[]> allStates = StateSpace.generateStateSpace(instance.stateSpace);
		
		
		double[][] optimalETC = new double[allStates.size()][instance.demandMean.length];
		LToptimalSolution solution = ForwardRecursion (instance, 0, optimalETC);
		
		
		for(int l=0; l<allStates.size(); l++) {
			System.out.println("state:"+allStates.get(l)[0]+"\t"+allStates.get(l)[1]+"\t"
								+ Arrays.toString(solution.optimalCost[l])+"\t"
								+ solution.optimalTransshipment[l] + "\t"
								+ solution.optimalQA[l]+"\t"
								+ solution.optimalQB[l]);
		}

	}


}
