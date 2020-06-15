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
	public static void plotTwoCostGivenQ(double[][] costOrder, double[][] costNoOrder, double Q, int stageIndex, InstanceDouble instance) {
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
	public static double[] costVaryingWithInventory(double[] optimalSchedule, InstanceDouble instance, boolean Normal){
		double[] reorderPoint = new double [instance.getStages()];

		/**create array for inventory levels**/
		int[] inventory = new int [instance.maxInventory - instance.minInventory + 1];
		for(int i=0;i<inventory.length;i++) {
			inventory[i] = i + instance.minInventory;
		}
		
		double demandProbabilities [][] = null;
		if(Normal == false) {
			demandProbabilities = sS.computeDemandProbability(instance.demandMean, instance.maxDemand, instance.tail);
		}else {
			demandProbabilities = sS.computeNormalDemandProbability(instance.demandMean, instance.stdParameter, instance.maxDemand, instance.tail);
		}

		double[][] optimalCostByInventory = new double [inventory.length][instance.getStages()];
		boolean[][] optimalActionByInventory= new boolean[inventory.length][instance.getStages()];

		double[][] costOrder = new double[inventory.length][instance.getStages()];
		double[][] costNoOrder = new double[inventory.length][instance.getStages()];

		for(int t=instance.getStages()-1;t>=0;t--) { // Time			   
			for(int i=0;i<inventory.length;i++) { // Inventory 
					/** a = Q (given) **/
					double totalCostOrder = sS.computePurchasingCost(optimalSchedule[t], instance.fixedOrderingCost, instance.unitCost);				 
					double scenarioProb = 0;
					for(int d=0;d<demandProbabilities[t].length;d++) { // Demand
						if((inventory[i] + optimalSchedule[t] - d <= instance.maxInventory) && (inventory[i] + optimalSchedule[t] - d >= instance.minInventory)) {
							totalCostOrder += demandProbabilities[t][d]*(
									sS.computeImmediateCost(
											inventory[i], 
											optimalSchedule[t], 
											d, 
											instance.holdingCost, 
											instance.penaltyCost, 
											instance.fixedOrderingCost, 
											instance.unitCost)
									+ ((t==instance.getStages()-1) ? 0 : optimalCostByInventory[(int) (i+optimalSchedule[t]-d)][t+1]) 
									);
							scenarioProb += demandProbabilities[t][d];
						}
					}
					totalCostOrder /= scenarioProb;
					costOrder[i][t] = totalCostOrder;

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
									+ ((t==instance.getStages()-1) ? 0 : optimalCostByInventory[i-d][t+1]) 
									);
							scenarioProb += demandProbabilities[t][d];
						}
					}
					totalCostNoOrder /= scenarioProb;
					costNoOrder[i][t] = totalCostNoOrder;

					optimalCostByInventory[i][t] = Math.min(totalCostNoOrder, totalCostOrder);
					optimalActionByInventory[i][t] = totalCostNoOrder < totalCostOrder ? false : true;
				
			}
		}
		
		/**if the optimal schedule does not contain a replenishment at period t**/
		for(int t=0; t<instance.getStages();t++) {
			if(optimalSchedule[t] == 0) {
				reorderPoint[t] = instance.minInventory;
			}else {
				for(int i= 0- instance.minInventory; i<inventory.length; i++) {  // Inventory   
					if(optimalActionByInventory[i][t] == false) {
						reorderPoint[t] = i + instance.minInventory;
						break;
					}
				}

			}
		}
		
		/*print all actions by inventory level and time period
		System.out.println();
		for(int i=0; i<inventory.length; i++) {
			System.out.print((i+instance.minInventory)+" \t");
			for(int t=0; t<instance.getStages(); t++) {
				System.out.print(optimalActionByInventory[i][t]+ "\t");
			}
			System.out.println();
		}
		
		
		//print cost by inventory level and time period
		System.out.println();
		for(int i=0; i<inventory.length; i++) {
			System.out.print((i+instance.minInventory)+" \t");
			for(int t=0; t<instance.getStages(); t++) {
				System.out.print(optimalCostByInventory[i][t]+ "\t");
			}
			System.out.println();
		}*/
		
		
		//print cost given a schedule
		//System.out.println("cost when computing reorder points is: "+optimalCostByInventory[(int) (instance.initialInventory - instance.minInventory)][0]);
		return reorderPoint;

	}
	
	public static void main(String[] args) {

		double fixedOrderingCost = 5;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 3;

		double tail = 0.00000001;

		int minInventory = -50;
		int maxInventory = 50;
		int maxQuantity = 9;

		double stdParameter = 0.25;

		double[] demandMean = {2,1,5,3};

		
		double[] optimalSchedule = {3, 3, 5, 4};
		

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

		boolean Normal = false;
		double[] reorderPoints = costVaryingWithInventory(optimalSchedule, instance, Normal);
	    
		
		System.out.print("reorderPoints = {");
		for(int t=0; t<demandMean.length;t++) {
			System.out.print(reorderPoints[t]);
			if(t<demandMean.length-1)System.out.print(",");
		}System.out.print("}");
		System.out.println();
		

	
	}

}
