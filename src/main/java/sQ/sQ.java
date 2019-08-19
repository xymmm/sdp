package sQ;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import sQ.data.sQsolution;
import sdp.data.Instance;
import sdp.data.Solution;
import sdp.util.Demand;

public class sQ {


	/**Static Methods**/


	/** compute demand probability
	 * 
	 * This method computes the probability of a given demand value.
	 * A Poisson distribution is generated according to the given mean of demand.
	 * The demand probability takes the value of Pr(x = demand) in this Poisson distribution.
	 * The probability value is discarded if it is smaller than a given truncation quantile.
	 * 
	 * **/
	static double[][] computeDemandProbability(int[] demandMean, int maxDemand, double tail) {
		double[][] demandProbability = new double [demandMean.length][maxDemand+1];
		for(int t=0; t<demandMean.length;t++) {
			demandProbability[t] = Demand.tabulateProbability(demandMean[t], tail);
		}
		return demandProbability;
	}

	/** compute immediate cost with actions
	 * 
	 * This method computes the immediate cost for periods except the first. 
	 * For an action with non-zero quantity, the immediate cost includes the ordering cost, holding cost and penalty cost; 
	 * otherwise, the immediate cost only incurs on the holding and penalty cost.
	 * The immediate cost will be multiplied by the (transition) demand probability to obtain a expected cost.
	 * **/
	static double computeImmediateCost(
			int inventoryLevel, 
			int Actions, 
			int demand,
			double holdingCost, 
			double penaltyCost, 
			double fixedOrderingCost, 
			double unitCost) {

		return holdingCost*Math.max(0, inventoryLevel + Actions - demand) + penaltyCost *Math.max(0, demand - inventoryLevel - Actions);

	}

	/** compute purchasing cost according to order quantity (action) **/
	static double computePurchasingCost(int a, double fixedOrderingCost, double unitCost) {
		return a > 0 ? fixedOrderingCost + a*unitCost : 0;
	}

	/** get action option by comparing two costs**/
	public static int getOptimalAction(double CostNoAction, double totalCost) {
		if(CostNoAction <= totalCost) {
			return 0;
		}else {
			return 1;			  
		}
	}

	/** get reorder points **/
	public static int[][] getReorderPoints(Instance instance, int[] inventory, int[][][]OptimalAction) {
		int[][] reorderPoint = new int [instance.maxQuantity][instance.getStages()-1];
		for(int a=0; a<instance.maxQuantity;a++) {
			for(int t = 0;t<=instance.getStages()-2;t++) {
				for (int i=0;i<inventory.length;i++) {
					if(OptimalAction[i][a][t]==1) {
						reorderPoint[a][t] = i;
						break;
					}
				}
			}
		}
		return reorderPoint;
	}


	/** print reorder points**/
	public static void printReorderPoints(Instance instance, sQsolution sQsolution) {
		for(int a=0; a<instance.maxQuantity;a++) {
			System.out.print((a+1)+" ");
			for(int t = 0;t<=instance.getStages()-2;t++) {
				System.out.print(sQsolution.reorderPoint[a][t]+" ");
			}System.out.println();
		}
	}
	
	/** plot cost - with no action for all stages**/
	public static void plotCostNoAction(Instance instance, sQsolution sQsolution, int stageIndex) {
	      XYSeries series1 = new XYSeries("Optimal policy");
	      for(int i=0;i<sQsolution.inventory.length;i++) {
	    	  series1.add(i+instance.minInventory,sQsolution.CostNoAction[i][stageIndex]);
	      }
	      XYDataset xyDataset = new XYSeriesCollection(series1);
	      JFreeChart chart = ChartFactory.createXYLineChart("ETC for Period "+ (stageIndex+1)+" with no action", "Opening inventory level", "Expected total cost",
	    		  xyDataset, PlotOrientation.VERTICAL, true, true, false);
	      ChartFrame frame = new ChartFrame("Period "+(stageIndex+1),chart);
	      frame.setVisible(true);
	      frame.setSize(1500,1200);
	}
	
	/** Plot costs - cost with no action and with a given Q for a given stage**/
	public static void plotComparedCosts(Instance instance, sQsolution sQsolution, int Q, int stageIndex, boolean seeAll) {
	      XYSeries series1 = new XYSeries("No action");
	      for(int i=(seeAll?0:-instance.minInventory);i<sQsolution.inventory.length;i++) {
	    	  series1.add(i+instance.minInventory,sQsolution.CostNoAction[i][stageIndex+1]);
	      }
	      XYSeries series2 = new XYSeries("Action of "+Q);
	      for(int i=(seeAll?0:-instance.minInventory);i<sQsolution.inventory.length;i++) {
	    	  series2.add(i+instance.minInventory,sQsolution.totalCost[i][Q-1][stageIndex]);
	      }
		  XYSeriesCollection collection = new XYSeriesCollection();
		  collection.addSeries(series1);
		  collection.addSeries(series2);
	      JFreeChart chart = ChartFactory.createXYLineChart("Expected Total Cost for Period "+ (stageIndex+2), "Opening inventory level", "Expected total cost",
	            collection, PlotOrientation.VERTICAL, true, true, false);
	      ChartFrame frame = new ChartFrame("Period "+(stageIndex+2),chart);
	      frame.setVisible(true);
	      frame.setSize(1500,1200);
	}

	/** main computation **/
	public static sQsolution solvesQInstance(Instance instance) {

		int Stages = instance.getStages();
		int[] inventory = new int [instance.maxInventory - instance.minInventory + 1];
		for(int i=0;i<inventory.length;i++) {
			inventory[i] = i + instance.minInventory;
		}
		double demandProbabilities [][] = computeDemandProbability(instance.demandMean, instance.maxDemand, instance.tail);


		/** when a=0 
		 * 
		 * This array stores the cost for an inventory level at one stage, when no replenishment order is placed.
		 * The first index represents the inventory level, and the second index represents the stages.
		 * **/
		double CostNoAction [][] = new double [inventory.length][Stages];

		/** Cost Computation a=0 **/
		for(int t=instance.getStages()-1;t>=0;t--) { // Time       	                
			for(int i=0;i<inventory.length;i++) { // Inventory
				double scenarioProb = 0;
				for(int d=0;d<demandProbabilities[t].length;d++) { // Demand
					double immediateCost;
					if((inventory[i] - d <= instance.maxInventory) && (inventory[i] - d >= instance.minInventory)) {// no action taken
						immediateCost = demandProbabilities[t][d]*(
								computeImmediateCost(inventory[i], 0, d, instance.holdingCost, instance.penaltyCost, instance.fixedOrderingCost, instance.unitCost)
								+ ((t==Stages-1) ? 0 : CostNoAction[i-d][t+1]) );// no previous cost for the last stage
						scenarioProb += demandProbabilities[t][d];
						CostNoAction[i][t] = CostNoAction[i][t] + immediateCost;
					}//else
				}
				CostNoAction[i][t] = CostNoAction[i][t]/scenarioProb;
			}
		}

		/** when a=Q 
		 * 
		 * The fist index represents the inventory level, second index represents the action quantity, and third index represents stages.
		 *  An entry of totalCost[][] represents the cost for an [inventory level] with an [action] at one [stage].
		 *  
		 *  For an inventory level, the cost under an action Q=* will be compared with CostNoAction[i][:] for all stages 
		 *  	to decide if it is worthwhile to place the replenishment order at that stage.
		 * 
		 *no action in the first period, so only consider costs with action for period 2, 3 and 4.
		 ***/
		double totalCost[][][] = new double[inventory.length][instance.maxQuantity][instance.getStages()-1];
		
		/** This array stores the comparison between cost with no action and cost with action Q, for period 2,3 and 4**/
		int OptimalAction[][][] = new int [inventory.length][instance.maxQuantity][instance.getStages()-1];

		/** Cost Computation a = Q, single Q for all periods **/
		for(int a=0; a<instance.maxQuantity;a++) { //"a" represents the action index, so the actual action volume is a+1

			for(int i=0;i<inventory.length;i++) { // Inventory
				for(int t=instance.getStages()-2;t>=0;t--) { // Time       	                

					totalCost[i][a][t]= computePurchasingCost(a+1, instance.fixedOrderingCost, instance.unitCost);
					//first "a" is used as index, second "a+1" is used as action volume
					double scenarioProb = 0;

					for(int d=0;d<demandProbabilities[t+1].length;d++) { // Demand
						double immediateCost;
						if((inventory[i] + (a+1) - d <= instance.maxInventory) && (inventory[i] + (a+1) - d >= instance.minInventory)) {//"a+1" as action volumn
							if((i==500)&&(a==249)&&(t==2)) {System.out.println("feasible demand value");}
							if((i==500)&&(a==249)&&(t==2)) {System.out.println(d);}
							immediateCost = demandProbabilities[t+1][d]*(
									computeImmediateCost(inventory[i], (a+1), d, instance.holdingCost, instance.penaltyCost, instance.fixedOrderingCost, instance.unitCost)
									+ ((t==instance.getStages()-2) ? 0 : totalCost[i+(a+1)-d][a][t+1]) );//computation on the index, so (a+1) is not used.
							scenarioProb += demandProbabilities[t+1][d];
							totalCost[i][a][t] = totalCost[i][a][t] + immediateCost;
						}
					}
					totalCost[i][a][t] = totalCost[i][a][t]/scenarioProb;
					if((i==500)&&(a==249)&&(t==2)) {
						System.out.println("inventory leve = "+(i+instance.minInventory)+", a = "+(a+1)+", stage = "+(t+2));
						System.out.println("scenarioProb = "+scenarioProb);
						System.out.println("cost with action " +(a+1) +" = "+totalCost[i][a][t]);
						}
					OptimalAction[i][a][t] = getOptimalAction(CostNoAction[i][t+1], totalCost[i][a][t]);
				}
			}
		}
		

		/** This array stores the reorder points, 
		 * for a single value of quantity at every stage except the first, 
		 * where the quantity is used for all stages with actions.**/
		int[][] reorderPoint = new int [instance.maxQuantity][instance.getStages()-1];
		reorderPoint = getReorderPoints(instance, inventory, OptimalAction);

		return new sQsolution(totalCost, CostNoAction, inventory, reorderPoint);
	}

	public static void main(String[] args) {

		/*** Problem instance ***/
		double fixedOrderingCost = 100;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 10;
		int[] demandMean = {20,40,60,40};


		/** SDP boundary conditions **/
		double tail = 0.00000001;

		int minInventory = -250;
		int maxInventory = 250;
		int maxQuantity = 1000;

		Instance instance = new Instance(
				fixedOrderingCost,
				unitCost,
				holdingCost,
				penaltyCost,
				demandMean,
				tail,
				minInventory,
				maxInventory,
				maxQuantity
				);

		/** Solve the classic instance **/
		sQsolution sQsolution = solvesQInstance(instance);

		/** Plot ETC without action for all stages **/
		//for(int t=0;t<instance.getStages();t++) {
			//plotCostNoAction(instance, sQsolution, t);
		//}
		
		/** problematic instance plotting**/
		//action = 50, stage = 1 - multiple intersections
		//plotComparedCosts(instance, sQsolution, 50, 0,true);
		//plotComparedCosts(instance, sQsolution, 50, 0, false);
		
		//action = 38, stage = 2 - multiple intersections at inventory level < 0
		//plotComparedCosts(instance, sQsolution, 38, 1, true);
		//plotComparedCosts(instance, sQsolution, 38, 1, false);
		
		//action = 100, stage = 3 - infinity total cost - plot and print
		//plotComparedCosts(instance, sQsolution, 100, 2, false);
		//for(int i=0;i<sQsolution.inventory.length;i++) {
			//System.out.println(sQsolution.totalCost[i][99][2]);
		//}
		
		//action = 1, stage = 3 - no intersection
		//plotComparedCosts(instance, sQsolution, 1, 2, false);
		//for(int i=0;i<sQsolution.inventory.length;i++) {
			//System.out.println(sQsolution.totalCost[i][0][2] - sQsolution.CostNoAction[i][3]);
		//}
		

		//printReorderPoints(instance, sQsolution);

	}








}
