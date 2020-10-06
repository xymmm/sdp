package lateralTransshipment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LT2Backwards2Stages {

	public static int[][] generateorderingQuantities(int[] state, LTinstance instance){
		ArrayList<int[]> actions = new ArrayList<int[]>();
		for(int i=0; i <= instance.maxInventory - state[0]; i++) {
			for(int j=0; j <= instance.maxInventory - state[1]; j++) {
				if((i<=instance.maxQuantity)&&(j<=instance.maxQuantity)) actions.add(new int[] {i, j});
			}
		}
		int[][] action = new int[actions.size()][2];
		for(int i=0; i<actions.size(); i++) {action[i] = actions.get(i);}
		return action;
	}

	public static int[] generateTransshipment4Quantity(int[] state, LTinstance instance, int[] optimalOrder) {
		List<Integer> transshipment = new ArrayList<>();
		int[] transshipQuantity = null;
		//negative, negative: case 4
		if((state[0]<=0)&&(state[1]<=0)) {
			return new int[] {0};
		}else if((state[0] <=0)&&(state[1] > 0)) {//case 3: transship from 2 to 1, transshipment <= 0
			for(int t=0; t<=state[1]; t++) {
				int[] newState = {state[0] + t, state[1] - t};	//update inventory level after transshipment
				if(newState[0]+optimalOrder[0]<=instance.maxInventory 
						&& newState[0]+optimalOrder[0] >=instance.minInventory
						&& newState[1] + optimalOrder[1] <= instance.maxInventory
						&& newState[1] + optimalOrder[1] >= instance.minInventory) {//transship does not hit boundary
					transshipment.add(-t);
				}
			}
			transshipQuantity = new int[transshipment.size()];
			for(int i=0; i<transshipment.size(); i++) {transshipQuantity[i] = transshipment.get(i);}
			return transshipQuantity;
		}else if((state[0] > 0)&&(state[1] <= 0)) {//case 2: transship from 1 to 2, transship >= 0
			for(int t=0; t<=state[0]; t++) {
				int[] newState = {state[0] - t, state[1] + t};	//update inventory level after transshipment
				if(newState[0]+optimalOrder[0]<=instance.maxInventory 
						&& newState[0]+optimalOrder[0] >=instance.minInventory
						&& newState[1] + optimalOrder[1] <= instance.maxInventory
						&& newState[1] + optimalOrder[1] >= instance.minInventory) {//transship does not hit boundary
					transshipment.add(t);
				}
			}
			transshipQuantity = new int[transshipment.size()];
			for(int i=0; i<transshipment.size(); i++) {transshipQuantity[i] = transshipment.get(i);}
			return transshipQuantity;
		}else {
			for(int t=-state[1]; t<= state[0]; t++) {
				if(t<=0) {//transship from 1 to 2
					int[] newState = new int[] { state[0]-t, state[1]+t};
					if(newState[0]+optimalOrder[0]<=instance.maxInventory 
							&& newState[0]+optimalOrder[0] >=instance.minInventory
							&& newState[1] + optimalOrder[1] <= instance.maxInventory
							&& newState[1] + optimalOrder[1] >= instance.minInventory) {
						transshipment.add(t);
					}
				}else {//transship from 2 to 1
					int[] newState = new int[] { state[0]-t, state[1]+t};
					if(newState[0]+optimalOrder[0]<=instance.maxInventory 
							&& newState[0]+optimalOrder[0] >=instance.minInventory
							&& newState[1] + optimalOrder[1] <= instance.maxInventory
							&& newState[1] + optimalOrder[1] >= instance.minInventory) {
						transshipment.add(t);
					}
				}
			}
			transshipQuantity = new int[transshipment.size()];
			for(int i=0; i<transshipment.size(); i++) {transshipQuantity[i] = transshipment.get(i);}
			return transshipQuantity;
		}
	}

	public static LTsolution computeLTinstance2stages(LTinstance instance, boolean noInitialTransship, boolean noInitialOrder) {
		int Stages = instance.demandMean1.length;		
		int[][] inventoryPairs = LT2locationsBackwards.generateInventoryPairs(instance);

		//demand[t][demandPairCount][3] ->{prob*prob, demand 1, demand 2}
		double[][][] demand = LT2locationsBackwards.generatePMF(instance.demandMean1, instance.demandMean2, instance.tail);	

		int[][][] optimalAction = new int [Stages][inventoryPairs.length][3];

		double[][] optimalCost = new double [2*Stages][inventoryPairs.length]; 
		double[][] totalCost = null;

		int[][][] optimalOrder = new int[Stages][inventoryPairs.length][2];
		int[][] optimalTransship = new int[Stages][inventoryPairs.length];


		for(int t=Stages*2-1;t>=0;t--) { 	

			System.out.println("period "+(t+1));
			long timePeriodStart = System.currentTimeMillis();

			if(t%2 == 1) {//stage 2 for period t: reorder
				int actualIndex = ((t+1)/2)-1;
				for(int i=0; i<inventoryPairs.length; i++) {
//					System.out.println(Arrays.toString(inventoryPairs[i]));
					int[][] order = generateorderingQuantities(inventoryPairs[i], instance);
					totalCost = new double[inventoryPairs.length][order.length];
					for(int a=0; a<order.length; a++) {
						double scenarioProb = 0;
						totalCost[i][a] = ((order[a][0] == 0) ? 0 : (instance.K+order[a][0]*instance.z))
								+ ((order[a][1] == 0) ? 0 : (instance.K+order[a][1]*instance.z));
						for(int d=0;d<demand[actualIndex].length;d++) { // Demand
							if(
									(inventoryPairs[i][0] + order[a][0] - demand[actualIndex][d][1] <= instance.maxInventory) 
									&& (inventoryPairs[i][0] + order[a][0] - demand[actualIndex][d][1] >= instance.minInventory)
									&& (inventoryPairs[i][1] + order[a][1] - demand[actualIndex][d][2] <= instance.maxInventory) 
									&& (inventoryPairs[i][1] + order[a][1] - demand[actualIndex][d][2] >= instance.minInventory)
									) {
								int[] closingState = {(int) (inventoryPairs[i][0] + order[a][0] - demand[actualIndex][d][1]),
													  (int) (inventoryPairs[i][1] + order[a][1] - demand[actualIndex][d][2])};
								int closingStateIndex = LT2locationsBackwards.getStateIndex(inventoryPairs, closingState);
								totalCost[i][a] += demand[actualIndex][d][0]*(
										LT2locationsBackwards.computeImmediateCost(instance, closingState)
										+((t==Stages*2-1) ? 0 : optimalCost[t+1][closingStateIndex])
										);
								scenarioProb += demand[actualIndex][d][0];
							}//else, we do nothing - not added in scenarios	
												
						}
						totalCost[i][a] = totalCost[i][a]/scenarioProb;		
					}//searched for all feasible order quantities for 2 locations
					sdp.util.writeText.writeDoubleArray(totalCost[i], "src/main/java/lateralTransshipment/totalCost.txt");
					optimalCost[t][i] = sdp.util.globleMinimum.getGlobalMinimum(totalCost[i]);
					int optimalOrderIdx = sdp.util.globalMinimumIndex.getGlobalMinimumJavaIndex(totalCost[i]);
					optimalOrder[actualIndex][i] = order[optimalOrderIdx];
				}				
			}else {//stage 1 for period t: transship
				int actualIndex = (t/2);
				for(int i=0;i<inventoryPairs.length;i++) {
//					System.out.println(Arrays.toString(inventoryPairs[i]));

					int[] transshipment = generateTransshipment4Quantity(inventoryPairs[i], instance, optimalOrder[actualIndex][i]);
					totalCost = new double[inventoryPairs.length][transshipment.length];
//					System.out.println(Arrays.toString(transshipment));
					for(int k=0; k<transshipment.length; k++) {
						int[] inventoryAfterTransship = {inventoryPairs[i][0] - transshipment[k], inventoryPairs[i][1] + transshipment[k]};
						int inventoryAfterTrnsshipIdx = LT2locationsBackwards.getStateIndex(inventoryPairs, inventoryAfterTransship);
						totalCost[i][k] = (Math.abs(transshipment[k]) == 0? 0 : instance.R + Math.abs(transshipment[k])*instance.v)
								+optimalCost[t+1][inventoryAfterTrnsshipIdx];
					}
					optimalCost[t][i] = sdp.util.globleMinimum.getGlobalMinimum(totalCost[i]);
					int optimalTransshipmentIdx = sdp.util.globalMinimumIndex.getGlobalMinimumJavaIndex(totalCost[i]);
					optimalTransship[actualIndex][i] = transshipment[optimalTransshipmentIdx];
				}
			}

			long timePeriodEnd = System.currentTimeMillis();
			System.out.println("time for period "+(t+1)+" = "+ (timePeriodEnd - timePeriodStart)/1000+"s");
		}//t
		double[][] optimalCostResults = new double[Stages][inventoryPairs.length];
		for(int t=0; t<Stages; t++) {
			for(int i=0; i<inventoryPairs.length; i++) {
				optimalAction[t][i] = new int[] {optimalTransship[t][i], optimalOrder[t][i][0], optimalOrder[t][i][1]};
				optimalCostResults[t][i] = optimalCost[2*t][i];
			}
		}
		return new LTsolution(inventoryPairs, optimalAction, optimalCostResults);
	}

	public static void main(String[] args) {
		int[] demandMean1 = {2,3};
		int[] demandMean2 = {2,3};
		int maxInventory  = 10;
		int minInventory  = -10;
		int maxQuantity   = 20;
		double K = 20;				//{K, R, b}: {7, 5, 3}  {5, 7, 3} 
		double z = 0;
		double R = 1000000000;
		double v = 0;
		double h = 1;
		double[] b = {5}; 
		double tail = 0.0001;
		//		boolean noInitialTransship = false;
		//		boolean noInitialOrder = true;
		boolean[] noInitialTransship = {false};//{false, true, true, false}; both actions, neither, no transship, no order
		boolean[] noInitialOrder 	 = {false};//{false, true, false, true};

		for(int i=0; i<noInitialTransship.length; i++) {
			for(int k=0; k<b.length; k++) {
				LTinstance instance = new LTinstance(demandMean1,demandMean2,maxInventory,minInventory,maxQuantity,K,z,R,v,h,b[k],tail);

				long timeStart = System.currentTimeMillis();
				LTsolution solution = computeLTinstance2stages(instance, noInitialTransship[i], noInitialOrder[i]);
				long timeEnd = System.currentTimeMillis();
				System.out.println("time consumed for SDP = "+(timeEnd - timeStart)/1000 +"s");
				//			printLTsolution(solution);
				LT2locationsBackwards.writeSolution(solution, "src/main/java/lateralTransshipment/writeResults.txt");		
				System.out.println();

				LT2locationsBackwards.convertCostMatrix(instance, solution, 0, "src/main/java/lateralTransshipment/convertCostMatrix.txt");
				LT2locationsBackwards.convertActionMatrix(instance, solution, 0, "src/main/java/lateralTransshipment/convertActionMatrix.txt");
				
/*				for(int l=0; l<solution.inventoryPairs.length; l++) {
					System.out.print(Arrays.toString(solution.inventoryPairs[l])+"\t");
					for(int t=0; t<instance.demandMean1.length; t++) {
						System.out.print(solution.optimalCost[t][l]+"\t");
					}
					System.out.println();
				}
				*/
			}
		}
	}

}
