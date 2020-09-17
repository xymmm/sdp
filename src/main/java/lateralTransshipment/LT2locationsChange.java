package lateralTransshipment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.apache.commons.math3.distribution.PoissonDistribution;

public class LT2locationsChange {

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

	public LT2locationsChange(int planningHorizon,
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

		/** generate feasible actions for a given state **/
		public ArrayList<int[]> generateFeasibleActions(State state, LTinstance instance){
			ArrayList<int[]> actions = new ArrayList<int[]>();
			State newState = null;

			if((state.initialInventoryA <=0)&&(state.initialInventoryB <= 0)) {//case 4: no transshipment
//				System.out.println("case 4");
				for(int i=0; i <= instance.maxInventory - state.initialInventoryA; i++) {
					for(int j=0; j <= instance.maxInventory - state.initialInventoryB; j++) {
						if((i<=instance.maxQuantity)&&(j<=instance.maxQuantity)) actions.add(new int[] {0, i, j});
					}
				}
				return actions;
			}else if((state.initialInventoryA <=0)&&(state.initialInventoryB > 0)) {//case 3: transship from 2 to 1, transshipment <= 0
//				System.out.println("case 3");
				for(int t=0; t<=state.initialInventoryB; t++) {
					newState = new State(state.period, state.initialInventoryA + t, state.initialInventoryB - t);	//update inventory level after transshipment
					for(int i=0; i <= instance.maxInventory - state.initialInventoryA; i++) {
						for(int j=0; j <= instance.maxInventory - state.initialInventoryB; j++) {
							if((i<=instance.maxQuantity)&&(j<=instance.maxQuantity)) actions.add(new int[] {-t, i, j});
						}
					}
				}
				return actions;
			}else if((state.initialInventoryA > 0)&&(state.initialInventoryB <= 0)) {//case 2: transship from 1 to 2, transship >= 0
//				System.out.println("case 2");
				for(int t=0; t<=state.initialInventoryA; t++) {
					newState = new State(state.period, state.initialInventoryA - t, state.initialInventoryB + t);	//update inventory level after transshipment
					for(int i=0; i <= instance.maxInventory - state.initialInventoryA; i++) {
						for(int j=0; j <= instance.maxInventory - state.initialInventoryB; j++) {
							if((i<=instance.maxQuantity)&&(j<=instance.maxQuantity)) actions.add(new int[] {t, i, j});
						}
					}
				}
				return actions;
			}else {										//case 1: transhsip undirected
				//int[] feasibleTransshipment = new int[state.i1 + state.i2 +1];
//				System.out.println("case 1");
				for(int t=-state.initialInventoryB; t<= state.initialInventoryA; t++) {				
					newState = new State(state.period, (state.initialInventoryA)-t, (state.initialInventoryB)+t);
					for(int i=0; i <= instance.maxInventory - newState.initialInventoryA; i++) {
						for(int j=0; j <= instance.maxInventory - newState.initialInventoryB; j++) {
							if((i<=instance.maxQuantity)&&(j<=instance.maxQuantity)) actions.add(new int[] {t, i, j});
						}
					}
				}
				return actions;
			}
		}

	}//end class State


	/*************************** generate actions and compute costs ********************************************************/

	Function<State, int[][]> actionGenerator;	//for a given state, generate a feasible action as an integer array: State -> int[][3]        

	@FunctionalInterface
	interface StateTransitionFunction <S, A, R> { //state, action, demand for location 1, demand for location 2
		public S apply (S s, A a, R r);
	}
	public StateTransitionFunction<State, int[], double[]> stateTransition;	

	@FunctionalInterface
	interface ImmediateValueFunction <S, A, R> { //state, value
		public double apply (S s, A a, R r);
	}
	public ImmediateValueFunction<State, int[], double[]> immediateValueFunction;



	/******************************************* recursion **************************************************************/
	Map<State, int[]> cacheActions = new HashMap<>();
	Map<State, Double> cacheValueFunction = new HashMap<>();
	double f(State state){
		
		//this month actions
		int[][] actions = state.getFeasibleActions();
		if(actions==null || actions.length==0){
			return 0d;
		}
		//get cost stream
		double[] costs =  
				Arrays.stream(actions).mapToDouble(action -> {
					//this month cost
					int curMonthIdx = state.period - 1;
					System.out.println(state);
					
					//cur month demand: [][pro demandA demandB]
					double[][] curMonthDemands = pmf[curMonthIdx];
										
					double curMonthCost = Arrays.stream(curMonthDemands).mapToDouble(demaind -> {
						double cost = immediateValueFunction.apply(state, action, demaind);


						//next month cost
						if(state.period < this.planningHorizon){
							State nextMonthState = stateTransition.apply(state, action, new double[]{demaind[1],demaind[2]});
							double nextMonthCost = f(nextMonthState);
							cost = cost + demaind[0]*nextMonthCost;
						}
						
						return cost;
					}).sum();
					
					return curMonthCost ;
				}).toArray();
		//get min cost
		double minCost = Arrays.stream(costs).min().getAsDouble();
		cacheValueFunction.put(state,minCost);
		
		//get first index of min cost (probably Multiple,only select first index )
		AtomicInteger firstMinCostIdx = new AtomicInteger(0);
		Arrays.stream(costs).forEach(cost -> {
			if(cost==minCost){
				return;
			}
			firstMinCostIdx.getAndIncrement();
		});
		
		int minCostIdx = firstMinCostIdx.get();
		if(minCostIdx>=actions.length){
			//is error,if true,actions[minCostIdx] probably out of range Exception,cost==minCost?
		}
		cacheActions.putIfAbsent(state, actions[minCostIdx]);

		return minCost;
	}

	/************************************ generate demand pairs and probabilities (pmf) ***********************************************************/
	public static double[][][] generatePMF(int[] demandMean1, int[] demandMean2, double tail){
		double[][][] pmf = new double[demandMean1.length][][];
		
		IntStream.range(0,demandMean1.length).forEach(t->{

			PoissonDistribution dist1 = new PoissonDistribution(demandMean1[t]);
			int maxDemand1 = dist1.inverseCumulativeProbability(1-tail);
			
			PoissonDistribution dist2 = new PoissonDistribution(demandMean2[t]);
			int maxDemand2 = dist2.inverseCumulativeProbability(1-tail);
			
			double[][] curPmf = new double[maxDemand1*maxDemand2][3];
			
			IntStream.range(0,maxDemand1).forEach(demand1 -> {
				IntStream.range(0,maxDemand2).forEach(demand2 -> {
					double p = dist1.probability(demand1)*dist2.probability(demand2)/
							(dist1.cumulativeProbability(maxDemand1)*dist2.cumulativeProbability(maxDemand2));
					curPmf[demand1*maxDemand2+demand2] =new double[]{p,demand1,demand2};
				});
			});

			pmf[t] = curPmf;
		});
		
		return pmf;
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

		LT2locationsChange inventory = new LT2locationsChange(demandMean1.length, pmf);

		/**
		 * This function returns the set of actions associated with a given state
		 */
		inventory.actionGenerator = state ->{
			ArrayList<int[]> actionList = state.generateFeasibleActions(state, instance);
			int[][] result = actionList.toArray(new int[0][0]);
			if(result==null || result.length==0){
				System.out.println("no actions ?????");
			}
			return result;
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
		inventory.immediateValueFunction = (state, action, demand) -> {
			double pro = demand[0];
			
			double cost = (Math.abs(action[0]) > 0 ? R + v*action[0] : 0) + (action[1]>0 ? K + z*action[1] : 0) + (action[2]>0 ? K+z*action[2] : 0);
			cost += ((state.initialInventoryA - action [0] + action[1] - demand[1] >=0) ? 
					h*(state.initialInventoryA - action [0] + action[1] - demand[1]) : (-b)*(state.initialInventoryA - action [0] + action[1] - demand[1]))
					+ ((state.initialInventoryB + action [0] + action[2] - demand[2] >=0) ? 
							h*(state.initialInventoryB + action [0] + action[2] - demand[2]) : (-b)*(state.initialInventoryB + action [0] + action[2] - demand[2]));
			//cost -= (state.period == planningHorizon ? salvageValue : 0)*(state.initialInventory+action-demand);
			
			cost = pro * cost;
			return cost;
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
		System.out.println("b_1("+initialInventoryA+", "+initialInventoryB+")="+Arrays.toString(inventory.cacheActions.get(initialState)));
	}

}
