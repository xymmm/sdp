package lateralTransshipment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

import org.apache.commons.math3.distribution.PoissonDistribution;

import lateralTransshipment.LT2locations.State;
import sQ.simulation.sQsimInstanceDouble;
import sS.sSsolution;
import umontreal.ssj.probdist.PoissonDist;
import umontreal.ssj.randvar.PoissonGen;
import umontreal.ssj.randvar.RandomVariateGenInt;
import umontreal.ssj.rng.MRG32k3a;
import umontreal.ssj.stat.Tally;

public class LT2locationsBackwards {


	/**generate demand & probability matrix**/
	//generatePMF(int[] demandMean1, int[] demandMean2, double tail)

	//not used
	public static double[][] generateDemandMatrix(LTinstance instance, int t){


		//		for(int t=0; t<instance.demandMean1.length; t++) {
		PoissonDistribution dist1 = new PoissonDistribution(instance.demandMean1[t]);
		PoissonDistribution dist2 = new PoissonDistribution(instance.demandMean2[t]);

		int maxDemand1 = dist1.inverseCumulativeProbability(instance.tail);
		int maxDemand2 = dist2.inverseCumulativeProbability(instance.tail);

		double[][] demand = new double[(maxDemand1 + 1)*(maxDemand2+1)][3];

		ArrayList<double[]> demandList = new ArrayList<double[]>();
		for(int i=0; i<=maxDemand1; i++) {
			for(int j=0; j<=maxDemand2; j++) {
				double[] pairs = {dist1.probability(i)*dist2.probability(j), i,j};	demandList.add(pairs);
			}
		}
		for(int l=0; l<demandList.size();l++) {
			demand[l] = demandList.get(l);
		}	
		//		}

		return demand;
	}

	/** compute immediate cost (holding and penalty) **/
	public static double computeImmediateCost(LTinstance instance, int[] closingState) {
		return ((closingState[0] >= 0) ? 
				instance.h*closingState[0] : (-instance.b)*closingState[0])
				+ ((closingState[1] >=0 ) ? 
						instance.h*closingState[1] : (-instance.b)*closingState[1]);
	}

	/** compute action cost **/
	public static double computeActionCost(LTinstance instance, int[] action) {
		return (Math.abs(action[0]) > 0 ? instance.R + instance.v*Math.abs(action[0]) : 0 )
				+(action[1] > 0 ? instance.K + action[1] * instance.v : 0)
				+(action[2] > 0 ? instance.K + action[2] * instance.v : 0);
	}

	/** generate all states **/
	public static int[][] generateInventoryPairs(LTinstance instance){
		int pairsCount = (instance.maxInventory - instance.minInventory + 1)*(instance.maxInventory - instance.minInventory + 1);		
		int[][] inventoryPairs = new int [pairsCount][2];	
		ArrayList<int[]> inventoryPairsList = new ArrayList<int[]>();
		for(int i=instance.minInventory; i<=instance.maxInventory; i++) {
			for(int j=instance.minInventory; j<=instance.maxInventory; j++) {
				int[] pairs = {i,j};	inventoryPairsList.add(pairs);
			}
		}
		for(int i=0; i<inventoryPairsList.size(); i++) {
			inventoryPairs[i] = inventoryPairsList.get(i);
		}
		return inventoryPairs;
	}

	/** get pair index **/
	public static int getStateIndex(int[][] inventoryPairs, int[] inventory) {
		int idx = 0;
		for(int i=0; i<inventoryPairs.length; i++) {
			if(inventoryPairs[i][0] == inventory[0] && inventoryPairs[i][1] == inventory[1]) idx = i;
		}
		return idx;
	}

	public static int[][] generateFeasibleActions(int[] state, LTinstance instance){
		ArrayList<int[]> actions = new ArrayList<int[]>();
		int[] newState = null;

		if((state[0] <=0)&&(state[1] <= 0)) {//case 4: no transshipment
			//			System.out.println("case 4");
			for(int i=0; i <= instance.maxInventory - state[0]; i++) {
				for(int j=0; j <= instance.maxInventory - state[1]; j++) {
					if((i<=instance.maxQuantity)&&(j<=instance.maxQuantity)) actions.add(new int[] {0, i, j});
				}
			}
			int[][] action = new int[actions.size()][3];
			for(int i=0; i<actions.size(); i++) {action[i] = actions.get(i);}
			return action;
		}else if((state[0] <=0)&&(state[1] > 0)) {//case 3: transship from 2 to 1, transshipment <= 0
			//			System.out.println("case 3");
			for(int t=0; t<=state[1]; t++) {
				newState = new int[] {state[0] + t, state[1] - t};	//update inventory level after transshipment
				for(int i=0; i <= instance.maxInventory - state[0]; i++) {
					for(int j=0; j <= instance.maxInventory - state[1]; j++) {
						if((i<=instance.maxQuantity)&&(j<=instance.maxQuantity)) actions.add(new int[] {-t, i, j});
					}
				}
			}
			int[][] action = new int[actions.size()][3];
			for(int i=0; i<actions.size(); i++) {action[i] = actions.get(i);}
			return action;
		}else if((state[0] > 0)&&(state[1] <= 0)) {//case 2: transship from 1 to 2, transship >= 0
			//			System.out.println("case 2");
			for(int t=0; t<=state[0]; t++) {
				newState = new int[] {state[0] - t, state[1] + t};	//update inventory level after transshipment
				for(int i=0; i <= instance.maxInventory - state[0]; i++) {
					for(int j=0; j <= instance.maxInventory - state[1]; j++) {
						if((i<=instance.maxQuantity)&&(j<=instance.maxQuantity)) actions.add(new int[] {t, i, j});
					}
				}
			}
			int[][] action = new int[actions.size()][3];
			for(int i=0; i<actions.size(); i++) {action[i] = actions.get(i);}
			return action;
		}else {										//case 1: transhsip undirected
			//int[] feasibleTransshipment = new int[state.i1 + state.i2 +1];
			//			System.out.println("case 1");
			for(int t=-state[1]; t<= state[0]; t++) {				
				newState = new int[] { state[0]-t, state[1]+t};
				for(int i=0; i <= instance.maxInventory - newState[0]; i++) {
					for(int j=0; j <= instance.maxInventory - newState[1]; j++) {
						if((i<=instance.maxQuantity)&&(j<=instance.maxQuantity)) actions.add(new int[] {t, i, j});
					}
				}
			}
			int[][] action = new int[actions.size()][3];
			for(int i=0; i<actions.size(); i++) {action[i] = actions.get(i);}
			return action;
		}
	}


	/************************************************************************************************************************************/
	static MRG32k3a randomStream = new MRG32k3a();

	static {
		long seed[] = {1234,1234,1234,1234,1234,1234};
		randomStream.setSeed(seed);
	}

	static int[] generateDemand(int[] demandMean) {
		RandomVariateGenInt genDemand;
		int[] demand = new int[demandMean.length];
		for(int t=0; t<demandMean.length; t++) {  
			genDemand = new PoissonGen(randomStream, new PoissonDist(demandMean[t])); 
			demand[t] = -genDemand.nextInt();
		}
		return demand;
	}

	public static Tally statCost = new Tally("stats on cost");

	public static double LTsim(int[] initialState, LTinstance instance, boolean print,
			int[][] inventoryPairs, int[][][] optimalAction){	

		int[] state = initialState;

		if(print) System.out.println("---------------------------------------------------");

		System.out.println("input initialState"+Arrays.toString(state));

		int[] demand1 = generateDemand(instance.demandMean1);
		int[] demand2 = generateDemand(instance.demandMean2);

		double cost = 0;

		for(int t=0; t<instance.demandMean1.length; t++) {
			if(print) System.out.println("period "+(t+1)+", state = "+Arrays.toString(state));

			int stateIndex = getStateIndex(inventoryPairs, state);
			int[] action = optimalAction[t][stateIndex];
			if(print) System.out.println("optimal action = "+Arrays.toString(action));

			state[0] = state[0] - action[0] + action[1] <= instance.maxInventory ? state[0] - action[0] + action[1] : instance.maxInventory;
			state[1] = state[1] + action[0] + action[2] <= instance.maxInventory ? state[1] + action[0] + action[2] : instance.maxInventory;
			if(print) System.out.println("state after action = "+Arrays.toString(state));
			cost+= computeActionCost(instance, action);
			if(print) System.out.println("cumulative cost = "+cost);

			if(print) System.out.println("demand = ["+demand1[t]+","+demand2[t]+"]");
			state[0] = state[0] + demand1[t] >= instance.minInventory ? state[0] + demand1[t] : instance.minInventory;
			state[1] = state[1] + demand2[t] >= instance.minInventory ? state[1] + demand2[t] : instance.minInventory;
			if(print) System.out.println("state after demand = "+Arrays.toString(state));
			cost+= computeImmediateCost(instance, state);
			if(print) System.out.println("cumulative cost = "+cost);

		}
		return cost;

	}

	public static void LTsimMultipleRuns(int[] initialState, LTinstance instance, int count, boolean print,
											int[][] inventoryPairs, int[][][] optimalAction) {
		for(int i=0; i<count; i++) {
			statCost.add(LTsim(initialState, instance, print,
					inventoryPairs, optimalAction));
		}
		statCost.setConfidenceIntervalStudent();
		System.out.println(statCost.report(0.9, 3));
		//System.out.println(statCost.average());
	}

	
	public static void printLTsolution(LTsolution solution) {
		System.out.println("------------------optimal cost------------------");
		for(int i=0; i<solution.inventoryPairs.length; i++) {
			System.out.print(solution.inventoryPairs[i][0]+","+solution.inventoryPairs[i][1]+ "\t");
			for(int t=0; t<solution.optimalCost.length; t++) {
				System.out.print(solution.optimalCost[t][i] + "\t");
			}
			System.out.println();
		}

		System.out.println("------------------optimal action------------------");
		for(int i=0; i<solution.inventoryPairs.length; i++) {
			System.out.print(solution.inventoryPairs[i][0]+","+solution.inventoryPairs[i][1]+ "\t");
			for(int t=0; t<solution.optimalCost.length; t++) {
				System.out.print(Arrays.toString(solution.optimalAction[t][i]) + "\t");
			}
			System.out.println();
		}
	}




/************************************************************************************************************************************/
public static LTsolution computeLTinstance(LTinstance instance, boolean initialOrder) {
	int Stages = instance.demandMean1.length;		
	int[][] inventoryPairs = generateInventoryPairs(instance);

	//demand[t][demandPairCount][3] ->{prob*prob, demand 1, demand 2}
	double[][][] demand = LT2locations.generatePMF(instance.demandMean1, instance.demandMean2, instance.tail);	
	//		double[][][] demand = generateDemandMatrix(instance);

	int[][][] optimalAction = new int [Stages][inventoryPairs.length][3];
	double[][] optimalCost = new double [Stages][inventoryPairs.length]; 
	double[][] totalCost = null;

	for(int t=Stages-1;t>=0;t--) { 	

		for(int i=0;i<inventoryPairs.length;i++) { 

			int[][] actions =  generateFeasibleActions(inventoryPairs[i], instance);
			totalCost = new double [inventoryPairs.length][actions.length];

			for(int a=0; a<actions.length;a++) {

				double scenarioProb = 0;
				totalCost[i][a] = computeActionCost(instance, actions[a]);
				//					assert totalCost[i][a] >= 0;

				for(int d=0;d<demand[t].length;d++) { // Demand
					if(
							(inventoryPairs[i][0] - actions[a][0] + actions[a][1] - demand[t][d][1] <= instance.maxInventory) 
							&& (inventoryPairs[i][0] - actions[a][0] + actions[a][1] - demand[t][d][1] >= instance.minInventory)
							&& (inventoryPairs[i][1] + actions[a][0] + actions[a][2] - demand[t][d][2] <= instance.maxInventory) 
							&& (inventoryPairs[i][1] + actions[a][0] + actions[a][2] - demand[t][d][2] >= instance.minInventory)
							) {
						//						   assert optimalCost[i+a-d][t+1] >= 0;
						int[] closingState = {(int) (inventoryPairs[i][0] - actions[a][0] + actions[a][1] - demand[t][d][1]),
								(int) (inventoryPairs[i][1] + actions[a][0] + actions[a][2] - demand[t][d][2])};
						int closingStateIndex = getStateIndex(inventoryPairs, closingState);
						totalCost[i][a] += demand[t][d][0]*(
								computeImmediateCost(instance, closingState)
								+((t==Stages-1) ? 0 : optimalCost[t+1][closingStateIndex])
								);


						scenarioProb += demand[t][d][0];
					}//else, we do nothing - not added in scenarios
				}
				totalCost[i][a] = totalCost[i][a]/scenarioProb;
			}
			optimalCost[t][i] = sdp.util.globleMinimum.getGlobalMinimum(totalCost[i]);
			int optimalActionIdx = sdp.util.globalMinimumIndex.getGlobalMinimumJavaIndex(totalCost[i]);
			optimalAction[t][i] = actions[optimalActionIdx];
		}
	}

/*
	System.out.println();
	System.out.println("------------------simulation------------------");
	int[] initialState = {1,5};
	int idx = getStateIndex(inventoryPairs, initialState);
	System.out.println("optimal cost of state " + Arrays.toString(initialState) + ": "+optimalCost[0][idx]);

	LTsimMultipleRuns(initialState, instance, 2, true,
			inventoryPairs, optimalAction);
	//		System.out.println(Arrays.toString(simulationCost));
	 * */

	return new LTsolution(inventoryPairs, optimalAction, optimalCost);

}



public static void main(String[] args) {
	int[] demandMean1 = {1,  2};
	int[] demandMean2 = {2, 3};
	int maxInventory  = 5;
	int minInventory  = -5;
	int maxQuantity   = 6;
	double K = 10;
	double z = 0;
	double R = 5;
	double v = 0;
	double h = 1;
	double b = 3; 
	double tail = 0.0001;

	LTinstance instance = new LTinstance(demandMean1,demandMean2,maxInventory,minInventory,maxQuantity,K,z,R,v,h,b,tail);

	boolean initialOrder = true;

	LTsolution solution = computeLTinstance(instance, initialOrder);
	
	printLTsolution(solution);
	


}

}
