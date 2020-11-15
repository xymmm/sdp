package lateralTransshipment_singleActionTest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Scanner;

import lateralTransshipment.LT2Backwards2Stages;
import lateralTransshipment.LT2locationsBackwards;
import lateralTransshipment.LTinstance;
import lateralTransshipment.LTsolution;

public class LT2locations_singleAction_byRead {
	
	public static double[][] readFromTXT(double[][] array) throws Exception {

		Scanner sc = new Scanner( new BufferedReader(new FileReader("src/main/java/lateralTransshipment_singleActionTest/optimalCostToRead2_4.txt")));

		while(sc.hasNextLine()) {
			for (int i=0; i<array.length; i++) {
				String[] line = sc.nextLine().trim().split("\t");
				for (int j=0; j<line.length; j++) {
					array[i][j] = Double.parseDouble(line[j]);
				}
			}
		}
//		System.out.println(Arrays.deepToString(array));
		return array;
	}
	
	public static LTsolution LT2locations_singleAction_byRead(LTinstance instance, int actionPeriodIdx, int[] testTransshipment, int[][] testOrder) {
		int Stages = instance.demandMean1.length;		
		int[][] inventoryPairs = LT2locationsBackwards.generateInventoryPairs(instance);

		//demand[t][demandPairCount][3] ->{prob*prob, demand 1, demand 2}
		double[][][] demand = LT2locationsBackwards.generatePMF(instance.demandMean1, instance.demandMean2, instance.tail);	

		int[][][] optimalAction = new int [Stages][inventoryPairs.length][3];

		double[][] optimalCost = new double [2*Stages][inventoryPairs.length]; 
		double[][] totalCost = null;

		int[][][] optimalOrder = new int[Stages][inventoryPairs.length][2];
		int[][] optimalTransship = new int[Stages][inventoryPairs.length];

		//read future cost: only care about the first-leading period
		double[][] futureCost = new double[inventoryPairs.length][Stages-actionPeriodIdx-1];
		try {
			futureCost = readFromTXT(futureCost);
		} catch (Exception e) {
			e.printStackTrace();
		}
		for(int i=0; i<inventoryPairs.length; i++) {
			optimalCost[actionPeriodIdx*2+2][i] = futureCost[i][0];
		}


		for(int t=actionPeriodIdx*2+1;t>=0;t--) { 	

			System.out.println("period "+(t+1));
			long timePeriodStart = System.currentTimeMillis();

			if(t%2 == 1) {//stage 2 for period t: reorder
				int actualIndex = ((t+1)/2)-1;
				for(int i=0; i<inventoryPairs.length; i++) {
					//testOrder
					totalCost = new double[inventoryPairs.length][testOrder.length];
					for(int a=0; a<testOrder.length; a++) {
						double scenarioProb = 0;
						totalCost[i][a] = ((testOrder[a][0] == 0) ? 0 : (instance.K+testOrder[a][0]*instance.z))
								+ ((testOrder[a][1] == 0) ? 0 : (instance.K+testOrder[a][1]*instance.z));
						for(int d=0;d<demand[actualIndex].length;d++) { // Demand
							if(
									(inventoryPairs[i][0] + testOrder[a][0] - demand[actualIndex][d][1] <= instance.maxInventory) 
									&& (inventoryPairs[i][0] + testOrder[a][0] - demand[actualIndex][d][1] >= instance.minInventory)
									&& (inventoryPairs[i][1] + testOrder[a][1] - demand[actualIndex][d][2] <= instance.maxInventory) 
									&& (inventoryPairs[i][1] + testOrder[a][1] - demand[actualIndex][d][2] >= instance.minInventory)
									) {
								int[] closingState = {(int) (inventoryPairs[i][0] + testOrder[a][0] - demand[actualIndex][d][1]),
										(int) (inventoryPairs[i][1] + testOrder[a][1] - demand[actualIndex][d][2])};
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
					optimalCost[t][i] = sdp.util.globleMinimum.getGlobalMinimum(totalCost[i]);
				}				
			}else {//stage 1 for period t: transship
//				int actualIndex = (t/2);
				for(int i=0;i<inventoryPairs.length;i++) {

					totalCost = new double[inventoryPairs.length][testTransshipment.length];
					//					System.out.println(Arrays.toString(transshipment));
					for(int k=0; k<testTransshipment.length; k++) {
						int[] inventoryAfterTransship = {inventoryPairs[i][0] - testTransshipment[k], inventoryPairs[i][1] + testTransshipment[k]};
						int inventoryAfterTrnsshipIdx = LT2locationsBackwards.getStateIndex(inventoryPairs, inventoryAfterTransship);
						totalCost[i][k] = (Math.abs(testTransshipment[k]) == 0? 0 : instance.R + Math.abs(testTransshipment[k])*instance.v)
								+optimalCost[t+1][inventoryAfterTrnsshipIdx];
					}
					optimalCost[t][i] = sdp.util.globleMinimum.getGlobalMinimum(totalCost[i]);

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
		int[] demandMean1 = {4, 6, 8, 6};
		int[] demandMean2 = {4, 6, 8, 6};
		int maxInventory  = 30;
		int minInventory  = -30;
		int maxQuantity   = 70;
		double K = 20;				//{K, R, b}: {7, 5, 3}  {5, 7, 3} 
		double z = 0;
		double[] R = {0};//{0, 1, 3, 5, 8, 10, 12, 14, 16, 18, 20, 25, 30, 35, 40, 50, 1000000};
		double v = 0;
		double h = 1;
		double b = 5; 
		double tail = 0.0001;
		
		int actionPeriodIdx = 0;
		int[] testTransshipment = {1};
		int[][] testOrder = {{0,0}};
		int[] testInventory = {30,29};

			for(int k=0; k<R.length; k++) {
				LTinstance instance = new LTinstance(demandMean1,demandMean2,maxInventory,minInventory,maxQuantity,K,z,R[k],v,h,b,tail);

				long timeStart = System.currentTimeMillis();
				LTsolution solution = LT2locations_singleAction_byRead(instance, actionPeriodIdx, testTransshipment, testOrder);
				long timeEnd = System.currentTimeMillis();
				System.out.println("time consumed for SDP = "+(timeEnd - timeStart)/1000 +"s");
				int testInventoryIdx = LT2locationsBackwards.getStateIndex(solution.inventoryPairs, testInventory);
				System.out.println("optimal cost of "+Arrays.toString(solution.inventoryPairs[testInventoryIdx])+" = "+solution.optimalCost[0][testInventoryIdx]);

//				LT2locationsBackwards.convertCostMatrix(instance, solution, 0, "src/main/java/lateralTransshipment_singleActionTest/convertCostMatrix.txt");

		}
	}


}
