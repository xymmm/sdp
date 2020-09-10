package lateralTransshipment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.math3.distribution.PoissonDistribution;

public class LT2locations {

	/**
	 * Non-stationary stochastic lot sizing problem with lateral transshipment for 2-location system.
	 * 
	 * Inventory at 2 locations can be replenished from the regular supplier (warehouse) but also proactive transshipment between locations.
	 * 
	 * Events order: for any period,
	 * 		check inventory levels (i1, i2) -> transship between 2 locations -> order replenishment -> meet demand.
	 * 
	 * For a state (t, i1, i2), actions include: 
	 * 		quantity of transshipment, quantity of replenishment orders at location 1 and 2 (presented in an integer array).
	 * 
	 * Action cost occurs when transshipping and reordering take places: charged a fixed cost for any action, and a linear unit ordering cost for ordered quantity.
	 * Immediate cost occurs at the end of each time period: holding cost for outstanding inventory, or penalty cost for backorders.
	 * 
	 * For programming, inventory is set within certain interval [minInventory, maxInventory],
	 * To iterate feasible actions, order quantity is also set to with a upper limitation: maxQuantity.
	 * 		One needs to make sure that the optimal action does not reaches this upper bound (maxQuantity).
	 **/


	int planningHorizon;
	double[][][] pmf;		//pmf[t]: [	[prob, demand1, demand2],[],...[]	]

	public LT2locations(int planningHorizon,
			double[][][] pmf) {
		this.planningHorizon = planningHorizon;
		this.pmf = pmf;
	}								

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

		public int[][] getFeasibleActions(){//all actions
			return actionGenerator.apply(this);
		}

		@Override
		public int hashCode(){
			String hash = this.toString();
			return hash.hashCode();
		}

		@Override
		public String toString(){
			return this.period + "_" + this.initialInventoryA + "_" + this.initialInventoryB;
		}

		@Override
		public boolean equals(Object o){
			if(o instanceof State)
				return ((State) o).period == this.period &&
				((State) o).initialInventoryA == this.initialInventoryA &&
				((State) o).initialInventoryB == this.initialInventoryB;
			else
				return false;
		}



		/* state(1, 125, 65) to (int) period = 1, (int) state.A = 125, (int) state.B = 65 */
		public State parse(String state) {
			String[] str = state.split("_");
			int[] levels = new int[2];					//store inventory levels of 2 locations
			int period = Integer.parseInt(str[0]);		//store current stage, actual number (not index)
			for(int i = 1; i< str.length; i++) {
				levels[i] = Integer.parseInt(str[i]);
			}
			return new State(period, levels[0], levels[1]);
		}
	}//end class State


	/*************************** generate actions and compute costs ********************************************************/

	Function<State, int[][]> actionGenerator;	//for a given state, generate a feasible action as an integer array: State -> int[][3]        

	@FunctionalInterface
	interface StateTransitionFunction <S, A, R> { //state, action, demand for location 1, demand for location 2
		public S apply (S s, A a, R r);
	}
	public StateTransitionFunction<State, Integer[], Double[]> stateTransition;	

	@FunctionalInterface
	interface ImmediateValueFunction <S, A, R> { //state, value
		public S apply (S s, A a, R r);
	}
	public ImmediateValueFunction<State, Integer[], Double[]> immediateValueFunction;



	/******************************************* recursion **************************************************************/
	Map<State, int[]> cacheActions = new HashMap<>();
	Map<State, Double> cacheValueFunction = new HashMap<>();
	double f(State state){
		return cacheValueFunction.computeIfAbsent(state, s -> {
			double val= Arrays.stream(s.getFeasibleActions())
					.map(action -> Arrays.stream(pmf)
							.mapToDouble(p -> p[1]*immediateValueFunction.apply(s, action, p[0])+
									(s.period < this.planningHorizon ?
											p[1]*f(stateTransition.apply(s, action, p[0])) : 0))
							.sum())
					.min()
					.getAsDouble();
			double bestAction = Arrays.stream(s.getFeasibleActions())
					.filter(orderQty -> Arrays.stream(pmf)
							.mapToDouble(p -> p[1]*immediateValueFunction.apply(s, action, p[0])+
									(s.period < this.planningHorizon ?
											p[1]*f(stateTransition.apply(s, action, p[0])):0))
							.sum() == val)
					.findAny()
					.getAsDouble();
			cacheActions.putIfAbsent(s, bestAction);
			return val;
		});
	}

	/************************************ generate demand pairs and probabilities (pmf) ***********************************************************/
	public static double[][][] generatePMF(int[] demandMean1, int[] demandMean2, double tail){
		double[][][] pmf = null;
		double[][] demandPairs = null;
		double[][] demandPairsProb = null;
		
		for(int t=0; t<demandMean1.length; t++) {			
			PoissonDistribution dist1 = new PoissonDistribution(demandMean1[t]);
			PoissonDistribution dist2 = new PoissonDistribution(demandMean2[t]);
			
			//demand pairs as a double array
			Stream<Integer> stream1 = IntStream.range(0, dist1.inverseCumulativeProbability(1-tail)).boxed();
			Stream<Integer> stream2 = IntStream.range(0, dist2.inverseCumulativeProbability(1-tail)).boxed();
			ArrayList<Integer[]> pairs = new ArrayList<>();
			//pairs = stream1.map(level1 -> stream2.map(level2 -> Stream.of(level1, level2)))
			// .collect(Collectors.toList());
			
			//prob
			for(int i=0; i<demandPairs[t].length;i++) {
				demandPairsProb[t][i] = (dist1.probability((int)demandPairs[t][0]) * dist2.probability((int)demandPairs[t][1])) 
					/ (dist1.cumulativeProbability(dist1.inverseCumulativeProbability(1-tail)) * dist2.cumulativeProbability(dist2.inverseCumulativeProbability(1-tail)));
				
				//merge
				pmf[t][i][0] = demandPairs[t][0]; pmf[t][i][1] = demandPairs[t][1]; pmf[t][i][2] = demandPairsProb[t][i];
			}
		}
		
	}
	


	/***********************************************************************************************/


	public static void main(String [] args){

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
		double[][][] pmf = generatePMF(demandMean1, demandMean2, tail);

		LT2locations inventory = new LT2locations(planningHorizon, pmf);

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
		inventory.stateTransition = (state, action, randomOutcome) -> 
		inventory.new State(state.period + 1, 
				(int) (state.initialInventoryA + action[0] + action[1] - randomOutcome[0]),
				(int) (state.initialInventoryB - action[0] + action[2] - randomOutcome[1])
				);

		/**
		 * Immediate value function for a given state
		 */
		inventory.immediateValueFunction = (state) -> {
			double immediateCost = ((state.initialInventoryA >= 0) ? (h*state.initialInventoryA) : (-b * state.initialInventoryA)) 
					+ ((state.initialInventoryB >= 0) ? (h*state.initialInventoryB) : (-b * state.initialInventoryB));
			//return cost;
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
		System.out.println("f_1("+initialInventoryA+", "+initialInventoryB+")="+inventory.f(initialState));
		/**
		 * Recover optimal action for period 1 when initial inventory at the beginning of period 1 is 1.
		 */
		System.out.println("b_1("+initialInventoryA+", "+initialInventoryB+")="+inventory.cacheActions.get(inventory.new State(initialPeriod,  initialInventoryA, initialInventoryB)));
	}

}
