package lateralTransshipment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

import org.apache.commons.math3.distribution.PoissonDistribution;

import umontreal.ssj.util.Chrono;

public class LT2locationsBackwards {


	/**generate demand & probability matrix**/
	//generatePMF(int[] demandMean1, int[] demandMean2, double tail)
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
				if(newState[0]<=instance.maxInventory && newState[0] >=instance.minInventory) {//transship does not hit boundary
					for(int i=0; i <= instance.maxInventory - state[0]; i++) {
						for(int j=0; j <= instance.maxInventory - state[1]; j++) {
							if((i<=instance.maxQuantity)&&(j<=instance.maxQuantity)) actions.add(new int[] {-t, i, j});
						}
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
				if(newState[1] <= instance.maxInventory && newState[1] >= instance.minInventory) {//transship does not hit boundary
					for(int i=0; i <= instance.maxInventory - state[0]; i++) {
						for(int j=0; j <= instance.maxInventory - state[1]; j++) {
							if((i<=instance.maxQuantity)&&(j<=instance.maxQuantity)) actions.add(new int[] {t, i, j});
						}
					}
				}
			}
			int[][] action = new int[actions.size()][3];
			for(int i=0; i<actions.size(); i++) {action[i] = actions.get(i);}
			return action;
		}else {//case 1: transhsip undirected
			//			System.out.println("case 1");
			for(int t=-state[1]; t<= state[0]; t++) {
				if(t<=0) {//transship from 1 to 2
					newState = new int[] { state[0]-t, state[1]+t};
					if(newState[1] <=instance.maxInventory && newState[1] >= instance.minInventory) {
						for(int i=0; i <= instance.maxInventory - newState[0]; i++) {
							for(int j=0; j <= instance.maxInventory - newState[1]; j++) {
								if((i<=instance.maxQuantity)&&(j<=instance.maxQuantity)) actions.add(new int[] {t, i, j});
							}
						}
					}
				}else {//transship from 2 to 1
					newState = new int[] { state[0]-t, state[1]+t};
					if(newState[0] <= instance.maxInventory && newState[0] >= instance.minInventory) {
						for(int i=0; i <= instance.maxInventory - newState[0]; i++) {
							for(int j=0; j <= instance.maxInventory - newState[1]; j++) {
								if((i<=instance.maxQuantity)&&(j<=instance.maxQuantity)) actions.add(new int[] {t, i, j});
							}
						}
					}
				}

			}
			int[][] action = new int[actions.size()][3];
			for(int i=0; i<actions.size(); i++) {action[i] = actions.get(i);}
			return action;
		}
	}

	public static int[][] generateNoOrderActions(int[] state, LTinstance instance){
		ArrayList<int[]> actions = new ArrayList<int[]>();
		int[] newState = null;

		if((state[0] <=0)&&(state[1] <= 0)) {//case 4: no transshipment
			return new int[][] {{0, 0, 0}};
		}else if((state[0] <=0)&&(state[1] > 0)) {//case 3: transship from 2 to 1, transshipment <= 0
			//			System.out.println("case 3");
			for(int t=0; t<=state[1]; t++) {
				newState = new int[] {state[0] + t, state[1] - t};	//update inventory level after transshipment
				if(newState[0] <=instance.maxInventory && newState[1] >= instance.minInventory) {
					actions.add(new int[] {-t, 0, 0});
				}
			}
			int[][] action = new int[actions.size()][3];
			for(int i=0; i<actions.size(); i++) {action[i] = actions.get(i);}
			return action;
		}else if((state[0] > 0)&&(state[1] <= 0)) {//case 2: transship from 1 to 2, transship >= 0
			//			System.out.println("case 2");
			for(int t=0; t<=state[0]; t++) {
				newState = new int[] {state[0] - t, state[1] + t};	//update inventory level after transshipment
				if(newState[1] <= instance.maxInventory && newState[1] >= instance.minInventory) {
					actions.add(new int[] {t, 0, 0});
				}
			}
			int[][] action = new int[actions.size()][3];
			for(int i=0; i<actions.size(); i++) {action[i] = actions.get(i);}
			return action;
		}else {//case 1: transhsip undirected
			//			System.out.println("case 1");
			for(int t=-state[1]; t<= state[0]; t++) {
				if(t<=0) {//transship from 1 to 2
					newState = new int[] { state[0]-t, state[1]+t};
					if(newState[1] <=instance.maxInventory && newState[1] >= instance.minInventory) {
						actions.add(new int[] {t, 0, 0});
					}
				}else {//transship from 2 to 1
					newState = new int[] { state[0]-t, state[1]+t};
					if(newState[0] <= instance.maxInventory && newState[0] >= instance.minInventory) {
						actions.add(new int[] {t, 0, 0});
					}
				}

			}
			int[][] action = new int[actions.size()][3];
			for(int i=0; i<actions.size(); i++) {action[i] = actions.get(i);}
			return action;
		}
	}

	public static int[][] generateNoTransshipActions(int[] state, LTinstance instance){
		ArrayList<int[]> actions = new ArrayList<int[]>();
		for(int i=0; i <= instance.maxInventory - state[0]; i++) {
			for(int j=0; j <= instance.maxInventory - state[1]; j++) {
				if((i<=instance.maxQuantity)&&(j<=instance.maxQuantity)) actions.add(new int[] {0, i, j});
			}
		}
		int[][] action = new int[actions.size()][3];
		for(int i=0; i<actions.size(); i++) {action[i] = actions.get(i);}
		return action;
	}

	/** print solution**/

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

	public static void writeSolution(LTsolution solution, String fileName) {
		//cost
		FileWriter fw = null;
		try {
			File f = new File(fileName);
			fw = new FileWriter(f, true);//true, continue to write
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(fw);
		for(int i=0; i<solution.inventoryPairs.length; i++) {
			pw.print(Arrays.toString(solution.inventoryPairs[i])+"\t");
			for(int t=0; t<solution.optimalCost.length;t++) {
				pw.print(solution.optimalCost[t][i]+"\t");
			}
			for(int t=0; t<solution.optimalCost.length;t++) {
				pw.print(Arrays.toString(solution.optimalAction[t][i])+"\t");
			}
			pw.println();
		}
		pw.println();
		pw.flush();
		try {
			fw.flush();
			pw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** convert solution cost matrix [Stages][inventoryPairs.length] to [inventory][inventory] and write to text**/
	public static void convertCostMatrix(LTinstance instance, LTsolution solution, int periodIndex, String fileName){
		double[][] costContour = new double[instance.maxInventory-instance.minInventory+1][instance.maxInventory-instance.minInventory+1];
		for(int i=0; i<costContour.length; i++) {
			for(int j=0; j<costContour[0].length; j++) {
				int index = i*costContour.length + j;
				costContour[i][j] = solution.optimalCost[periodIndex][index];
			}
		}
		FileWriter fw = null;
		try {
			File f = new File(fileName);
			fw = new FileWriter(f, true);//true, continue to write
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(fw);
		//first line: \t minInventory minInventory+1, ..., maxInventory
		pw.print("\t");
		for(int j=0; j<costContour.length; j++) {
			pw.print(j+instance.minInventory + "\t");
		}pw.println();
		//main body
		for(int i=costContour.length-1; i>=0; i--) {
			pw.print(i+instance.minInventory+"\t");			
			for(int j=0; j<costContour[0].length; j++) {
				pw.print(costContour[i][j]+"\t");							
			}
			pw.println();
		}
		pw.println();
		pw.flush();
		try {
			fw.flush();
			pw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	/** convert solution cost matrix [Stages][inventoryPairs.length] to [inventory][inventory] and write to text**/
	public static void convertActionMatrix(LTinstance instance, LTsolution solution, int periodIndex, String fileName){
		int[][][] ActionContour = new int[instance.maxInventory-instance.minInventory+1][instance.maxInventory-instance.minInventory+1][3];
		for(int i=0; i<ActionContour.length; i++) {
			for(int j=0; j<ActionContour[0].length; j++) {
				int index = i*ActionContour.length + j;
				ActionContour[i][j] = solution.optimalAction[periodIndex][index];
			}
		}
		FileWriter fw = null;
		try {
			File f = new File(fileName);
			fw = new FileWriter(f, true);//true, continue to write
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(fw);
		//first line: \t minInventory minInventory+1, ..., maxInventory
		pw.print("\t");
		for(int j=0; j<ActionContour[0].length; j++) {
			pw.print(j+instance.minInventory + "\t");
		}pw.println();
		//main boday
		for(int i=ActionContour.length-1; i>=0; i--) {
			pw.print(i+instance.minInventory+"\t");			
			for(int j=0; j<ActionContour[0].length; j++) {
				pw.print(ActionContour[i][j][0]+"|"+ActionContour[i][j][1]+"|"+ActionContour[i][j][2]+"\t");							
			}
			pw.println();
		}
		pw.println();
		pw.flush();
		try {
			fw.flush();
			pw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	/************************************************************************************************************************************/
	public static LTsolution computeLTinstance(LTinstance instance, boolean noInitialTransship, boolean noInitialOrder) {
		int Stages = instance.demandMean1.length;		
		int[][] inventoryPairs = generateInventoryPairs(instance);

		//demand[t][demandPairCount][3] ->{prob*prob, demand 1, demand 2}
		double[][][] demand = generatePMF(instance.demandMean1, instance.demandMean2, instance.tail);	
		//		double[][][] demand = generateDemandMatrix(instance);

		int[][][] optimalAction = new int [Stages][inventoryPairs.length][3];
		double[][] optimalCost = new double [Stages][inventoryPairs.length]; 
		double[][] totalCost = null;

		for(int t=Stages-1;t>=0;t--) { 	

			System.out.println("period "+(t+1));
			long timePeriodStart = System.currentTimeMillis();

			for(int i=0;i<inventoryPairs.length;i++) { 
				//				System.out.println(Arrays.toString(inventoryPairs[i]));
				int[][] actions = null;
				if(t==0 && noInitialTransship == true && noInitialOrder == true) {//work as normal
					actions = new int[][]{{0, 0, 0}};
				}else if(t==0 && noInitialTransship == true && noInitialOrder == false) {//only no transship in the first period
					actions = generateNoTransshipActions(inventoryPairs[i], instance);
				}else if(t==0 && noInitialTransship == false && noInitialOrder == true) {//only no order in the first period
					actions = generateNoOrderActions(inventoryPairs[i], instance);
				}else {
					actions =  generateFeasibleActions(inventoryPairs[i], instance);
				}

				totalCost = new double [inventoryPairs.length][actions.length];

				for(int a=0; a<actions.length;a++) {

					double scenarioProb = 0;
					totalCost[i][a] = computeActionCost(instance, actions[a]);
//					totalCost[i][a] = (t==0) ? 0 : computeActionCost(instance, actions[a]);		//mengyuan's formulation for joint replenishment

					for(int d=0;d<demand[t].length;d++) { // Demand
						if(
								(inventoryPairs[i][0] - actions[a][0] + actions[a][1] - demand[t][d][1] <= instance.maxInventory) 
								&& (inventoryPairs[i][0] - actions[a][0] + actions[a][1] - demand[t][d][1] >= instance.minInventory)
								&& (inventoryPairs[i][1] + actions[a][0] + actions[a][2] - demand[t][d][2] <= instance.maxInventory) 
								&& (inventoryPairs[i][1] + actions[a][0] + actions[a][2] - demand[t][d][2] >= instance.minInventory)
								) {
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
			long timePeriodEnd = System.currentTimeMillis();
			System.out.println("time for period "+(t+1)+" = "+ (timePeriodEnd - timePeriodStart)/1000+"s");
		}
		return new LTsolution(inventoryPairs, optimalAction, optimalCost);
	}



	public static void main(String[] args) {
		int[] demandMean1 = {12, 12, 12, 12};
		int[] demandMean2 = {12, 12, 12, 12};
		int maxInventory  = 80;
		int minInventory  = -50;
		int maxQuantity   = 100;
		double K = 50;				//{K, R, b}: {7, 5, 3}  {5, 7, 3} 
		double z = 0;
		double R = 1000000;
		double v = 0;
		double h = 1;
		double[] b = {30,10}; 
		double tail = 0.0001;
		//		boolean noInitialTransship = false;
		//		boolean noInitialOrder = true;
		boolean[] noInitialTransship = {false};//{false, true, true, false}; both actions, neither, no transship, no order
		boolean[] noInitialOrder 	 = {false};//{false, true, false, true};

		for(int i=0; i<noInitialTransship.length; i++) {
			for(int k=0; k<b.length; k++) {
				LTinstance instance = new LTinstance(demandMean1,demandMean2,maxInventory,minInventory,maxQuantity,K,z,R,v,h,b[k],tail);

				long timeStart = System.currentTimeMillis();
				LTsolution solution = computeLTinstance(instance, noInitialTransship[i], noInitialOrder[i]);
				long timeEnd = System.currentTimeMillis();
				System.out.println("time consumed for SDP = "+(timeEnd - timeStart)/1000 +"s");
				//			printLTsolution(solution);
				writeSolution(solution, "src/main/java/lateralTransshipment/writeResults.txt");		
				System.out.println();

				convertCostMatrix(instance, solution, 0, "src/main/java/lateralTransshipment/convertCostMatrix.txt");
				convertActionMatrix(instance, solution, 0, "src/main/java/lateralTransshipment/convertActionMatrix.txt");
			}

		}



		/*
		Chrono timer = new Chrono();

		LTsimInstance simInstance = new LTsimInstance(demandMean1, demandMean2, maxInventory, minInventory, K, z, R, v, h, b,
				solution.inventoryPairs, solution.optimalAction, solution.optimalCost); 
		int count = 100000;
		boolean print = false;

		lateralTransshipment.LTsimulation.LTsimMultipleRuns(simInstance, count, print);

		System.out.println("-------------------------------------------");
		int initialStateIndex = getStateIndex(solution.inventoryPairs, simInstance.getInitialState());
		System.out.println("Simulation: optimal cost of state"+Arrays.toString(simInstance.getInitialState()) +"= "+solution.optimalCost[0][initialStateIndex]);
		System.out.println("-------------------------------------------");

		simInstance.statCost.setConfidenceIntervalStudent();
		System.out.println(simInstance.statCost.report(0.9, 3));
		System.out.println("Total CPU time: "+timer.format());
		System.out.println(simInstance.statCost.average());
		 */


	}

}
