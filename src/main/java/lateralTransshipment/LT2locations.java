package lateralTransshipment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.apache.commons.math3.distribution.PoissonDistribution;

import minlp_Poisson.simPoissonInstance;
import umontreal.ssj.probdist.PoissonDist;
import umontreal.ssj.randvar.PoissonGen;
import umontreal.ssj.randvar.RandomVariateGenInt;
import umontreal.ssj.rng.MRG32k3a;
import umontreal.ssj.stat.Tally;

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

		/** generate feasible actions for a given state **/
		public ArrayList<int[]> generateFeasibleActions(State state, LTinstance instance){
			ArrayList<int[]> actions = new ArrayList<int[]>();
			State newState = null;

			if((state.initialInventoryA <=0)&&(state.initialInventoryB <= 0)) {//case 4: no transshipment
				//System.out.println("case 4");
				for(int i=0; i <= instance.maxInventory - state.initialInventoryA; i++) {
					for(int j=0; j <= instance.maxInventory - state.initialInventoryB; j++) {
						if((i<=instance.maxQuantity)&&(j<=instance.maxQuantity)) actions.add(new int[] {0, i, j});
					}
				}
				return actions;
			}else if((state.initialInventoryA <=0)&&(state.initialInventoryB > 0)) {//case 3: transship from 2 to 1, transshipment <= 0
				//System.out.println("case 3");
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
				//System.out.println("case 2");
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
				//System.out.println("case 1");
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

		//get cost stream
		double[] costs =  
				Arrays.stream(actions).mapToDouble(action -> {
					
					//cur month demand: [][pro demandA demandB]
					double[][] curDemand = pmf[state.period - 1];
										
					double currentCost = Arrays.stream(curDemand).mapToDouble(demand -> {
						double cost = immediateValueFunction.apply(state, action, demand);
						
						//next month cost
						if(state.period < this.planningHorizon){
							State futureState = stateTransition.apply(state, action, new double[]{demand[1],demand[2]});
							double futureCost = f(futureState);
							cost = cost + demand[0]*futureCost;
						}
						
						return cost;
					}).sum();
					
				
					return currentCost ;
				}).toArray();
		//get min cost
		double minCost = Arrays.stream(costs).min().getAsDouble();
		cacheValueFunction.put(state, minCost);
		
		//get first index of min cost (probably Multiple,only select first index )
		AtomicInteger firstMinCostIdx = new AtomicInteger(0);
		Arrays.stream(costs).forEach(cost -> {
			if(cost==minCost){
				return;
			}
			firstMinCostIdx.getAndIncrement();
		});
		
		int minCostIdx = firstMinCostIdx.get();

		cacheActions.putIfAbsent(state, actions[minCostIdx]);

		return minCost;
	}

	/************************************ generate demand pairs and probabilities (pmf) ***********************************************************/
	public static double[][][] generatePMF(int[] demandMean1, int[] demandMean2, double tail){
		double[][][] pmf = new double[demandMean1.length][][];
		
		IntStream.range(0,demandMean1.length).forEach(t->{

			//maximum demand
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
	
	/***************************** simulation ******************************************************/

	static MRG32k3a randomStream = new MRG32k3a();	
	static {
		long seed[] = {1234,1234,1234,1234,1234,1234};
		randomStream.setSeed(seed);
	}	
	static int generatePoissonDemand(double demandMean) {
		RandomVariateGenInt genDemand;		  
		genDemand = new PoissonGen(randomStream, new PoissonDist(demandMean)); 
		int demand = genDemand.nextInt();
		return -demand;
	}
	
	double LTsim(State initialState, LTinstance instance){
		double cost = 0;
		int t=0;		
		do {
			int[] action = cacheActions.get(initialState);
			State replenishedState = new State(t+1, initialState.initialInventoryA-action[0]+action[1], 
										initialState.initialInventoryB + action[0] + action[2]);
			cost+= (Math.abs(action[0]) > 0 ? instance.R + instance.v*action[0] : 0) 
					+ (action[1]>0 ? instance.K + instance.z*action[1] : 0) 
					+ (action[2]>0 ? instance.K + instance.z*action[2] : 0);
			int[] demand = new int[2]; 
			demand[0] = generatePoissonDemand(instance.demandMean1[t]); demand[1] = generatePoissonDemand(instance.demandMean2[t]);
			State closingState = new State(t+2, replenishedState.initialInventoryA + demand[0], replenishedState.initialInventoryB + demand[1]);
			cost+= (closingState.initialInventoryA >= 0 ? instance.h * closingState.initialInventoryA : instance.b * closingState.initialInventoryA)
					+(closingState.initialInventoryB >= 0 ? instance.h * closingState.initialInventoryB : instance.b * closingState.initialInventoryB);
			initialState = closingState;
		}while(t<instance.demandMean1.length);
		return cost;
	}
	
	public static Tally statCost = new Tally("stats on cost");
	
	void MultipleSim(State initialState, LTinstance instance, int count){
		for(int i=0; i<count; i++) {
			statCost.add(LTsim(initialState, instance));
		}
	}




	/***************************** main ************************************************************/

	public static void main(String [] args){
		/** time record - start**/
		long timeStart = System.currentTimeMillis();
		
		int[] demandMean1 = {2, 4};
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

		double[][][] pmf = generatePMF(demandMean1, demandMean2, tail);

		LT2locations inventory = new LT2locations(demandMean1.length, pmf);

		//generateFeasibleActions
		inventory.actionGenerator = state ->{
			ArrayList<int[]> actionList = state.generateFeasibleActions(state, instance);
			int[][] action = actionList.toArray(new int[0][0]);
			return action;
		};

		//state transition
		inventory.stateTransition = (state, action, randomOutcome) -> 
		inventory.new State(state.period + 1, 
				(int) (state.initialInventoryA + action[0] + action[1] - randomOutcome[0]),
				(int) (state.initialInventoryB - action[0] + action[2] - randomOutcome[1])
				);
		
		//immediate cost * prob
		inventory.immediateValueFunction = (state, action, demand) -> {
			double prob = demand[0];			
			double cost = (Math.abs(action[0]) > 0 ? R + v*action[0] : 0) + (action[1]>0 ? K + z*action[1] : 0) + (action[2]>0 ? K+z*action[2] : 0);
			cost += ((state.initialInventoryA - action [0] + action[1] - demand[1] >=0) ? 
					h*(state.initialInventoryA - action [0] + action[1] - demand[1]) : (-b)*(state.initialInventoryA - action [0] + action[1] - demand[1]))
					+ ((state.initialInventoryB + action [0] + action[2] - demand[2] >=0) ? 
							h*(state.initialInventoryB + action [0] + action[2] - demand[2]) : (-b)*(state.initialInventoryB + action [0] + action[2] - demand[2]));
			
			cost = prob * cost;
			return cost;
		};

		//Initial problem conditions
		int initialPeriod = 1;
		int initialInventoryA = 0;
		int initialInventoryB = 0;
		State initialState = inventory.new State(initialPeriod, initialInventoryA, initialInventoryB);

		//optimal cost
		System.out.println("f("+initialInventoryA+", "+initialInventoryB+")="+inventory.f(initialState));
				
		//optimal action for period 1
		System.out.println("optAction("+initialInventoryA+", "+initialInventoryB+")="
															+Arrays.toString(inventory.cacheActions.get(initialState)));

		/** time record - end**/
		long timeEnd = System.currentTimeMillis();
		System.out.println("time consumed on Forward DP: "+(timeEnd - timeStart)/1000+"s");
		
		/****************************************************************************/
		timeStart = System.currentTimeMillis();
		int count = 1;
		MultipleSim(initialState, instance, count);
		statCost.setConfidenceIntervalStudent();
		System.out.println(statCost.report(0.9, 3));
		System.out.println(statCost.average());
		timeEnd = System.currentTimeMillis();
		System.out.println("time consumed on simulation (forward DP): "+(timeEnd - timeStart)/1000+"s");

	}

}
