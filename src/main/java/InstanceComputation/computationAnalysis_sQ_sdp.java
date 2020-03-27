package InstanceComputation;

import java.util.Arrays;

import reorderQuantitySystem.sQsystemSolution;
import sQ.sdp.sQsolution;
import sS.sSsolution;
import sdp.data.InstanceDouble;

public class computationAnalysis_sQ_sdp {

	public static void main(String[] args) {

		double holdingCost = 1;

		double[] fixedOrderingCost = {500, 1000, 1500};
		double[] unitCost		   = {0,1};
		double[] penaltyCost	   = {5, 10, 20};
		double[] stdParameter	   = {0.1, 0.2, 0.3};

		double initialStock = 0;

		double tail = 0.00000001;

		int minInventory = -1500;
		int maxInventory = 1500;
		int maxQuantity = 800;

		boolean Normal = true;

		double[][] demandMean = {
				{100, 100, 100, 100},//4
				{100, 100, 100, 100, 100, 100},
				{100, 100, 100, 100, 100, 100, 100, 100},
				{100, 100, 100, 100, 100, 100, 100, 100, 100, 100},
				{100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100},//12
				{100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100},
				{100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100},
				{100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100},
				{100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100},//20
				{100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100},
				{100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100}				
		};

		double[][][][][][] Results = new double[stdParameter.length][fixedOrderingCost.length][penaltyCost.length][unitCost.length][demandMean.length][3];

		for(int d=0; d<demandMean.length; d++) {
			System.out.println("=====================================================");
			sdp.util.writeText.writeNewLine("src/main/java/InstanceComputation/lengthTest.txt");

			for(int s = 0; s<stdParameter.length; s++) {//3
				System.out.println("stdParameter = "+stdParameter[s]);
				for(int f=0; f<fixedOrderingCost.length; f++) {//3
					System.out.println("fixed cost = "+fixedOrderingCost[f]);
					//for(int p=0; p<penaltyCost.length; p++) {//3
						System.out.println("penalty cost = "+penaltyCost[f]);
						for(int u=0; u<unitCost.length; u++) {//2
							System.out.println("unit cost = "+unitCost[u]);

							InstanceDouble instance = new InstanceDouble(
									fixedOrderingCost[f], unitCost[u], holdingCost, penaltyCost[f],
									demandMean[d], tail, minInventory, maxInventory, maxQuantity, stdParameter[s]);

							sSsolution sSsolution = sS.sS.solveInstance(instance, true, Normal);
							Results[s][f][f][u][d][0] = sSsolution.optimalCost[(int) (instance.initialInventory - instance.minInventory)][0];

							sQsystemSolution sQsolution = sQ.sdp.sQ.solvesQInstance(instance, Normal);
							Results[s][f][f][u][d][1] = sQsolution.optimalCost;
							
							Results[s][f][f][u][d][2] = 100*(Results[s][f][f][u][d][1] - Results[s][f][f][u][d][0])/Results[s][f][f][u][d][0];
							
							sdp.util.writeText.writeDoubleArray(Results[s][f][f][u][d],
									"src/main/java/InstanceComputation/lengthTest.txt");
							System.out.println(Arrays.toString(Results[s][f][f][u][d]));
							//int[] reorderPoint = reorderQuantitySystem.reorderPoint.computeReorderPoint(instance, sQsolution);
							//System.out.println("optimal cost = "+sQsolution.optimalCost);
							//System.out.println("optimal schedule = " + Arrays.toString(sQsolution.optimalSchedule));
							//System.out.println("reorder points = " + Arrays.toString(reorderPoint));
						}
					}
				//}
			}
		}
	}


}
