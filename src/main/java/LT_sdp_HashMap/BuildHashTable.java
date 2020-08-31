package LT_sdp_HashMap;

import java.util.HashMap;
import java.util.Set;

public class BuildHashTable {

	public HashMap<int[], Integer[]> actions;
	public HashMap<int[], Double> costs;
	
	public BuildHashTable(HashMap<int[], Double> LT) {
		this.actions = new HashMap<int[], Integer[]>();
		this.costs = new HashMap<int[], Double>();
	}
	
	/**  **
	
	public static double getOptimalCost(State s){
	   return this.cost.get(bla bla)
	}
	public static double getOptimalAction(State s);
	public static void store(State, Action a, double c);
	
	/**/
	
	
	/** add key-value pair**/
	public static void addToMap(HashMap<int[], Double> LT, State initialState, int[] action, double ETC) {
		int[] key = {initialState.i1, initialState.i2, action[0], action[1], action[2]};
		LT.put(key, ETC);
	}
	
	
	
	/** compute (cost of an action + expected immediate cost: u(x) + sum(c(x)) + E[H+B]) for a state with all possible demand 
	 *  the input set 'futureStates' will be generated before knowning feasible actions, 
	 *  	elements (possible future states) will be added for a state throughout iteration.
	 *  **/
	public static costStatePair computeCurrentStageETC(State initialState, int[] action, LTinstance instance, int stageIndex, Set<State> futureStates) {
		
		//transshipping and ordering cost
		double actionCost = CostComputation.computeTransshipmentCost(action, instance) + CostComputation.computeReorderCost(action, instance);
		
		//closing cost
		double closingCost = 0;
		double scenarioProb = 0;
		double demandProb = 0;
		int maxDemand1 = TransitionProbability.getMaxDemand(instance.tail, instance.demandMean1[stageIndex]);
		int maxDemand2 = TransitionProbability.getMaxDemand(instance.tail, instance.demandMean2[stageIndex]);
		for(int d1=0; d1<=maxDemand1; d1++) {
			for(int d2 = 0; d2 <= maxDemand2; d2++) {
			if(
					(initialState.i1-action[0] + action[1]-d1 <= instance.maxInventory)
					&&(initialState.i1-action[0] + action[1]-d1 >= instance.minInventory)
					&&(initialState.i2+action[0] + action[2]-d2 <= instance.maxInventory)
					&&(initialState.i2+action[0] + action[2]-d2 >= instance.minInventory)
					)
			demandProb = TransitionProbability.computeTransitProbByDemand(instance, initialState, action, d1, d2, stageIndex);
			scenarioProb += demandProb;
			
			//add new pair of inventory levels to the set as a possible future state
			State newState = new State(initialState.i1 - d1, initialState.i2 - d2);
			futureStates.add(newState);
			
			closingCost += demandProb*CostComputation.computeClosingCost(newState, instance);
			}
		}
		closingCost = closingCost/scenarioProb;
		
		return new costStatePair(actionCost + closingCost, futureStates);
	}
	
	
	/** compute future cost: E[C_{t+1}(i, i')] for all new possible states i' that can transit from i under a given action**/
	public static double computeFutureETC(State initialState, int[] action, LTinstance instance, 
											int stageIndex, Set<State> futureStates, HashMap<int[], Double> LT) {
		double futureCost = 0;	
		double scenarioProb = 0;
		for(int s=0; s<futureStates.size(); s++) {
			State newState = futureStates.get
			double stateProb = TransitionProbability.computeTransitProbByStates(instance, initialState, futureStates.get(s), action, stageIndex);
		}
		return futureCost;
	}
	
	/** print states when they are stored as int array*
	public static void printState(HashMap<int[], Double> map) {
		for(int i=0; i<map.size(); i++) {
			Set<int[]> statePresent = map.keySet();
			System.out.println(Arrays.deepToString(statePresent.toArray()));
		}
	}*/
	
}
