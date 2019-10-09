package sQ.sdp;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import sQ.simulation.sQsimPoisson;
import sQ.simulation.sQsimInstanceInt;
import sS.sdp.sS;
import sdp.data.Instance;
import umontreal.ssj.util.Chrono;

public class sQ {

	/** print reorder points **/
	public static void printReorderPoints(Instance instance, sQsolution sQsolution) {
		System.out.println("reorder points (s) under (s,Q) policy: ");
		for(int t = 0;t<instance.getStages();t++){
			System.out.print(sQsolution.getsSQ(instance, sQsolution)[t]+" ");
		}System.out.println();
	}
	
	/** print optimal quantity **/
	public static void printOptimalQuantity(Instance instance, sQsolution sQsolution) {
		System.out.println("Optimal Q for all periods is: ");
		System.out.println(sQsolution.getOpt_aSQ(instance)+1);
	}
	
	/** print cost of optimal quantity **/
	public static void printOpitmalCost(Instance instance, sQsolution sQsolution){
		System.out.println("Optimal cost with initial inventory level " +(instance.initialInventory)+" is: ");
		System.out.println(sQsolution.totalCost[instance.initialInventory - instance.minInventory][sQsolution.getOpt_aSQ(instance)+1][0]);
	}

	/** Plot costs - cost with no action and with a given Q for a given stage**/
	static void plotComparedCosts(Instance instance, sQsolution sQsolution, int t) {
		XYSeries series = new XYSeries("sQ plot");
		for(int a=0;a<instance.maxQuantity;a++) {
			series.add(a,sQsolution.totalCost[instance.initialInventory - instance.minInventory][a][t]);
		}
		XYDataset xyDataset = new XYSeriesCollection(series);
		JFreeChart chart = ChartFactory.createXYLineChart("SDP with (s,Q) policy", "Feasible Replenishment Quantity", "Expected total cost",
				xyDataset, PlotOrientation.VERTICAL, true, true, false);
		ChartFrame frame = new ChartFrame("sQ plot",chart);
		frame.setVisible(true);
		frame.setSize(1500,1200);
	}
	
	public static void presentsQresults(Instance instance, sQsolution sQsolution) {
		plotComparedCosts(instance, sQsolution, 0);
		//printReorderPoints(instance, sQsolution);
		//System.out.println();
		printOptimalQuantity(instance, sQsolution);
		System.out.println();
		printOpitmalCost(instance, sQsolution);
	}

	/** main computation **/
	public static sQsolution solvesQInstance(Instance instance, boolean initialOrder) {

		int[] inventory = new int [instance.maxInventory - instance.minInventory + 1];
		for(int i=0;i<inventory.length;i++) {
			inventory[i] = i + instance.minInventory;
		}
		
		//demandProbabilities[stages][demandValue] = Prob(dt = demandValue), dt is the realized demand of the random varuable d_t
		//double demandProbabilities [][] = sS.computeDemandProbability(instance.demandMean, instance.maxDemand, instance.tail);//Poisson
		double demandProbabilities[][] = sS.computeNormalDemandProbability(instance.demandMean, instance.stdParameter, instance.maxDemand, instance.tail);
		
		double totalCost[][][] = new double[inventory.length][instance.maxQuantity+1][instance.getStages()];
		boolean optimalAction[][][] = new boolean [inventory.length][instance.maxQuantity + 1][instance.getStages()];
		
		int Q;
		for(int a=0; a<instance.maxQuantity+1;a++) { //"a" represents the action index, so the actual action volume is a+1
			for(int t=instance.getStages()-1;t>=0;t--) { // Time			   
				for(int i=0;i<inventory.length;i++) { // Inventory   
					/** a > 0 **/
					Q = ((t==0)&&(!initialOrder))? 0: a;
					double totalCostOrder = sS.computePurchasingCost(a, instance.fixedOrderingCost, instance.unitCost); 
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
									+ ((t==instance.getStages()-1) ? 0 : totalCost[i+Q-d][Q][t+1]) 
									);
							scenarioProb += demandProbabilities[t][d];
						}
					}
					totalCostOrder /= scenarioProb;
					//if((a==0)&&(i==-instance.minInventory)) System.out.println("t="+t+" cost order = "+totalCostOrder);//check if a=0 == costNoOrder

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
									+ ((t==instance.getStages()-1) ? 0 : totalCost[i-d][a][t+1]) /** changed from totalCost[i-d][0][t+1] **/
									);
							scenarioProb += demandProbabilities[t][d];
						}
					}
					totalCostNoOrder /= scenarioProb;
					//if((a==0)&&(i==-instance.minInventory)) System.out.println("t="+t+" cost no order = "+totalCostNoOrder);//check if a=0 == costNoOrder
					
					totalCost[i][a][t] = Math.min(totalCostNoOrder, totalCostOrder);
					optimalAction[i][a][t] = totalCostNoOrder < totalCostOrder ? false : true;
				}
			}
		}
		return new sQsolution(totalCost, optimalAction, inventory);
	}

	public static void main(String[] args) {

		Chrono timer = new Chrono();

		double fixedOrderingCost = 100;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 10;

		double tail = 0.00000001;

		int minInventory = -500;
		int maxInventory = 500;
		int maxQuantity = 500;
		
		double stdParameter = 0.25;
		
		//instance classic
		int[] demandMean = {20,40,60,40};
		
		//instance 5
		//int[] demandMean = {50,30,60,20,40,50};

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
		sQsolution sQsolution = solvesQInstance(instance,true);
		
		/*
		boolean optActPeriod0[][] = new boolean[instance.maxInventory - instance.minInventory + 1][instance.maxQuantity + 1];
		for(int i = 0; i < optActPeriod0.length; i++) {
			for(int a = 0; a < optActPeriod0[i].length; a++) {
				optActPeriod0[i][a] = sQsolution.optimalAction[i][a][0];
			}	
		}
		System.out.println(Arrays.deepToString(optActPeriod0)); //Careful, BIG MATRIX
		 */

		presentsQresults(instance, sQsolution);
		
		System.out.println();
		for(int t=0; t<instance.getStages();t++) {
			System.out.println("a: " + (sQsolution.getOpt_aSQ(instance)+1) + "\t"
								+ "t: "+ (t+1)+ "\t"  
								+sQsolution.totalCost[instance.initialInventory - instance.minInventory][sQsolution.getOpt_aSQ(instance)+1][t]);
		}
		
		/*
		for(int i=0; i<sQsolution.inventory.length;i++) {
			System.out.println("i: "+ (i+instance.minInventory) + "\t" + sQsolution.totalCost[i][sQsolution.getOpt_aSQ(instance)+1][0]);
		}
		*/
		

	}


}
