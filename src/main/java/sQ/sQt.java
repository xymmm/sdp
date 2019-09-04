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

public class sQt {

	/***** Output methods *****/
	
	/** print reorder points s **/
	static void prints(Instance instance, sQtsolution sQtsolution) {
		System.out.println();
		System.out.println("Reorder points: ");
		for(int t=0; t<instance.getStages(); t++) {
			System.out.println(sQtsolution.getssQt(instance, sQtsolution)[t]);
		}
	}
	
	/** print Qt **/
	static void printQt(Instance instance, sQtsolution sQtsolution) {
		System.out.println();
		System.out.println("Order quantities:");
		for(int t=0; t<instance.getStages(); t++) {
			System.out.println(sQtsolution.getQt(instance)[t]);
		}
	}	
	
	/** print optimal costs **/
	static void printOptimalCost(int inventoryIndex, Instance instance, sQtsolution sQtsolution) {
		System.out.println("Optimal Cost: ");
		System.out.println(sQtsolution.totalCost[0][instance.initialInventory - instance.minInventory][sQtsolution.getQt(instance)[0]]);
	}
	
	/** print optimal costs **/
	static void printOptimalAction(int inventoryIndex, Instance instance, sQtsolution sQtsolution) {
	}
	
	/** plot costs**/
	static void plotCost(Instance instance, sQtsolution sQtsolution, int t) {
		XYSeries series = new XYSeries("No action");
		for(int a=0;a<instance.maxQuantity+1;a++) {
			series.add(a,sQtsolution.totalCost[t][instance.initialInventory - instance.minInventory][a]);
		}
		XYSeriesCollection collection = new XYSeriesCollection();
		collection.addSeries(series);
		JFreeChart chart = ChartFactory.createXYLineChart("Expected Total Cost for Period "+ (t+1), "Feasible Replenishment Quantity", "Expected total cost",
				collection, PlotOrientation.VERTICAL, true, true, false);
		ChartFrame frame = new ChartFrame("Period "+(t+1),chart);
		frame.setVisible(true);
		frame.setSize(1500,1200);
	}
	
	/***** main computation *****/
	public static sQtsolution solvesQt(Instance instance) {
		
		//working matrix
		int[] inventory = new int [instance.maxInventory - instance.minInventory + 1];
		for(int i=0;i<inventory.length;i++) {
			inventory[i] = i + instance.minInventory;
		}
		double demandProbabilities [][] = SDP.computeDemandProbability(instance.demandMean, instance.maxDemand, instance.tail);
		
		//different dimension index from sdp or sQ, as period takes the first.
		double totalCost[][][] = new double[instance.getStages()][inventory.length][instance.maxQuantity+1];
		boolean optimalAction[][][] = new boolean [instance.getStages()][inventory.length][instance.maxQuantity+1];

		for(int t=instance.getStages()-1;t>=0;t--) {//periods
			for(int a=0; a<instance.maxQuantity+1;a++) {//a
				for(int i=0; i<inventory.length;i++) {//inventory
					/** a>0 **/
					double totalCostOrder = SDP.computePurchasingCost(a, instance.fixedOrderingCost, instance.unitCost);
					double scenarioProb = 0;
					for(int d=0; d<demandProbabilities[t].length;d++) {
						if((inventory[i] + a - d <= instance.maxInventory) && (inventory[i] + a - d >= instance.minInventory)) {
							totalCostOrder += demandProbabilities[t][d] * (
									SDP.computeImmediateCost(inventory[i], 
															 a, 
															 d, 
															 instance.holdingCost, 
															 instance.penaltyCost, 
															 instance.fixedOrderingCost, 
															 instance.unitCost)
									+ ((t==instance.getStages()-1) ? 0 : totalCost[t+1][i+a-d][a]) 
									);
							scenarioProb += demandProbabilities[t][d];
						}
					}
					totalCostOrder /= scenarioProb;
					
					/** a=0 **/
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
													+ ((t==instance.getStages()-1) ? 0 : totalCost[t+1][i-d][a]) 
													);
							scenarioProb += demandProbabilities[t][d];
						}								
					}
					totalCostNoOrder /= scenarioProb;
					//System.out.println(totalCostNoOrder + " "+ totalCostOrder);
					totalCost[t][i][a] = Math.min(totalCostNoOrder, totalCostOrder);
					optimalAction[t][i][a] = (totalCostNoOrder < totalCostOrder) ? false:true;
				}
			}
		}
		
		return new sQtsolution(totalCost, optimalAction, inventory);
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
		sQtsolution sQtsolution = solvesQt(instance);

		printOptimalCost(instance.initialInventory-instance.minInventory, instance, sQtsolution);

		printQt(instance, sQtsolution);
		prints(instance, sQtsolution);
		
		/** simulations **/
		System.out.println();
		System.out.println("Simulations:");
		int[] reorderPoint = {13, 27,51, 27};//sQtsolution.getssQt(instance, sQtsolution);
		int[] actionQuantity = {83, 147, 109, 49};//sQtsolution.getQt(instance);
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
		
		System.out.println();
		System.out.println("Total CPU time: "+timer.format());

		}//main
	
}//class
