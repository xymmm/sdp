package sQ.sdp;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import sS.sdp.sS;
import sdp.data.Instance;

public class sQgivenQ {
	
	/**plot TWO costs with a given Q - Reorder & Non-reorder**/
	public static void plotTwoCostGivenQ(double[] costOrder, double[] costNoOrder, int Q, int stageIndex, Instance instance, double costLimit) {
		  XYSeries series1 = new XYSeries("Cost with Reorder");
	      for(int i=0;i<costOrder.length;i++) {
	    	  if(costOrder[i]<costLimit) series1.add((i+instance.minInventory),costOrder[i]);
	      }
	      XYSeries series2 = new XYSeries("Cost with No Reorder");
	      for(int i=0;i<costOrder.length;i++) {
	    	  if(costNoOrder[i]<costLimit) series2.add((i+instance.minInventory),costNoOrder[i]);
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
	

	/****compute cost function f(Q,t,i) with given t and Q****/
	public static sQgivenQsolution costVaryingWithInventory(int Q, Instance instance, boolean initialOrder){
		int[] inventory = new int [instance.maxInventory - instance.minInventory + 1];
		for(int i=0;i<inventory.length;i++) {
			inventory[i] = i + instance.minInventory;
		}

		double[][] costGivenQ = new double [instance.getStages()][inventory.length];
		boolean[][] actionGivenQ = new boolean[instance.getStages()][inventory.length];
		
		double[][] costOrder = new double[instance.getStages()][inventory.length];
		double[][] costNoOrder = new double[instance.getStages()][inventory.length];

		double demandProbabilities [][] = sS.computeDemandProbability(instance.demandMean, instance.maxDemand, instance.tail);
		//double demandProbabilities[][] = sS.computeNormalDemandProbability(instance.demandMean, instance.stdParameter, instance.maxDemand, instance.tail);
		
		for(int t=instance.getStages()-1;t>=0;t--) { // Time			   
			for(int i=0;i<inventory.length;i++) { // Inventory   
				/** a = Q (given) **/
				Q = ((t==0)&&(!initialOrder)) ?  0 : Q;
				double totalCostOrder = sS.computePurchasingCost(Q, instance.fixedOrderingCost, instance.unitCost); 
				double scenarioProb = 0;
				for(int d=0;d<demandProbabilities[t].length;d++) { // Demand
					if((inventory[i] + Q - d <= instance.maxInventory) && (inventory[i] + Q - d >= instance.minInventory)) {
						totalCostOrder += demandProbabilities[t][d]*(
								sS.computeImmediateCost(
										inventory[i], 
										Q, 
										d, 
										instance.holdingCost, 
										instance.penaltyCost, 
										instance.fixedOrderingCost, 
										instance.unitCost)
								+ ((t==instance.getStages()-1) ? 0 : costGivenQ[t+1][i+Q-d]) 
								);
						scenarioProb += demandProbabilities[t][d];
					}
				}
				totalCostOrder /= scenarioProb;
				costOrder[t][i] = totalCostOrder;

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

				costGivenQ[t][i] = Math.min(totalCostNoOrder, totalCostOrder);
				actionGivenQ[t][i] = totalCostNoOrder < totalCostOrder ? false : true;
			}
		}

		return new sQgivenQsolution(inventory, costGivenQ, actionGivenQ, costOrder, costNoOrder);
	}
	
	
	
	public static void main(String[] args) {

		double fixedOrderingCost = 100;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 10;

		double tail = 0.00000001;

		int minInventory = -500;
		int maxInventory = 500;
		int maxQuantity = 500;

		double stdParameter = 0.25;

		int[] demandMean = {20, 40, 60, 40};
		
		int[][] demandMeanInput = {
				{20, 40, 60, 40, 20, 40},
				{40, 60, 40},
				{60, 40},
				{40}
		};

		int Q = 83;
		
		double[] costLimit = {20000, 15000, 10000, 5200};
				
		for(int d=0; d<1;d++) {

			/** create and resolve instance**/
			Instance instance = new Instance(fixedOrderingCost, unitCost, holdingCost, penaltyCost, demandMeanInput[d], 
					tail, minInventory, maxInventory, maxQuantity, stdParameter );	
			//determine s by compare c(s) and c(s+Q)
			sQgivenQsolution sQgivenQ = costVaryingWithInventory(Q,instance,false);
			
			/**print and plot ETC**/
			System.out.println("cost with initial stock = "+instance.initialInventory+" is "+sQgivenQ.costGivenQ[0][instance.initialInventory-instance.minInventory]);
			sdp.util.plotOneDimensionArray.plotCostGivenQGivenStage(sQgivenQ.costGivenQ[0], sQgivenQ.inventory, "inventory level", "expected cost", "Expected cost without initial order t=1");//cost
			for(int i=instance.initialInventory-instance.minInventory; i<sQgivenQ.inventory.length; i++) {
				System.out.println((i+instance.minInventory)+"   "+sQgivenQ.costGivenQ[0][i]);
			}
			
			/**resolve reorder points by cost differences**/
			double[] costDifference = new double[maxInventory-minInventory+1-Q];
			for(int j=0; j<costDifference.length; j++) {
				costDifference[j] = sQgivenQ.costGivenQ[0][j] - sQgivenQ.costGivenQ[0][j+Q];
				//System.out.println(costDifference[j]);
			}
			sdp.util.plotOneDimensionArray.plotCostGivenQGivenStage(costDifference, sQgivenQ.inventory, "inventory level", "cost difference", "t="+(d+1));//cost difference
			for(int j=0; j<costDifference.length-1; j++) {
				if(costDifference[j] <= fixedOrderingCost) {
					System.out.println("BreakPoints = "+(j + minInventory));
					break;
				}
			}
			
			/**determine s by compare c(order) and c(no order)**/
			sQgivenQsolution sQgivenQorder = costVaryingWithInventory(Q,instance, true);
			
				plotTwoCostGivenQ(sQgivenQorder.costOrder[0], sQgivenQorder.costNoOrder[0], Q, 0, instance,costLimit[d]);
			
			int[] s = sQgivenQsolution.getsGivenQ(instance, sQgivenQorder);
			System.out.println("reorder points by comparing actions = " + s[0]);
		
		}


		/* print costs for MATLAB plots
		for(int t=0;t<instance.getStages();t++) {
			System.out.println("t="+t+"===========================================================");
			//for(int i=instance.initialInventory-instance.minInventory;i<instance.initialInventory-50-instance.minInventory+251;i++) {
				//System.out.println(sQgivenQ.costOrder[t][i]);
			//}System.out.println();
			for(int i=instance.initialInventory-instance.minInventory;i<instance.initialInventory-50-instance.minInventory+251;i++) {
				System.out.println(sQgivenQ.costNoOrder[t][i]);
			}System.out.println();
		}


		System.out.print("reorderPoints = {");
			for(int t=0; t<costGivenQ.length;t++) {
				System.out.print(sGivenQ[t]);
				if(t<costGivenQ.length-1)System.out.print(",");
			}System.out.print("}");System.out.println();
		 */

		
	}

}
