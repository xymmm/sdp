package sQ;

import java.util.Arrays;

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
			int a, 
			int demand,
			double holdingCost, 
			double penaltyCost, 
			double fixedOrderingCost, 
			double unitCost) {

		return holdingCost*Math.max(0, inventoryLevel + a - demand) + penaltyCost *Math.max(0, demand - inventoryLevel - a);

	}

	/** compute purchasing cost according to order quantity (action) **/
	static double computePurchasingCost(int a, double fixedOrderingCost, double unitCost) {
		return a > 0 ? fixedOrderingCost + a*unitCost : 0;
	}

	/** get reorder points **/
	public static int[][] getReorderPoints(Instance instance, int[] inventory, boolean[][][]optimalAction) {
		// TODO
	   return null;
	}


	/** print reorder points**/
	public static void printReorderPoints(Instance instance, sQsolution sQsolution) {
	   for(int t = 0;t<instance.getStages();t++) {
         System.out.print(sQsolution.s[t]+" ");
      }System.out.println();
	}
	
	/** Plot costs - cost with no action and with a given Q for a given stage**/
	public static void plotComparedCosts(Instance instance, sQsolution sQsolution, int t) {
	      
	      XYSeries series1 = new XYSeries("No action");
	      for(int a=0;a<instance.maxQuantity;a++) {
	    	  series1.add(a,sQsolution.totalCost[instance.initialInventory - instance.minInventory][a][t]);
	      }
	      /*XYSeries series2 = new XYSeries("Action of "+Q);
	      for(int i=(seeAll?0:-instance.minInventory);i<sQsolution.inventory.length;i++) {
	    	  series2.add(i+instance.minInventory,sQsolution.totalCost[i][sQsolution.opt_a][stageIndex]);
	      }*/
		  XYSeriesCollection collection = new XYSeriesCollection();
		  collection.addSeries(series1);
		  //collection.addSeries(series2);
	      JFreeChart chart = ChartFactory.createXYLineChart("Expected Total Cost for Period "+ (t+1), "Opening inventory level", "Expected total cost",
	            collection, PlotOrientation.VERTICAL, true, true, false);
	      ChartFrame frame = new ChartFrame("Period "+(t+1),chart);
	      frame.setVisible(true);
	      frame.setSize(1500,1200);
	}

	/** main computation **/
	public static sQsolution solvesQInstance(Instance instance) {

		int[] inventory = new int [instance.maxInventory - instance.minInventory + 1];
		for(int i=0;i<inventory.length;i++) {
			inventory[i] = i + instance.minInventory;
		}
		double demandProbabilities [][] = computeDemandProbability(instance.demandMean, instance.maxDemand, instance.tail);

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
		double totalCost[][][] = new double[inventory.length][instance.maxQuantity+1][instance.getStages()];
		
		/** This array stores the comparison between cost with no action and cost with action Q, for period 2,3 and 4**/
		boolean optimalAction[][][] = new boolean [inventory.length][instance.maxQuantity + 1][instance.getStages()];

		/** Cost Computation a = Q, single Q for all periods **/
		for(int a=0; a<instance.maxQuantity+1;a++) { //"a" represents the action index, so the actual action volume is a+1
			for(int t=instance.getStages()-1;t>=0;t--) { // Time			   
			   for(int i=0;i<inventory.length;i++) { // Inventory   
			      
			      double totalCostOrder = computePurchasingCost(a, instance.fixedOrderingCost, instance.unitCost);
					
					double scenarioProb = 0;

					for(int d=0;d<demandProbabilities[t].length;d++) { // Demand
						if((inventory[i] + a - d <= instance.maxInventory) && (inventory[i] + a - d >= instance.minInventory)) {//"a+1" as action volumn
						   totalCostOrder += demandProbabilities[t][d]*(
									computeImmediateCost(inventory[i], a, d, instance.holdingCost, instance.penaltyCost, instance.fixedOrderingCost, instance.unitCost)
									+ ((t==instance.getStages()-1) ? 0 : totalCost[i+a-d][a][t+1]) );//computation on the index, so (a+1) is not used.
							scenarioProb += demandProbabilities[t][d];
						}
					}
					totalCostOrder /= scenarioProb;
					
					
					double totalCostNoOrder = 0;
					scenarioProb = 0;
					for(int d=0;d<demandProbabilities[t].length;d++) { // Demand
                  if((inventory[i] - d <= instance.maxInventory) && (inventory[i] - d >= instance.minInventory)) {//"a+1" as action volumn
                     totalCostNoOrder += demandProbabilities[t][d]*(
                           computeImmediateCost(inventory[i], 0, d, instance.holdingCost, instance.penaltyCost, instance.fixedOrderingCost, instance.unitCost)
                           + ((t==instance.getStages()-1) ? 0 : totalCost[i-d][a][t+1]) );//computation on the index, so (a+1) is not used.
                     scenarioProb += demandProbabilities[t][d];
                  }
               }
               totalCostNoOrder /= scenarioProb;
					
					totalCost[i][a][t] = Math.min(totalCostNoOrder, totalCostOrder);

					optimalAction[i][a][t] = totalCostNoOrder < totalCostOrder ? false : true;
				}
			}
		}
		
		// Determine the optimal a. What is the optimal a?
		
   	int a = 1;
      int minIndex = a;
      double minCost = totalCost[instance.initialInventory - instance.minInventory][minIndex][0]; //Time zero
      do {
         if(minCost > totalCost[instance.initialInventory - instance.minInventory][++a][0]) {
            minCost = totalCost[instance.initialInventory - instance.minInventory][++a][0];
            minIndex = a - 1;
         }
      }while(a < instance.maxQuantity - 1);
      int opt_a = minIndex;
		
      int[] s = new int[instance.getStages()];
		// Get the reorder points.
      for(int t=0;t<instance.getStages();t++) { // Time
         for(int i=0;i<inventory.length;i++) {  // Inventory   
            if(optimalAction[i][opt_a][t] == false) {
               s[t] = i + instance.minInventory;
               break;
            }
         }
      }
      
		// Build a solution.

		return new sQsolution(totalCost, optimalAction, inventory, s, opt_a);
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

		int minInventory = -500;
		int maxInventory = 500;
		int maxQuantity = 500;

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
		
		System.out.println("Optimal solution cost: "+sQsolution.totalCost[instance.initialInventory - instance.minInventory][sQsolution.opt_a][0]);
		System.out.println("Optimal Q: "+(sQsolution.opt_a+1));
		
		boolean optActPeriod0[][] = new boolean[instance.maxInventory - instance.minInventory + 1][instance.maxQuantity + 1];
		for(int i = 0; i < optActPeriod0.length; i++) {
		   for(int a = 0; a < optActPeriod0[i].length; a++) {
		      optActPeriod0[i][a] = sQsolution.optimalAction[i][a][0];
		   }
		}
		
		//System.out.println(Arrays.deepToString(optActPeriod0)); //Careful, BIG MATRIX
		
		/** Plot ETC without action for all stages **/
		//for(int t=0;t<instance.getStages();t++) {
			//plotCostNoAction(instance, sQsolution, t);
		//}
		
		plotComparedCosts(instance, sQsolution, 0);
		
		printReorderPoints(instance, sQsolution);

	}








}
