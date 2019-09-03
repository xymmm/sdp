package sQ;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import sQsimulation.sQsim;
import sQsimulation.sQsimInstance;
import sdp.SDP;
import sdp.data.Instance;
import umontreal.ssj.util.Chrono;

public class sQ {

	/** print reorder points **/
	public static void printReorderPoints(Instance instance, sQsolution sQsolution) {
		for(int t = 0;t<instance.getStages();t++){
			System.out.print(sQsolution.getsSQ(instance, sQsolution)[t]+" ");
		}System.out.println();
	}
	
	/** print optimal quantity **/
	public static void printOptimalQuantity(Instance instance, sQsolution sQsolution) {
		System.out.println("Optimal Q: "+(sQsolution.getOpt_aSQ(instance)+1));
	}
	
	/** print cost of optimal quantity **/
	public static void printOpitmalCost(Instance instance, sQsolution sQsolution){
		System.out.println("Optimal solution cost: "+sQsolution.totalCost[instance.initialInventory-instance.minInventory][sQsolution.getOpt_aSQ(instance)+1][0]);
	}

	/** Plot costs - cost with no action and with a given Q for a given stage**/
	static void plotComparedCosts(Instance instance, sQsolution sQsolution, int t) {
		XYSeries series1 = new XYSeries("No action");
		for(int a=0;a<instance.maxQuantity;a++) {
			series1.add(a,sQsolution.totalCost[instance.initialInventory - instance.minInventory][a][t]);
		}
		XYSeriesCollection collection = new XYSeriesCollection();
		collection.addSeries(series1);
		JFreeChart chart = ChartFactory.createXYLineChart("Expected Total Cost for Period "+ (t+1), "Feasible Replenishment Quantity", "Expected total cost",
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
		double demandProbabilities [][] = SDP.computeDemandProbability(instance.demandMean, instance.maxDemand, instance.tail);
		double totalCost[][][] = new double[inventory.length][instance.maxQuantity+1][instance.getStages()];
		boolean optimalAction[][][] = new boolean [inventory.length][instance.maxQuantity + 1][instance.getStages()];

		/** Cost Computation a = Q, single Q for all periods **/
		for(int a=0; a<instance.maxQuantity+1;a++) { //"a" represents the action index, so the actual action volume is a+1
			for(int t=instance.getStages()-1;t>=0;t--) { // Time			   
				for(int i=0;i<inventory.length;i++) { // Inventory   
					/** a > 0 **/
					double totalCostOrder = SDP.computePurchasingCost(a, instance.fixedOrderingCost, instance.unitCost); 
					double scenarioProb = 0;
					for(int d=0;d<demandProbabilities[t].length;d++) { // Demand
						if((inventory[i] + a - d <= instance.maxInventory) && (inventory[i] + a - d >= instance.minInventory)) {
							totalCostOrder += demandProbabilities[t][d]*(
									SDP.computeImmediateCost(
											inventory[i], 
											a, 
											d, 
											instance.holdingCost, 
											instance.penaltyCost, 
											instance.fixedOrderingCost, 
											instance.unitCost)
									+ ((t==instance.getStages()-1) ? 0 : totalCost[i+a-d][a][t+1]) 
									);
							scenarioProb += demandProbabilities[t][d];
						}
					}
					totalCostOrder /= scenarioProb;

					/** a = 0**/
					double totalCostNoOrder = 0;
					scenarioProb = 0;
					for(int d=0;d<demandProbabilities[t].length;d++) { // Demand
						if((inventory[i] - d <= instance.maxInventory) && (inventory[i] - d >= instance.minInventory)) {
							totalCostNoOrder += demandProbabilities[t][d]*(
									SDP.computeImmediateCost(
											inventory[i], 
											0, 
											d, 
											instance.holdingCost, 
											instance.penaltyCost, 
											instance.fixedOrderingCost, 
											instance.unitCost)
									+ ((t==instance.getStages()-1) ? 0 : totalCost[i-d][a][t+1]) 
									);
							scenarioProb += demandProbabilities[t][d];
						}
					}
					totalCostNoOrder /= scenarioProb;
					totalCost[i][a][t] = Math.min(totalCostNoOrder, totalCostOrder);
					optimalAction[i][a][t] = totalCostNoOrder < totalCostOrder ? false : true;
				}
			}
		}
		return new sQsolution(totalCost, optimalAction, inventory);
	}

	public static void main(String[] args) {

		Chrono timer = new Chrono();

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
		
		/*
		boolean optActPeriod0[][] = new boolean[instance.maxInventory - instance.minInventory + 1][instance.maxQuantity + 1];
		for(int i = 0; i < optActPeriod0.length; i++) {
			for(int a = 0; a < optActPeriod0[i].length; a++) {
				optActPeriod0[i][a] = sQsolution.optimalAction[i][a][0];
			}	
		}
		System.out.println(Arrays.deepToString(optActPeriod0)); //Careful, BIG MATRIX
		 */

		plotComparedCosts(instance, sQsolution, 0);
		printOpitmalCost(instance, sQsolution);
		printOptimalQuantity(instance, sQsolution);
		printReorderPoints(instance, sQsolution);

		/** Simulations **/
		System.out.println();
		System.out.println("Simulations:");
		int[] reorderPoint = sQsolution.getsSQ(instance, sQsolution);
		int[] actionQuantity = {sQsolution.getOpt_aSQ(instance), 
				sQsolution.getOpt_aSQ(instance), 
				sQsolution.getOpt_aSQ(instance), 
				sQsolution.getOpt_aSQ(instance)};
		sQsimInstance sQsystem = new sQsimInstance(
				fixedOrderingCost,
				unitCost,
				holdingCost,
				penaltyCost,
				demandMean,
				tail,
				minInventory,
				maxInventory,
				actionQuantity,
				reorderPoint
				);

		int count = 50000;
		sQsim.simulationsQinstanceRuns(sQsystem, count);

		sQsystem.statCost.setConfidenceIntervalStudent();
		System.out.println(sQsystem.statCost.report(0.9, 3));
		System.out.println("Total CPU time: "+timer.format());

	}


}
