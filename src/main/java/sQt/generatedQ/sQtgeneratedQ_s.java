package sQt.generatedQ;

import java.util.Arrays;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import sQ.sdp.sQgivenQsolution;
import sS.sdp.sS;
import sdp.data.Instance;

public class sQtgeneratedQ_s {
	
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
	public static void plotTwoCostGivenQ(double[][] costOrder, double[][] costNoOrder, int Q, int stageIndex, Instance instance) {
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
	public static sQtgeneratedQ_s_solution costVaryingWithInventory(int[] Q, Instance instance, boolean initialOrder){
		int[] inventory = new int [instance.maxInventory - instance.minInventory + 1];
		for(int i=0;i<inventory.length;i++) {
			inventory[i] = i + instance.minInventory;
		}

		double[][] costGivenQ = new double [instance.getStages()][inventory.length];
		boolean[][] actionGivenQ = new boolean[instance.getStages()][inventory.length];
		
		double[][] costOrder = new double[instance.getStages()][inventory.length];
		double[][] costNoOrder = new double[instance.getStages()][inventory.length];

		double demandProbabilities [][] = sS.computeDemandProbability(instance.demandMean, instance.maxDemand, instance.tail);//Poisson
		//double demandProbabilities[][] = sS.computeNormalDemandProbability(instance.demandMean, instance.stdParameter, instance.maxDemand, instance.tail);//normal
			
		for(int t=instance.getStages()-1;t>=0;t--) { // Time			   
			for(int i=0;i<inventory.length;i++) { // Inventory   
				/** a = Q (given) **/
				Q[t] = ((t==0)&&(!initialOrder)) ?  0 : Q[t];
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

		return new sQtgeneratedQ_s_solution(costGivenQ, actionGivenQ, costOrder, costNoOrder);
	}
	
	public static void main(String[] args) {

		double fixedOrderingCost = 10;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 5;

		double tail = 0.00000001;

		int minInventory = -50;
		int maxInventory = 50;
		int maxQuantity = 9;

		double stdParameter = 0.25;

		int[] demandMean = {2,4,6,4};
		int[][] demandMeanInput = {
				{2,4,6,4},
				{4,6,4},
				{6,4},
				{4}
		};
		
		int[] Q = {8, 0, 9, 0};
		
		for(int d=0; d<demandMeanInput.length; d++) {
		Instance instance = new Instance(
				fixedOrderingCost,
				unitCost,
				holdingCost,
				penaltyCost,
				demandMeanInput[d],
				tail,
				minInventory,
				maxInventory,
				maxQuantity,
				stdParameter
				);
		
		sQtgeneratedQ_s_solution sQgivenQ = costVaryingWithInventory(Q,instance,true);
		
		double costGivenQ[][] = sQgivenQ.costGivenQ;
		int[] sGivenQ = sQgivenQ.getsGivenQ(instance, sQgivenQ);
		
		
		for(int t=0; t<costGivenQ.length;t++) {
		    //System.out.println("t: "+ (t+1)+ "\t"+ sQgivenQ.costGivenQ[t][instance.initialInventory-instance.minInventory]);
			//plotTwoCostGivenQ(sQgivenQ.costOrder, sQgivenQ.costNoOrder, Q[t], t, instance);
		}
	    System.out.println(Arrays.toString(sQgivenQ.costGivenQ[0]));
		
	    /*
		System.out.print("reorderPoints = {");
		for(int t=0; t<costGivenQ.length;t++) {
			System.out.print(sGivenQ[t]);
			if(t<costGivenQ.length-1)System.out.print(",");
		}System.out.print("}");
		System.out.println();
		*/

	}
	}

}
