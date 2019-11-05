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
import sS.sdp.sS;
import sS.sdp.sSsolution;
import sdp.data.Instance;

public class multipleInstances_sQ{
	
	public static double[] period25costs(Instance instance, boolean initialOrder) {
		/**run sS, sQ-Q costs for 25-period instances. results = [cost_sS, cost_sQ-Q, Q]**/
		double[] results = new double[5];
		//sS
		sSsolution sSsolution = sS.solveInstance(instance, initialOrder);
		results[0] = sSsolution.optimalCost[instance.initialInventory - instance.minInventory][0]; 
		results[1] = sSsolution.timeConsumed;
		//sQ
		sQsolution sQsolution = sQ.solvesQInstance(instance,initialOrder);
		results[2] = sQsolution.totalCost[instance.initialInventory - instance.minInventory][sQsolution.getOpt_aSQ(instance)+1][0];
		results[3] = sQsolution.getOpt_aSQ(instance)+1;
		results[4] = sQsolution.timeConsumedsQ;
		return results;
	}
	
	public static int[] period25reorderPoints(Instance instance, double[] results, boolean initialOrder){
		/** run sQ-Q for s**/
		int[] s = new int[instance.getStages()];
		sQgivenQsolution sQs = sQgivenQ.costVaryingWithInventory((int) results[3],instance,initialOrder);
		s = sQs.getsGivenQ(instance, sQs);
		return s;
	}
	
	public static void writeToTextParameters(int fixedOrderingCost, int penaltyCost, int unitCost, double stdParameter){
		FileWriter fw = null;
		try {
			File f = new File("E:\\sQpoissonInstances.txt");
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
	
	public static void writeToTextCostResults(Instance instance, double[] results, boolean initialOrder) {//int s
		FileWriter fw = null;
		try {
			File f = new File("E:\\sQpoissonInstances.txt");
			fw = new FileWriter(f, true);//true, continue to write
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(fw);
		pw.println(Arrays.toString(instance.demandMean));
		pw.println(initialOrder + "     "+results[0]+"     "+ results[1] +"     "+results[2]+"     "+ results[3] +"     "+results[4]);
		pw.flush();
		try {
			fw.flush();
			pw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	public static void writeToTextReorderPoints(Instance instance, boolean initialOrder, int[] s) {//int s
		FileWriter fw = null;
		try {
			File f = new File("E:\\sQpoissonInstaces_reorderPoints.txt");
			fw = new FileWriter(f, true);//true, continue to write
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(fw);
		pw.println(Arrays.toString(instance.demandMean));
		for(int t=0; t<instance.getStages();t++) {
			pw.print(s[t]+"   ");
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
	
	public static void main(String[] args) {
		/**problem parameters**/
		int holdingCost = 1;
		int penaltyCost = 10;
		int unitCost = 0;
		int fixedCost = 100;
		double tail = 0.00000001;
		int minInventory = -1000;
		int maxInventory = 1000;
		int maxQuantity = 1000;
		
		/**demand distributions**/
		double stdParameter = 0.1;
		int demandMeans[][] = {
				{11,38,115,175,115,38},
				{122,77,124,95,107,73},
				{100,62,31,43,199,172},
				{100,100,100,100,100,100,100,100},
				{211,119,165,47,100,62,31,82},
				{97,33,41,76,122,29,37,66},
				{54,23,35,49,66,26,37,41,71,19},
				{65,73,59,42,36,31,28,23,18,11},
				{11,18,23,28,31,36,42,59,73,65},
				{9,17,22,35,49,52,68,53,50,36,23,18},
				{76,58,22,54,69,21,13,89,75,66,45,29},
				{60,60,60,60,60,60,60,60,60,60,60,60}
		};
		
		//System.out.println("total number = "+penaltyCost.length * unitCost.length * fixedCost.length * stdParameter.length * demandMeans.length);
		//int count = 1;
		
		//for(int p=0; p<penaltyCost.length; p++) {
			//for(int u=0;u<unitCost.length;u++) {
				//for(int f=0;f<fixedCost.length;f++) {
					//for(int std = 0; std<stdParameter.length;std++) {
						
						//writeToTextParameters(fixedCost[f], penaltyCost[p], unitCost[u], stdParameter[std]);
						
						for(int d = 0; d<demandMeans.length; d++) {
							System.out.println("instance "+(d+1));
							//Instance instance = new Instance(fixedCost[f],unitCost[u],holdingCost,penaltyCost[p],demandMeans[d],
									//tail,minInventory,maxInventory,maxQuantity,stdParameter[std]);
							Instance instance = new Instance(fixedCost,unitCost,holdingCost,penaltyCost,demandMeans[d],
									tail,minInventory,maxInventory,maxQuantity,stdParameter);
							System.out.println(Arrays.toString(demandMeans[d]));
							
								double[] cost = period25costs(instance, true);
								writeToTextCostResults(instance, cost, true);
								System.out.println("cost+time done");
								
								int [] s = period25reorderPoints(instance, cost, true);
								writeToTextReorderPoints(instance, true, s);
								System.out.println("s done");
								/*
								cost = period25costs(instance, false);
								//s = period25reorderPoints(instance, cost, false);
								writeToTextResults(instance, cost, false);
								System.out.println("False done");
								*/
								
								//status
								//System.out.println(count);
								//count++;
							
							
						}
					//}
				//}
			//}
		//}

	}

}
