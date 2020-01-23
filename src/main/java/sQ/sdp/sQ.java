package sQ.sdp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import sS.sdp.sS;
import sdp.data.Instance;
import umontreal.ssj.util.Chrono;

public class sQ {
	
	/** print optimal quantity **/
	public static void printOptimalQuantity(Instance instance, sQsolution sQsolution) {
		System.out.println("Optimal Q for all periods is: ");
		System.out.println(sQsolution.getOpt_aSQ(instance)+1);
	}
	
	/** print cost of optimal quantity **/
	public static void printOpitmalCost(Instance instance, sQsolution sQsolution){
		System.out.println("Optimal cost with initial inventory level " +(instance.initialInventory)+" is: ");
		System.out.println(sQsolution.totalCost[sQsolution.getOpt_aSQ(instance)+1][instance.initialInventory - instance.minInventory][0]);
	}

	/** Plot costs - cost with no action and with a given Q for a given stage**/
	static void plotComparedCosts(Instance instance, sQsolution sQsolution, int t) {
		XYSeries series = new XYSeries("sQ plot");
		for(int a=0;a<instance.maxQuantity;a++) {
			series.add(a,sQsolution.totalCost[a][instance.initialInventory - instance.minInventory][t]);
		}
		XYDataset xyDataset = new XYSeriesCollection(series);
		JFreeChart chart = ChartFactory.createXYLineChart("SDP with (s,Q) policy", "Feasible Replenishment Quantity", "Expected total cost",
				xyDataset, PlotOrientation.VERTICAL, true, true, false);
		ChartFrame frame = new ChartFrame("sQ plot",chart);
		frame.setVisible(true);
		frame.setSize(1500,1200);
	}
	
	public static void presentsQresults(Instance instance, sQsolution sQsolution) {
		//plotComparedCosts(instance, sQsolution, 0);
		//printReorderPoints(instance, sQsolution);
		//System.out.println();
		printOptimalQuantity(instance, sQsolution);
		System.out.println();
		printOpitmalCost(instance, sQsolution);
	}

	/** main computation **/
	public static sQsolution solvesQInstance(Instance instance) {

		int[] inventory = new int [instance.maxInventory - instance.minInventory + 1];
		for(int i=0;i<inventory.length;i++) {
			inventory[i] = i + instance.minInventory;
		}
		
		//demandProbabilities[stages][demandValue] = Prob(dt = demandValue), dt is the realized demand of the random varuable d_t
		double demandProbabilities [][] = sS.computeDemandProbability(instance.demandMean, instance.maxDemand, instance.tail);//Poisson
		//double demandProbabilities[][] = sS.computeNormalDemandProbability(instance.demandMean, instance.stdParameter, instance.maxDemand, instance.tail);
		
		double totalCost[][][] = new double[instance.maxQuantity+1][inventory.length][instance.getStages()];
		boolean optimalAction[][][] = new boolean [instance.maxQuantity + 1][inventory.length][instance.getStages()];
		
		/** for check **/
		double[][][] costOrder = new double[instance.maxQuantity+1][inventory.length][instance.getStages()];
		double[][][] costNoOrder = new double[instance.maxQuantity+1][inventory.length][instance.getStages()];

		long startTime = System.currentTimeMillis();
		for(int a=0; a<=instance.maxQuantity;a++) { //"a" represents the action index, so the actual action volume is a+1
			for(int t=instance.getStages()-1;t>=0;t--) { // Time			   
				for(int i=0;i<inventory.length;i++) { // Inventory   
					
					double totalCostOrder = sS.computePurchasingCost(a, instance.fixedOrderingCost, instance.unitCost); 
					double totalCostNoOrder = 0;
					double scenarioProbOrder = 0;
					double scenarioProbNo = 0;
					
					for(int d=0;d<demandProbabilities[t].length;d++) { // Demand
						/** a > 0**/
						if((inventory[i] + a - d <= instance.maxInventory) && (inventory[i] + a - d >= instance.minInventory)) {
							totalCostOrder += demandProbabilities[t][d]*(
											sS.computeImmediateCost(
											inventory[i], 
											a, 
											d, 
											instance.holdingCost, 
											instance.penaltyCost, 
											instance.fixedOrderingCost, 
											instance.unitCost)
											+ ((t==instance.getStages()-1) ? 0 : totalCost[a][i+a-d][t+1]) 
									);
							scenarioProbOrder += demandProbabilities[t][d];
						}
						/** a = 0**/
						if((inventory[i] - d <= instance.maxInventory) && (inventory[i] - d >= instance.minInventory)) {
							totalCostNoOrder += demandProbabilities[t][d]*(
									sS.computeImmediateCost(
											inventory[i], 
											0, 
											d, 
											instance.holdingCost, 
											instance.penaltyCost, 
											instance.fixedOrderingCost, 
											instance.unitCost)
									+ ((t==instance.getStages()-1) ? 0 : totalCost[a][i-d][t+1]) 
									);
							scenarioProbNo += demandProbabilities[t][d];
						}
					}
					totalCostOrder /= scenarioProbOrder;
					totalCostNoOrder /= scenarioProbNo;
					
					costOrder[a][i][t] 		= totalCostOrder;
					costNoOrder[a][i][t] 	= totalCostNoOrder;
					
					totalCost[a][i][t] = Math.min(totalCostNoOrder, totalCostOrder);
					optimalAction[a][i][t] = totalCostNoOrder < totalCostOrder ? false : true;
				}
			}
		}
		long endTime=System.currentTimeMillis();
		long timeConsumedsQ = endTime - startTime;
		System.out.println(timeConsumedsQ + " ms");
		
		writeToText(costOrder);
		writeToText(costNoOrder);
		
		return new sQsolution(totalCost, optimalAction, inventory, timeConsumedsQ);
	}

	public static void main(String[] args) {

		Chrono timer = new Chrono();

		double fixedOrderingCost = 10;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 5;

		double tail = 0.00000001;

		int minInventory = -50;
		int maxInventory = 50;
		int maxQuantity = 9;

		double stdParameter = 0.25;

		//int[] demandMean = {20, 40, 60, 40};
		int[] demandMean = {2,4,6,4};
		
		Instance instance = new Instance(
				fixedOrderingCost,
				unitCost,
				holdingCost,
				penaltyCost,
				demandMean,
				tail,
				minInventory,
				maxInventory,
				maxQuantity,
				stdParameter
				);

		/** Solve the classic instance **/
		sQsolution sQsolution = solvesQInstance(instance);
		
		/*
		boolean optActPeriod0[][] = new boolean[instance.maxInventory - instance.minInventory + 1][instance.maxQuantity + 1];
		for(int i = 0; i < optActPeriod0.length; i++) {
			for(int a = 0; a < optActPeriod0[i].length; a++) {
				optActPeriod0[i][a] = sQsolution.optimalAction[a][i][0];
			}	
		}
		System.out.println(Arrays.deepToString(optActPeriod0)); //Careful, BIG MATRIX
		 */

		presentsQresults(instance, sQsolution);
		
		//present ETC of inventory 0 with the optimal quantity
		System.out.println();
		for(int t=0; t<instance.getStages();t++) {
			System.out.println("a: " + (sQsolution.getOpt_aSQ(instance)+1) + "\t"
								+ "t: "+ (t+1)+ "\t"  
								+sQsolution.totalCost[sQsolution.getOpt_aSQ(instance)+1][instance.initialInventory - instance.minInventory][t]);
		}
		
		for(int i=0; i<sQsolution.inventory.length;i++) {
			System.out.print("i: "+ (i+instance.minInventory) + "\t");
			for(int t=0; t<instance.demandMean.length; t++) {
				System.out.print(sQsolution.totalCost[sQsolution.getOpt_aSQ(instance)+1][i][t] + "\t");
			}
			System.out.println();
		}
		
		writeToText(sQsolution.totalCost);
		

	}
	
	
	
	public static void writeToText(double[][][] totalCost){
		FileWriter fw = null;
		try {
			File f = new File("E:\\sQresultCheck.txt");
			fw = new FileWriter(f, true);//true, continue to write
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(fw);
		pw.println(Arrays.deepToString(totalCost));
		pw.println();pw.println();pw.println();
		pw.flush();
		try {
			fw.flush();
			pw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
