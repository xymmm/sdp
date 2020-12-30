package lateralTransshipment_singleActionTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Scanner;

import lateralTransshipment.LT2locationsBackwards;
import lateralTransshipment.LTinstance;

public class LT2locations_singleAction_byRead {

	public static double[][] readFromTXT(double[][] array) throws Exception {

		Scanner sc = new Scanner( new BufferedReader(new FileReader("src/main/java/lateralTransshipment_singleActionTest/optimalCostToRead2_4.txt")));
//		Scanner sc = new Scanner( new BufferedReader(new FileReader("src/main/java/lateralTransshipment_singleActionTest/K20z1R0v1-1qdrt-overall-2_4toRead.txt")));

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
	

	public static testSolution LT2locations_singleAction_byRead(LTinstance instance, int actionPeriodIdx, int[] testTransshipment, int[][] testOrder, int[] testInventory) {
		int Stages = instance.demandMean1.length;		
		int[][] inventoryPairs = LT2locationsBackwards.generateInventoryPairs(instance);

		//demand[t][demandPairCount][3] ->{prob*prob, demand 1, demand 2}
		double[][][] demand = LT2locationsBackwards.generatePMF(instance.demandMean1, instance.demandMean2, instance.tail);	

		double[][] optimalCost = new double [Stages][inventoryPairs.length]; 
		double totalCost = 0;

		//read future cost: only care about the first-leading period
		double[][] futureCost = new double[inventoryPairs.length][Stages-actionPeriodIdx-1];
		try {futureCost = readFromTXT(futureCost);
		} catch (Exception e) {e.printStackTrace();}

		for(int i=0; i<inventoryPairs.length; i++) {optimalCost[actionPeriodIdx+1][i] = futureCost[i][0];}

		double penaltyCost1 = 0; double penaltyCost2 = 0; double holdingCost1 = 0; double holdingCost2 = 0;	

//		int testInventoryIdx = LT2locationsBackwards.getStateIndex(inventoryPairs, testInventory);

		for(int t=actionPeriodIdx;t>=0;t--) { 	
			int[] actions = new int[]{testTransshipment[0], testOrder[0][0], testOrder[0][1]};				
			double scenarioProb = 0;
			double sh1 = 0; double sh2 = 0; double sp1 = 0; double sp2 = 0;
			totalCost = lateralTransshipment.LT2locationsBackwards.computeActionCost(instance, actions);
			for(int d=0;d<demand[t].length;d++) { // Demand
				if(
						(testInventory[0] - actions[0] + actions[1] - demand[t][d][1] <= instance.maxInventory) 
						&& (testInventory[0] - actions[0] + actions[1] - demand[t][d][1] >= instance.minInventory)
						&& (testInventory[1] + actions[0] + actions[2] - demand[t][d][2] <= instance.maxInventory) 
						&& (testInventory[1] + actions[0] + actions[2] - demand[t][d][2] >= instance.minInventory)
						) {
					int[] closingState = {(int) (testInventory[0] - actions[0] + actions[1] - demand[t][d][1]),
							(int) (testInventory[1] + actions[0] + actions[2] - demand[t][d][2])};
					int closingStateIndex = lateralTransshipment.LT2locationsBackwards.getStateIndex(inventoryPairs, closingState);
					totalCost += demand[t][d][0]*(
							lateralTransshipment.LT2locationsBackwards.computeImmediateCost(instance, closingState)
							+((t==Stages-1) ? 0 : optimalCost[t+1][closingStateIndex])
							);
					scenarioProb += demand[t][d][0];
					if(closingState[0]>=0) {
						sh1+=demand[t][d][0];
						holdingCost1+=demand[t][d][0]*(instance.h*closingState[0]);
					}else {
						sp1+=demand[t][d][0];
						penaltyCost1+=demand[t][d][0]*(-instance.b*closingState[0]);
					}
					if(closingState[1]>=0) {
						sh2+=demand[t][d][0];
						holdingCost2+=demand[t][d][0]*(instance.h*closingState[1]);
					}else {
						sp2+=demand[t][d][0];
						penaltyCost2+=demand[t][d][0]*(-instance.b*closingState[1]);
					}

				}//else, we do nothing - not added in scenarios
			}
			totalCost = totalCost/scenarioProb;
			holdingCost1 /= sh1; holdingCost2 /= sh2; penaltyCost1 /= sp1; penaltyCost2 /= sp2;

		}

		System.out.println("h1 = "+holdingCost1);
		System.out.println("h2 = "+holdingCost2);
		System.out.println("b1 = "+penaltyCost1);
		System.out.println("b2 = "+penaltyCost2);
		System.out.println("sum of holding = "+(holdingCost1+holdingCost2));
		System.out.println("sum of penalty = "+(penaltyCost1+penaltyCost2));
//		System.out.println("computation: "+(holdingCost1+holdingCost2+penaltyCost1+penaltyCost2)+" + "+optimalCost[actionPeriodIdx+1][testInventoryIdx]+" = "+totalCost);
		return new testSolution(holdingCost1, holdingCost2, penaltyCost1, penaltyCost2, totalCost);
	}


	public static void main(String[] args) throws Exception {
		int[] demandMean1 = {4, 6, 8, 6};
		int[] demandMean2 = {4, 6, 8, 6};
		int maxInventory  = 30;
		int minInventory  = -30;
		int maxQuantity   = 70;
		double K = 20;				//{K, R, b}: {7, 5, 3}  {5, 7, 3} 
		double z = 1;//0
		double[] R = {0};//{0, 1, 3, 5, 8, 10, 12, 14, 16, 18, 20, 25, 30, 35, 40, 50, 1000000};
		double v = 1;//0
		double h = 1;
		double b = 5; 
		double tail = 0.0001;

		int actionPeriodIdx = 0;
		int[] testTransshipment = {1};
		int[][] testOrder = {{0,28}};
		int[] testInventory = {1,0};
 
		for(int k=0; k<R.length; k++) {
					System.out.println("test inventory: "+Arrays.toString(testInventory));
					System.out.println("test action: ["+testTransshipment[0]+", "+testOrder[0][0]+", "+testOrder[0][1]+"]");
					LTinstance instance = new LTinstance(demandMean1,demandMean2,maxInventory,minInventory,maxQuantity,K,z,R[k],v,h,b,tail);
//					int[] inventory = {i,j};
//					testOrder = new int[][]{{action[i][1], action[i][2]}};
//					testTransshipment = new int[] {action[i][0]};
					testSolution solution = LT2locations_singleAction_byRead(instance, actionPeriodIdx, testTransshipment, testOrder, testInventory);
					System.out.println("optimal cost of "+Arrays.toString(testInventory)+" = "+solution.total);
//					double[] solutionArray = {solution.hc1, solution.hc2, solution.pc1, solution.pc2, solution.total};
//					sdp.util.writeText.writeDoubleArray(solutionArray, "src/main/java/lateralTransshipment_singleActionTest/writeSeperate.txt");

		}
	}


}
