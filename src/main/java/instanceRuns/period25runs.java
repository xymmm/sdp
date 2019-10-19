package instanceRuns;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import sQ.sdp.sQ;
import sQ.sdp.sQgivenQ;
import sQ.sdp.sQgivenQsolution;
import sQ.sdp.sQsolution;
import sS.sdp.sS;
import sS.sdp.sSsolution;
import sdp.data.Instance;

public class period25runs {
	
	public static double[] period25costs(Instance instance, boolean initialOrder) {
		/**run sS, sQ-Q costs for 25-period instances. results = [cost_sS, cost_sQ-Q, Q]**/
		double[] results = new double[3];
		//sS
		sSsolution sSsolution = sS.solveInstance(instance, initialOrder);
		results[0] = sSsolution.optimalCost[instance.initialInventory - instance.minInventory][0];
		//sQ
		sQsolution sQsolution = sQ.solvesQInstance(instance,initialOrder);
		results[1] = sQsolution.totalCost[instance.initialInventory - instance.minInventory][sQsolution.getOpt_aSQ(instance)+1][0];
		results[2] = sQsolution.getOpt_aSQ(instance)+1;
		return results;
	}
	
	public static int[] period25reorderPoints(Instance instance, double[] results, boolean initialOrder){
		/** run sQ-Q for s**/
		int[] s = new int[25];
		sQgivenQsolution sQs = sQgivenQ.costVaryingWithInventory((int) results[2],instance,initialOrder);
		s = sQs.getsGivenQ(instance, sQs);
		return s;
	}
	
	public static void writeToTextParameters(int fixedOrderingCost, int penaltyCost, int unitCost, double stdParameter){
		FileWriter fw = null;
		try {
			File f = new File("E:\\25periods.txt");
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
	
	public static void writeToTextResults(Instance instance, double[] results, boolean initialOrder) {//int s
		FileWriter fw = null;
		try {
			File f = new File("E:\\25periods.txt");
			fw = new FileWriter(f, true);//true, continue to write
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(fw);
		pw.println(initialOrder + "     "+results[0]+"     "+ results[1] +"     "+results[2]);
		/*
		for(int t=0; t<instance.getStages();t++) {
			pw.print(s[t]+"   ");
		}*/
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
		int[] penaltyCost = {5,10,20};
		int[] unitCost = {0,1};
		int[] fixedCost = {200,300,400};
		double tail = 0.00000001;
		int minInventory = -1000;
		int maxInventory = 1000;
		int maxQuantity = 1000;
		
		/**demand distributions**/
		double[] stdParameter = {0.1,0.2,0.3};
		int demandMeans[][] = {
				{11,17,26,38,53,71,92,115,138,159,175,186,190,186,175,159,138,115,92,71,53,38,26,17,11}, 				//LCY1
				{23,32,42,55,70,86,103,120,136,150,161,168,170,168,161,150,136,120,103,86,70,55,42,32,23},				//LCY2
				{130,150,127,76,27,10,36,88,136,149,121,68,22,11,42,96,140,148,114,60,18,14,50,104,144},				//SIN1
				{122,130,120,98,77,70,81,103,124,130,118,95,75,71,84,107,126,129,115,91,73,72,87,110,127},				//SIN2
				{100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100},	//STA
				{178,178,136,211,119,165,47,100,62,31,43,199,172,96,69,8,29,135,97,70,248,57,11,94,13},					//RAND
				{2,51,152,467,268,489,446,248,281,363,155,293,220,93,107,234,124,184,223,101,123,99,31,82},			//EMP1
				{47,81,236,394,164,287,508,391,754,694,261,195,320,111,191,160,55,84,58},					//EMP2
				{44,116,264,144,146,198,74,183,204,114,165,318,119,482,534,136,260,299,76,218,323,102,174,284},		//EMP3
				{49,188,64,279,453,224,223,517,291,547,646,224,215,440,116,185,211,26,55}					//EMP4
				};
		
		System.out.println("total number = "+penaltyCost.length * unitCost.length * fixedCost.length * stdParameter.length * demandMeans.length);
		int count = 1;
		
		for(int p=0; p<penaltyCost.length; p++) {
			for(int u=0;u<unitCost.length;u++) {
				for(int f=0;f<fixedCost.length;f++) {
					for(int std = 0; std<stdParameter.length;std++) {
						
						writeToTextParameters(fixedCost[f], penaltyCost[p], unitCost[u], stdParameter[std]);
						
						for(int d = 0; d<demandMeans.length; d++) {
							
							Instance instance = new Instance(fixedCost[f],unitCost[u],holdingCost,penaltyCost[p],demandMeans[d],
									tail,minInventory,maxInventory,maxQuantity,stdParameter[std]);
							
								double[] cost = period25costs(instance, true);
								//int[] s = period25reorderPoints(instance, cost, true);
								writeToTextResults(instance, cost, true);
								
								cost = period25costs(instance, false);
								//s = period25reorderPoints(instance, cost, false);
								//writeToTextResults(instance, cost, false);
								
								//status
								System.out.println(count);
								count++;
							
							
						}
					}
				}
			}
		}

	}

}
