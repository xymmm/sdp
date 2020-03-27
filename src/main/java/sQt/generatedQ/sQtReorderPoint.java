package sQt.generatedQ;

import java.util.Arrays;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import sQ.sdp.sQreorderPointSolution;
import sS.sS;
import sdp.data.Instance;
import sdp.data.InstanceDouble;

public class sQtReorderPoint {
	
	/**plot OPTIMAL cost with a given Q**/
	public static void plotCostGivenQGivenStage(double[][] costGivenQ, int Q, int stageIndex, Instance instance) {
		XYSeries series = new XYSeries("Optimal Cost with Given Q");
		for(int i = 0; i<instance.maxInventory - instance.minInventory + 1; i++) {
			series.add((i+instance.minInventory),costGivenQ[stageIndex][i]);
		}
		XYDataset xyDataset = new XYSeriesCollection(series);
		JFreeChart chart = ChartFactory.createXYLineChart("expected cost with given Q="+Q+" at period "+(stageIndex+1), "inventory level", "expected cost",
				xyDataset, PlotOrientation.VERTICAL, true, true, false);
		ChartFrame frame = new ChartFrame("Cost with Given Q",chart);
		frame.setVisible(true);
		frame.setSize(1800,1500);
	}
	/**plot TWO costs with a given Q - Reorder & Non-reorder**/
	public static void plotTwoCostGivenQ(double[][] costOrder, double[][] costNoOrder, int Q, int stageIndex, InstanceDouble instance) {
		  XYSeries series1 = new XYSeries("Cost with Reorder");
	      for(int i=0;i<costOrder[0].length;i++) {
	    	  series1.add((i+instance.minInventory),costOrder[stageIndex][i]);
	      }
	      XYSeries series2 = new XYSeries("Cost with No Reorder");
	      for(int i=0;i<costOrder[0].length;i++) {
	    	  series2.add((i+instance.minInventory),costNoOrder[stageIndex][i]);
	      }
		  XYSeriesCollection collection = new XYSeriesCollection();
		  collection.addSeries(series1);
		  collection.addSeries(series2);
	      JFreeChart chart = ChartFactory.createXYLineChart("Expected Cost of Reordering and No Reordering with Given Q="+Q+" at period "+(stageIndex+1), "inventory level", "expected cost",
	            collection, PlotOrientation.VERTICAL, true, true, false);
	      ChartFrame frame = new ChartFrame("ETC Comparison",chart);
	      frame.setVisible(true);
			frame.setSize(1800,1500);
	}
	
	/**plot reorder points varying with Q**/
	public static void plotsGivenQforAllQ(int[][] s, Instance instance, int stageIndex) {
		XYSeries series = new XYSeries("reorder point with Given Qs");
		for(int q=0; q<instance.maxQuantity;q++) {
			if(s[q][stageIndex]>=-30) series.add((q+1),s[q][stageIndex]);
		}
		XYDataset xyDataset = new XYSeriesCollection(series);
		JFreeChart chart = ChartFactory.createXYLineChart("reorder point s varying with given Qs"+" at period "+(stageIndex+1), "feasible quantities", "reorder points",
				xyDataset, PlotOrientation.VERTICAL, true, true, false);
		ChartFrame frame = new ChartFrame("reorder point with Given Qs",chart);
		frame.setVisible(true);
		frame.setSize(1800,1500);
	}

	/****compute cost function f(Q,t,i) with given t and Q****/
	public static sQtReorderPointSolution costVaryingWithInventory(int[] Q, InstanceDouble instance, boolean initialOrder, boolean Normal){
		int[] inventory = new int [instance.maxInventory - instance.minInventory + 1];
		for(int i=0;i<inventory.length;i++) {
			inventory[i] = i + instance.minInventory;
		}

		double[][] costGivenQ = new double [instance.getStages()][inventory.length];
		boolean[][] actionGivenQ = new boolean[instance.getStages()][inventory.length];
		
		double[][] costOrder = new double[instance.getStages()][inventory.length];
		double[][] costNoOrder = new double[instance.getStages()][inventory.length];

		double demandProbabilities [][] = null;
		if(Normal == false) {
			demandProbabilities = sS.computeDemandProbability(instance.demandMean, instance.maxDemand, instance.tail);
		}else {
			demandProbabilities = sS.computeNormalDemandProbability(instance.demandMean, instance.stdParameter, instance.maxDemand, instance.tail);
		}
		
		long startTime = System.currentTimeMillis();
		
		for(int t=instance.getStages()-1;t>=0;t--) { // Time			   
			for(int i=0;i<inventory.length;i++) { // Inventory   
				/** a = Q (given) **/
				//Q[t] = ((t==0)&&(!initialOrder)) ?  0 : Q[t];
				double totalCostOrder = sS.computePurchasingCost(Q[t], instance.fixedOrderingCost, instance.unitCost); 
				double scenarioProb = 0;
				for(int d=0;d<demandProbabilities[t].length;d++) { // Demand
					if((inventory[i] + Q[t] - d <= instance.maxInventory) && (inventory[i] + Q[t] - d >= instance.minInventory)) {
						totalCostOrder += demandProbabilities[t][d]*(
								sS.computeImmediateCost(
										inventory[i], 
										Q[t], 
										d, 
										instance.holdingCost, 
										instance.penaltyCost, 
										instance.fixedOrderingCost, 
										instance.unitCost)
								+ ((t==instance.getStages()-1) ? 0 : costGivenQ[t+1][i+Q[t]-d]) 
								);
						scenarioProb += demandProbabilities[t][d];
					}
				}
				totalCostOrder /= scenarioProb;
				costOrder[t][i] = totalCostOrder;
				//if(i==instance.initialInventory - instance.minInventory) System.out.println(costOrder[t][instance.initialInventory - instance.minInventory]);
				
				/** a = 0**/
				double totalCostNoOrder = 0;
				scenarioProb = 0;
				for(int d=0;d<demandProbabilities[t].length;d++) { // Demand
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
								+ ((t==instance.getStages()-1) ? 0 : costGivenQ[t+1][i-d]) 
								);
						scenarioProb += demandProbabilities[t][d];
					}
				}
				totalCostNoOrder /= scenarioProb;
				costNoOrder[t][i] = totalCostNoOrder;
				//if(i==instance.initialInventory - instance.minInventory) System.out.println(costNoOrder[t][instance.initialInventory - instance.minInventory]);

				costGivenQ[t][i] = Math.min(totalCostNoOrder, totalCostOrder);
				actionGivenQ[t][i] = totalCostNoOrder < totalCostOrder ? false : true;
			}
		}
		
		long endTime = System.currentTimeMillis();
		long timeConsumed_sQtst = endTime - startTime;

		return new sQtReorderPointSolution(costGivenQ, actionGivenQ, costOrder, costNoOrder, timeConsumed_sQtst);
	}
	
	public static void main(String[] args) {

		double fixedOrderingCost = 500;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 20;

		double tail = 0.00000001;

		int minInventory = -1500;
		int maxInventory = 1500;
		int maxQuantity = 800;

		double stdParameter = 0.1;
		
		boolean Normal = true;

		double[] demandMean = {130,150,127,76,27,10,36,88,136,149,121,68,22,11,42,96,140,148,114,60,18,14,50,104,144};

		
		int[] Q = {197,288,164,85,58,146,255,171,418,274,157,86,66,160,268,178,394,266,158,94,74,72,282,175,51
};
		

		InstanceDouble instance = new InstanceDouble(
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
		
		sQtReorderPointSolution sQgivenQ = costVaryingWithInventory(Q,instance,true, Normal);
		
		double costGivenQ[][] = sQgivenQ.costGivenQ;
		int[] sGivenQ = sQgivenQ.getsGivenQ(instance, sQgivenQ);
		
		/*
		for(int t=0; t<1;t++) {
			for(int i=0; i<costGivenQ[t].length; i++) {
		    System.out.println(i+minInventory + "\t" + sQgivenQ.costGivenQ[t][i]);
			}
			System.out.println();
			plotTwoCostGivenQ(sQgivenQ.costOrder, sQgivenQ.costNoOrder, Q[t], t, instance);
		}*/
		
	    
		System.out.print("reorderPoints = {");
		for(int t=0; t<costGivenQ.length;t++) {
			System.out.print(sGivenQ[t]);
			if(t<costGivenQ.length-1)System.out.print(",");
		}System.out.print("}");
		System.out.println();
		

	
	}

}
