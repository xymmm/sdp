package sQ.sdp;

import java.util.Arrays;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import reorderQuantitySystem.sQsystemSolution;
import sS.sS;
import sdp.data.Instance;
import sdp.data.InstanceDouble;

public class sQ {

	/** Plot costs - cost with no action and with a given Q for a given stage**/
	static void plotComparedCosts(Instance instance, sQsolution sQsolution, int t) {
		XYSeries series = new XYSeries("sQ plot");
		for(int a=0;a<instance.maxQuantity;a++) {
			series.add(a,sQsolution.optimalCost);
		}
		XYDataset xyDataset = new XYSeriesCollection(series);
		JFreeChart chart = ChartFactory.createXYLineChart("SDP with (s,Q) policy", "Feasible Replenishment Quantity", "Expected total cost",
				xyDataset, PlotOrientation.VERTICAL, true, true, false);
		ChartFrame frame = new ChartFrame("sQ plot",chart);
		frame.setVisible(true);
		frame.setSize(1500,1200);
	}
	

	/** main computation **/
	public static sQsystemSolution solvesQInstance(InstanceDouble instance, boolean Normal) {

		int[] inventory = new int [instance.maxInventory - instance.minInventory + 1];
		for(int i=0;i<inventory.length;i++) {
			inventory[i] = i + instance.minInventory;
		}
		
		double[][] demandProbabilities = null;
		if(Normal) {
			demandProbabilities = sS.computeNormalDemandProbability(instance.demandMean, instance.stdParameter, instance.maxDemand, instance.tail);
		}else {
			demandProbabilities= sS.computeDemandProbability(instance.demandMean, instance.maxDemand, instance.tail);//Poisson
		}
		
		double totalCost[][][] = new double[instance.maxQuantity+1][inventory.length][instance.getStages()];
		boolean optimalAction[][][] = new boolean [instance.maxQuantity + 1][inventory.length][instance.getStages()];

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
					
					totalCost[a][i][t] = Math.min(totalCostNoOrder, totalCostOrder);
					optimalAction[a][i][t] = totalCostNoOrder < totalCostOrder ? false : true;
				}
			}
		}
		
		//get optimal Q
		int a = 0;
		int minIndex = a;
		double minCost = totalCost[minIndex][(int) (instance.initialInventory - instance.minInventory)][0]; //Time zero
		do {
			if(minCost > totalCost[a+1][(int) (instance.initialInventory - instance.minInventory)][0]) {
				minCost = totalCost[a+1][(int) (instance.initialInventory - instance.minInventory)][0];
				minIndex = a+1;
			}
			a = a + 1;
		}while(a < instance.maxQuantity - 1);
		int opt_a = minIndex+1;
		
		//get optimal schedule
		boolean[] optimalActionGivenQ = optimalAction[opt_a][(int) (instance.initialInventory - instance.minInventory)];
		double[] optimalActionDouble = new double[instance.demandMean.length];
		int[] schedule =  new int[instance.demandMean.length];
		for(int t=0; t<instance.demandMean.length; t++) {
			optimalActionDouble[t] = (optimalActionGivenQ[t] == true) ? 1 : 0;
			schedule[t] = (int) (optimalActionDouble[t] * opt_a);
		}
		
		double optimalCost = totalCost[opt_a][(int) (instance.initialInventory - instance.minInventory)][0];

		
		return new sQsystemSolution(schedule, optimalCost, demandProbabilities);
	}


	public static void main(String[] args) {

		double fixedOrderingCost = 100;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 10;

		double tail = 0.00000001;

		int minInventory = -500;
		int maxInventory = 500;
		int maxQuantity = 200;

		double stdParameter = 0.25;

		double[] demandMean = {20, 40, 60, 40};
		//double[] demandMean = {2,4,6,4};
		
		boolean Normal = true;
		
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

		/** Solve the classic instance **/
		sQsystemSolution sQsolution = solvesQInstance(instance, Normal);
			
		//present ETC of inventory 0 with the optimal quantity
		/*System.out.println();
		for(int t=0; t<instance.getStages();t++) {
			System.out.println("a: " + sQsolution.opt_a + "\t"
								+ "t: "+ (t+1)+ "\t"  
								+sQsolution.totalCost[sQsolution.opt_a][(int) (instance.initialInventory - instance.minInventory)][t]);
		}
	*/
		
		int[] reorderPoint = reorderQuantitySystem.reorderPoint.computeReorderPoint(instance, sQsolution);
		System.out.println("optimal cost = "+sQsolution.optimalCost);
		System.out.println("optimal schedule = " + Arrays.toString(sQsolution.optimalSchedule));
		System.out.println("reorder points = " + Arrays.toString(reorderPoint));
		



		

	}
	


}
