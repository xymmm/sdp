package instanceRuns;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import sQ.sdp.sQ;
import sQ.sdp.sQgivenQ;
import sQ.sdp.sQgivenQsolution;
import sQ.sdp.sQsolution;
import sQt.generatedQ.sQtgenerated;
import sQt.generatedQ.sQtgeneratedQ_s;
import sQt.generatedQ.sQtgeneratedQ_s_solution;
import sQt.generatedQ.sQtgeneratedSolution;
import sS.sdp.sS;
import sS.sdp.sSsolution;
import sdp.data.Instance;
import sdp.data.InstanceDouble;

public class multipleInstances_sQt {

	public static double[][] period25costs(Instance instance, boolean initialOrder, InstanceDouble instanceDouble) {
		/**run sS, sQ-Q costs for 25-period instances. results = [[cost_sS, time_sS, cost_sQ-Q, Q_sQ, time_sQ, cost_sQt, time_sQt],[Qt_sQt]]**/
		double[][] results = new double[2][Math.max(7, instance.getStages())];
		//sS
		sSsolution sSsolution = sS.solveInstance(instance, initialOrder);
		results[0][0] = sSsolution.optimalCost[instance.initialInventory - instance.minInventory][0]; 
		results[0][1] = sSsolution.timeConsumed;
		//sQ
		sQsolution sQsolution = sQ.solvesQInstance(instance,initialOrder);
		results[0][2] = sQsolution.totalCost[instance.initialInventory - instance.minInventory][sQsolution.getOpt_aSQ(instance)+1][0];
		results[0][3] = sQsolution.getOpt_aSQ(instance)+1;
		results[0][4] = sQsolution.timeConsumedsQ;
		//sQt
		sQtgeneratedSolution sQtsolution = sQtgenerated.sQtgeneratedSingle(instanceDouble);
		results[0][5] = sQtsolution.minCost[instance.initialInventory - instance.minInventory];
		results[0][6] = sQtsolution.timeConsumed_sQt;
		results[1] = sQtsolution.optQ[instance.initialInventory - instance.minInventory];
		return results;
	}
	
	public static int[] solveReorderPointsQ(Instance instance, double[][] results, boolean initialOrder){
		/** run sQ-Q for s**/
		int[] s = new int[instance.getStages()];
		sQgivenQsolution sQs = sQgivenQ.costVaryingWithInventory((int) results[0][3],instance,initialOrder);
		s = sQs.getsGivenQ(instance, sQs);
		return s;
	}
	
	public static int[] solveReorderPointsQt(Instance instance, double[][] results, boolean initialOrder) {
		int[] s = new int[instance.getStages()];
		int d = 1;
		int Q[] = new int[instance.getStages()];
		for(int t=0; t<instance.getStages(); t++) {
			Q[t] = (int) (results[1][t]/d);
		}
		sQtgeneratedQ_s_solution sQts = sQtgeneratedQ_s.costVaryingWithInventory(Q, instance, initialOrder);
		s = sQts.getsGivenQ(instance, sQts);
		return s;
	}
	
	public static void writeToTextParameters(int fixedOrderingCost, int penaltyCost, int unitCost, double stdParameter){
		FileWriter fw = null;
		try {
			File f = new File("E:\\sQtpoissonInstances.txt");
			fw = new FileWriter(f, true);//true, continue to write
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(fw);
		pw.println(fixedOrderingCost);
		pw.println(penaltyCost);
		pw.println(unitCost);
		pw.println(stdParameter);
		pw.flush();
		try {
			fw.flush();
			pw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeToTextCostResults(Instance instance, double[][] results, boolean initialOrder) {//int s
		FileWriter fw = null;
		try {
			File f = new File("E:\\sQtpoissonInstances.txt");
			fw = new FileWriter(f, true);//true, continue to write
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(fw);
		pw.println(Arrays.toString(instance.demandMean));
		pw.println(initialOrder + " ");
		for(int i=0; i<7; i++) {
			pw.print(results[0][i]+"     ");
		}
		pw.println(Arrays.toString(results[1]));
		pw.flush();
		try {
			fw.flush();
			pw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	public static void writeToTextReorderPoints(Instance instance, boolean initialOrder, int[][] s) {//int s
		FileWriter fw = null;
		try {
			File f = new File("E:\\sQtpoissionInstances_reorderPoints.txt");
			fw = new FileWriter(f, true);//true, continue to write
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(fw);
		pw.println(Arrays.toString(instance.demandMean));
		pw.println(Arrays.deepToString(s));
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
	
	public static void main(String[] args) {
		/**problem parameters**/
		int holdingCost = 1;
		int[] penaltyCost = {5,10,5,3};
		int[] unitCost = {0,1};
		int[] fixedCost = {10,5,3,5};
		double tail = 0.00000001;
		int minInventory = -50;
		int maxInventory = 50;
		int maxQuantity = 9;
		
		/**demand distributions**/
		double stdParameter = 0.1;

		int demandMeans[][] = {
				{2,4,6,4},
				{9,7,5,3},
				{2,5,7,8},
				{6,6,6,6},
				{5,7,2,5},
				{4,5,6,7,6},
				{8,6,3,7,9},
				{6,3,9,1,5},
				{2,5,4,8,3},
				{4,4,8,8,3}
		};

		
		// with multiple parameters
		System.out.println("total number = "+ unitCost.length * fixedCost.length *  demandMeans.length);
		int count = 1;
		
		for(int p=0; p<penaltyCost.length; p++) {
			for(int u=0;u<unitCost.length;u++) {
				//for(int f=0;f<fixedCost.length;f++) {
											
						writeToTextParameters(fixedCost[p], penaltyCost[p], unitCost[u], stdParameter);
						
						for(int d = 0; d<demandMeans.length; d++) {
							System.out.println("instance "+(d+1));
							//Instance instance = new Instance(fixedCost[f],unitCost[u],holdingCost,penaltyCost[p],demandMeans[d],
									//tail,minInventory,maxInventory,maxQuantity,stdParameter[std]);
							Instance instance = new Instance(fixedCost[p],unitCost[u],holdingCost,penaltyCost[p],demandMeans[d],
									tail,minInventory,maxInventory,maxQuantity,stdParameter);
							InstanceDouble instanceDouble = new InstanceDouble(fixedCost[p], unitCost[u], holdingCost, penaltyCost[p],demandMeans[d],
									tail,minInventory,maxInventory,maxQuantity,stdParameter);
							System.out.println(Arrays.toString(demandMeans[d]));
							
								double[][] cost = period25costs(instance, true, instanceDouble);
								writeToTextCostResults(instance, cost, true);
								System.out.println("cost+time done");
								
								int[][] reorderPoints = new int [2][instance.getStages()];
								reorderPoints[0] = solveReorderPointsQ(instance, cost, true);
								reorderPoints[1] = solveReorderPointsQt(instance, cost, true);
								writeToTextReorderPoints(instance, true, reorderPoints);
								System.out.println("s done");
								
								/*
								cost = period25costs(instance, false);
								//s = period25reorderPoints(instance, cost, false);
								writeToTextResults(instance, cost, false);
								System.out.println("False done");
								*/								
								//status
								System.out.println(count);
								count++;
							
							
						//}
					}
				}
			}
		

	}
	
}
