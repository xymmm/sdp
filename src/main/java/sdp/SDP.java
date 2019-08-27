package sdp;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import sdp.data.Instance;
import sdp.util.Demand;

public class SDP {

	/** Static methods **/

	/** compute demand probability
	 * 
	 * This method computes the probability of a given demand value.
	 * A Poisson distribution is generated according to the given mean of demand.
	 * The demand probability takes the value of Pr(x = demand) in this Poisson distribution.
	 * The probability value is discarded if it is smaller than a given truncation quantile.
	 * 
	 * **/
	public static double[][] computeDemandProbability(int[] demandMean, int maxDemand, double tail) {
		double[][] demandProbability = new double [demandMean.length][maxDemand+1];
		for(int t=0; t<demandMean.length;t++) {
			demandProbability[t] = Demand.tabulateProbability(demandMean[t], tail);
		}
		return demandProbability;
	}

	/** compute immediate cost with actions **/
	public static double computeImmediateCost(
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
	public static double computePurchasingCost(int a, double fixedOrderingCost, double unitCost) {
		return a > 0 ? fixedOrderingCost + a*unitCost : 0;
	}


	/** get the optimal cost **/
	static double getOptimalCost(double[] expectedTotalCosts) {
		double min = expectedTotalCosts[0];
		for(int a=1;a<expectedTotalCosts.length;a++) {
			if(expectedTotalCosts[a]<min) {
				min = expectedTotalCosts[a];
			}
		}
		return min;
	}

	/** get the optimal actions **/
	static double getOptimalAction(double[] expectedTotalCosts) {
		double min = expectedTotalCosts[0];
		double action = 0;
		for(int a=1;a<expectedTotalCosts.length;a++) {
			if(expectedTotalCosts[a]<min) {
				min = expectedTotalCosts[a];
				action = a;
			}
		}
		return action;
	}
	
	/** print the optimal costs **/
	static void printOptimalCost(Instance instance, sdpSolution solution) {
		for(int i=0;i<solution.inventory.length;i++) {
			System.out.print((i+instance.minInventory)+" ");
			for(int t=0;t<instance.getStages();t++) {
				System.out.print(solution.optimalCost[i][t] + " ");
			}System.out.println();
		}
		System.out.println();
	}

	/** print the optimal actions **/
	static void printOptimalActions(Instance instance, sdpSolution solution) {
		for(int i=0;i<solution.inventory.length;i++) {
			System.out.print((i+instance.minInventory)+" ");
			for(int t=0;t<instance.getStages();t++) {
				System.out.print(solution.optimalAction[i][t] + " ");
			}System.out.println();
		}
		System.out.println();
	}

	/** Plot the expected optimal cost **/
	static void plotOptimalCost(Instance instance, sdpSolution solution) {
		XYSeries series = new XYSeries("SDP Plot");
		for(int i=0-instance.minInventory;i<solution.inventory.length;i++) {
			series.add(i+instance.minInventory,solution.optimalCost[i][0]);
		}
		XYDataset xyDataset = new XYSeriesCollection(series);
		JFreeChart chart = ChartFactory.createXYLineChart("SDP Model", "Opening inventory level", "Expected total cost",
				xyDataset, PlotOrientation.VERTICAL, false, true, false);
		ChartFrame frame = new ChartFrame("SDP Plot",chart);
		frame.setVisible(true);
		frame.setSize(1500,1200);
	}

	/** compute the expected total cost and get optimal actions **/
	public static sdpSolution solveInstance(Instance instance, boolean initialOrder) {
		/** model stages? **/
		int Stages = instance.getStages();

		/** 
		 * The state represents the inventory level at the beginning of a period.
		 * 
		 * Given the maximum demand, maximum inventory and the planning horizon, 
		 * the inventory level that all periods can possibly stand is ranged from [-planning Horizon * maximum Demand, maximum Inventory].
		 * 
		 * We use an integer array, inventory[], to store all possible states.
		 * The index of inventory[] is a serial number, which will be used in the following computation to track back to the state. 
		 * **/
		int[] inventory = new int [instance.maxInventory - instance.minInventory + 1];
		for(int i=0;i<inventory.length;i++) {
			inventory[i] = i + instance.minInventory;
		}

		/** 
		 * The action represents placing an order with quantity Q at the beginning of a period. 
		 * Q can take a value from 0 to a given maximum quantity.
		 * For any state i, the value of feasible action a satisfies:
		 *                 -planning horizon * maximum demand <= i - demand + a <= maximum inventory..
		 * **/
		double optimalAction[][] = new double [inventory.length][Stages];

		/**
		 * The transition probability represents the probability of that one state i transfers to a state j if action a is taken.
		 * It is generally denoted as P(i,j,a) = Pr(demand = i+a-j), which is related to the probability of demand.
		 * So we transform the transition probability to the demand probability in order to simplify the computation. 
		 * 
		 * The probability of each possible demand in each period is computed and stored in a 2D array, demandProbabilities[][].
		 * The first index represents the possible demand value ranged from 0 to maxDemand, and the second index represents the time period.
		 * **/
		double demandProbabilities [][] = computeDemandProbability(instance.demandMean, instance.maxDemand, instance.tail);

		/** Compute ETC 
		 *
		 * A 2D array, totalCost[][], is created to store the expected total cost for a period with feasible actions.
		 * The first index represents the possible states, and the second index represents the possible actions.
		 * This array is locally applied by each period.
		 * 
		 * For a state, we go through all possible actions; 
		 * 	  we first initialize the purchasing cost for each action according to the order quantity;
		 *    secondly, for a pair of state and action, we judge if the action is feasible by substituting the demand;
		 *    	if it is feasible, we multiply the demand probability to the immediate cost to obtain expected cost cumulatively,
		 *    	and normalize the expected cost based on the scenarios.
		 *    
		 * A 2D array, optimalCost[][], is created to store the optimal cost.
		 * The first index represents the possible states, and the second index represents the stages.
		 * Each entry in the array optimalCost[state][stage] is obtained by selecting the minimum value of array totalCost[state] within each period.
		 * **/
		double totalCost[][] = null;
		double optimalCost[][] = new double [inventory.length][Stages]; 

		for(int t=Stages-1;t>=0;t--) { // Time
			totalCost = new double [inventory.length][(t==0)?( (initialOrder) ? instance.maxQuantity+1 : 0) : instance.maxQuantity+1 ];
			for(int i=0;i<inventory.length;i++) { // Inventory
				for(int a = 0; a <= ( (t==0)?( (initialOrder) ? instance.maxQuantity : 0) : instance.maxQuantity ); a++) { //Actions
					double scenarioProb = 0;
					totalCost[i][a] = computePurchasingCost(a, 
															instance.fixedOrderingCost, 
															instance.unitCost);
					for(int d=0;d<demandProbabilities[t].length;d++) { // Demand
						if((inventory[i] + a - d <= instance.maxInventory) && (inventory[i] + a - d >= instance.minInventory)) {
							totalCost[i][a] += demandProbabilities[t][d]*(
											computeImmediateCost(inventory[i], 
																 a, 
																 d, 
																 instance.holdingCost, 
																 instance.penaltyCost, 
																 instance.fixedOrderingCost, 
																 instance.unitCost)
											+ ((t==Stages-1) ? 0 : optimalCost[i+a-d][t+1]) 
											);
							scenarioProb += demandProbabilities[t][d];
						}//else, we do nothing.
					}
					totalCost[i][a] = totalCost[i][a]/scenarioProb;
				}
				optimalCost[i][t] = getOptimalCost(totalCost[i]);
				optimalAction[i][t] = getOptimalAction(totalCost[i]);
			}
		}

		return new sdpSolution(optimalAction, optimalCost, inventory);
	}

	public static void main(String[] args) {

		double fixedOrderingCost = 100;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 10;
		int[] demandMean = {20,40,60,40};

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

		sdpSolution solution = solveInstance(instance, true);

		printOptimalCost(instance, solution);
		plotOptimalCost(instance, solution);
		printOptimalActions(instance, solution);
	}

}
