package lateralTransshipment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.DoubleStream;

public class LT2locations {

	/**
	 * Consider a 3-period inventory control problem. At the beginning of each period the firm should decide how many units of a 
	 * product should be produced. 
	 * 
	 * If production takes place for x units, where x {@literal >} 0, we incur a production cost c(x). 
	 * This cost comprises both a fix and a variable component: c(x) = 0, if x = 0; c(x) = 3+2x, otherwise. (ordering cost)
	 * 
	 * Production in each period cannot exceed 4 units. (maxquantity of Q)
	 * 
	 * Demand in each period takes two possible values: 1 or 2 units with equal 
	 * probability (0.5). Demand is observed in each period only after production has occurred. 
	 * 
	 * After meeting current period's demand holding cost of $1 per unit is incurred for any item that is carried over from one period to the next. 
	 * 
	 * Because of limited capacity the inventory at the end of each period cannot exceed 3 units. All demand should be met on time 
	 * (no backorders). If at the end of the planning horizon (i.e. period 3) the firm still has units in stock, these can be 
	 * salvaged at $2 per unit. The initial inventory is 1 unit.
	 * 
	 * @author Roberto Rossi
	 *
	 *
	 * backorders, retrieve optimal actions on transshipment and regular orders
	 */


	int planningHorizon;
	double[][][] pmf1;
	double[][][] pmf2;

	public LT2locations(int planningHorizon,
			double[][][] pmf1,
			double[][][] pmf2) {
		this.planningHorizon = planningHorizon;
		this.pmf1 = pmf1;
		this.pmf2 = pmf2;
	}								//input lot-sizing instance with planning horizon and demand probabilities (with demand units)

	/****************************** create class for States ***********************************************************/

	class State{
		public int period;
		public int initialInventoryA;
		public int initialInventoryB;

		public State(int period, int initialInventoryA, int initialInventoryB){
			this.period = period;
			this.initialInventoryA = initialInventoryA;
			this.initialInventoryB = initialInventoryB;

		}

		public int[] getFeasibleActions(){//apply lateral transshipment actions
			return actionGenerator.apply(this);
		}//function awaits

		@Override
		public int hashCode(){
			String hash = "";
			hash = (hash + period) + "_" + this.initialInventoryA + " " + this.initialInventoryB;
			return hash.hashCode();
		}//done 

		@Override
		public boolean equals(Object o){
			if(o instanceof State)
				return ((State) o).period == this.period &&
				((State) o).initialInventoryA == this.initialInventoryA &&
				((State) o).initialInventoryB == this.initialInventoryB;
			else
				return false;
		}				//find state

		@Override
		public String toString(){
			return this.period + " " + this.initialInventoryA + " " + this.initialInventoryB;
		}

		/* state(1, 125, 65) to (int) period = 1, (int) state.A = 125, (int) state.B = 65 */
		public State parse(String state) {
			String[] str = state.split(" ");
			int[] levels = new int[2];					//store inventory levels of 2 locations
			int period = Integer.parseInt(str[0]);		//store current stage, actual number (not index)
			for(int i = 1; i< str.length; i++) {
				levels[i] = Integer.parseInt(str[i]);
			}
			return new State(period, levels[0],levels[1]);
		}
	}//end class State


	/*************************** generate actions and compute cost ********************************************************/

	Function<State, int[]> actionGenerator;	//for a given state, generate a feasible action

	
	@FunctionalInterface
	interface StateTransitionFunction <S, A, R1, R2> { //state, action, demand for location 1, demand for location 2
		public S apply (S s, A a, R1 r, R2 p);
	}
	public StateTransitionFunction<State, Integer[], Double, Double> stateTransition;

	@FunctionalInterface
	//only immediate cost - holding or penalty cost
	interface ImmediateValueFunction <S> { //state, value
		public S apply (S s);
	}
	public ImmediateValueFunction<State> immediateValueFunction;

	@FunctionalInterface
	interface ActionValueFuction<S, A>{
		public S apply(S s, int[] a);
	}
	public ActionValueFuction<State, int[]> actionValueFunction;

	Map<State, Double> cacheActions = new HashMap<>();
	Map<State, Double> cacheValueFunction = new HashMap<>();
	double f(int periodIndex, State state, LTinstance instance){
		return cacheValueFunction.computeIfAbsent(state, s -> {
			double val= Arrays.stream(s.getFeasibleActions())
						.map(action -> p[1]*actionValueFunction.apply(s, action) + //action cost (ordering and transshipping)
								Arrays.stream(pmf1[periodIndex])
										.mapToDouble(p -> p[1]*immediateValueFunction.apply(s))+ 
									   Arrays.stream(pmf2[periodIndex])
										.mapToDouble(p -> p[1]*immediateValueFunction.apply(s))+
									(s.period < this.planningHorizon ?
											p[1]*f(stateTransition.apply(s, action, p[0])) : 0)
							.sum())
								.min()
									.getAsDouble();
			double bestOrderQty = Arrays.stream(s.getFeasibleActions())
					.filter(orderQty -> Arrays.stream(pmf)
							.mapToDouble(p -> p[1]*immediateValueFunction.apply(s, orderQty, p[0])+
									(s.period < this.planningHorizon ?
											p[1]*f(stateTransition.apply(s, orderQty, p[0])):0))
							.sum() == val)
					.findAny()
					.getAsDouble();
			cacheActions.putIfAbsent(s, bestOrderQty);
			return val;
		});
	}

	/***********************************************************************************************/


	public static void main(String [] args){

		/*int planningHorizon = 3;         //Planning horizon length
		double fixedProductionCost = 3;  //Fixed production cost
		double perUnitProductionCost = 2;//Per unit production cost
		int warehouseCapacity = 3;      //Production capacity
		double holdingCost = 1;          //Holding cost
		double salvageValue = 2;         //Salvage value
		int maxOrderQty = 4;             //Max order quantity
		 */

		int[] demandMean1 = {2,4};
		int[] demandMean2 = {6, 4};
		int maxInventory  = 10;
		int minInventory  = -10;
		int maxQuantity   = 10;
		double K = 10;
		double z = 0;
		double R = 5;
		double v = 0;
		double h = 1;
		double b = 3; 
		double tail = 0.000000000001;

		LTinstance instance = new LTinstance(demandMean1,demandMean2,maxInventory,minInventory,maxQuantity,K,z,R,v,h,b,tail);

		/**
		 * Probability mass function: Demand in each period takes two possible values: 1 or 2 units 
		 * with equal probability (0.5).
		 */
		double[][][] pmf1 = null; 
		double[][][] pmf2 = null;
		int[] maxDemand1 = new int[instance.demandMean1.length];
		int[] maxDemand2 = new int[instance.demandMean1.length];
		int[] minDemand1 = new int[instance.demandMean1.length];
		int[] minDemand2 = new int[instance.demandMean1.length];
		for(int t=0; t<instance.demandMean1.length; t++) {
			maxDemand1[t] = (int) Arrays.stream(pmf1[t]).mapToDouble(v -> v[0]).max().getAsDouble();
			maxDemand2[t] = (int) Arrays.stream(pmf2[t]).mapToDouble(v -> v[0]).max().getAsDouble();
			minDemand1[t] = (int) Arrays.stream(pmf1[t]).mapToDouble(v -> v[0]).min().getAsDouble();
			minDemand2[t] = (int) Arrays.stream(pmf2[t]).mapToDouble(v -> v[0]).min().getAsDouble();
		}

		LT2locations inventory = new LT2locations(planningHorizon, pmf1, pmf2);

		/**
		 * This function returns the set of actions associated with a given state
		 */
		inventory.actionGenerator = state ->{
			int minQ = Math.max(maxDemand - state.initialInventory, 0);
			return DoubleStream.iterate(minQ, orderQty -> orderQty + 1)
					.limit(Math.min(maxOrderQty, instance.maxInventory +  
							minDemand - state.initialInventory - minQ) + 1)
					.toArray();
		};//generateFeasibleActions

		/**
		 * State transition function; given a state, an action and a random outcome, the function
		 * returns the future state
		 */
		inventory.stateTransition = (state, action, randomOutcome1, randomOutcome2) -> 
		inventory.new State(state.period + 1, 
				(int) (state.initialInventoryA + action[0] + action[1] - randomOutcome1),
				(int) (state.initialInventoryB - action[0] + action[2] - randomOutcome2)
				);

		/**
		 * Immediate value function for a given state
		 */
		inventory.immediateValueFunction = (state) -> {
			double immediateCost = ((state.initialInventoryA >= 0) ? (h*state.initialInventoryA) : (-b * state.initialInventoryA)) 
					+ ((state.initialInventoryB >= 0) ? (h*state.initialInventoryB) : (-b * state.initialInventoryB));
			//return cost;
		};
		
		inventory.actionValueFunction = (state, action) ->{
			double actionCost = ((action[0] > 0) ? (instance.R + instance.v * Math.abs(action[0])) : 0)
									+ ((action[1]>0) ? (instance.K + instance.z*action[1]) : 0)
										+ ((action[2]>0) ? (instance.K + instance.z*action[2]) : 0);
		};

			
	

		/**
		 * Initial problem conditions
		 */
		int initialPeriod = 1;
		int initialInventoryA = 1;
		int initialInventoryB = 1;
		State initialState = inventory.new State(initialPeriod, initialInventoryA, initialInventoryB);

		/**
		 * Run forward recursion and determine the expected total cost of an optimal policy
		 */
		System.out.println("f_1("+initialInventoryA+", "+initialInventoryB+")="+inventory.f(0, initialState, instance));
		/**
		 * Recover optimal action for period 1 when initial inventory at the beginning of period 1 is 1.
		 */
		System.out.println("b_1("+initialInventoryA+", "+initialInventoryB+")="+inventory.cacheActions.get(inventory.new State(initialPeriod,  initialInventoryA, initialInventoryB)));
	}

}
