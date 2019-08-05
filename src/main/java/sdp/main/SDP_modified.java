package sdp.main;
import org.apache.commons.math3.distribution.PoissonDistribution;

public class SDP_modified {

	   /**
	    * This method computes the probability of a given demand value.
	    * 
	    * A Poisson distribution is generated according to the given mean of demand.
	    * The demand probability takes the value of Pr(x = demand) in this Poisson distribution.
	    * 
	    * The probability value is discarded if it is smaller than a given truncation quantile.
	    * 
	    * **/
	   static double[][] computeDemandProbability(int[] demandMean, int maxDemand, double tail) {
		   double[][] demandProbability = new double [maxDemand+1][demandMean.length];
		   for(int t=0; t<demandMean.length;t++) {
			   PoissonDistribution dist = new PoissonDistribution(demandMean[t]);
			   for(int i=0;i<=maxDemand;i++) {
				   demandProbability [i][t] = dist.probability(i);
				   if(demandProbability [i][t]<tail) {
					   demandProbability[i][t] = 0;
				   }
			   }
		   }
		   return demandProbability;
	   }
	   
	   /** compute immediate cost **/
	   
	   /**
	    * This method computes the immediate cost for periods except the first. 
	    * 
	    * For an action with non-zero quantity, the immediate cost includes the ordering cost, holding cost and penalty cost; 
	    * otherwise, the immediate cost only incurs on the holding and penalty cost.
	    * 
	    * The immediate cost will be multiplied by the (transition) demand probability to obtain a expected cost.
	    * **/
	   static double computeImmediateCost(int [] inventory, int i, int Actions, int demand,
			   										double holdingCost, double penaltyCost, double fixedOrderingCost, double unitCost) {
		   if(Actions == 0) {
			   return holdingCost*Math.max(0, inventory[i] + Actions - demand) + penaltyCost *Math.max(0, demand - inventory[i] - Actions);
		   }else {
			   return fixedOrderingCost + unitCost * Actions 
					   + holdingCost*Math.max(0, inventory[i] + Actions - demand) + penaltyCost *Math.max(0, demand - inventory[i] - Actions);
		   }
	   }
	   

	   /**
	    * This method computes the immediate cost for the first time period.
	    * 
	    * We do not take any action in the first period, i.e., there is no order placed in the first period.
	    * 
	    * Therefore, the immediate cost of the first period consists of the holding and penalty cost.
	    *  
	    * **/
	   static double computeImmediateCostFirstPeriod(int[] inventory, int i, int demand,
			   										 double holdingCost, double penaltyCost) {
		   return holdingCost*Math.max(0, inventory[i] - demand) + penaltyCost*Math.max(0, demand - inventory[i]);
				   
	   }
	   
	   /**
	    * This method selects the minimum value of a double array, 
	    * which is used for obtaining the optimal expected cost among costs of all feasible actions for each inventory level.
	    * **/
	   static double getOptimalCost(double[] arr) {
			double min = arr[0];
			for(int i=1;i<arr.length;i++) {
				if(arr[i]<min) {
					min = arr[i];
				}
			}
			return min;
		}
	   


		public static void main(String[] args) {
		   
		   /*** Problem instance ***/
		   double fixedOrderingCost = 100;
		   double unitCost = 0;
		   double holdingCost = 1;
		   double penaltyCost = 10;
		   int[] demandMean = {20,40,60,40};
		   
		   
		   /** SDP boundary conditions **/
		   double tail = 0.0001;
		   
		   int maxDemand = 250;
		   int maxInventory = 250;
		   int maxQuantity = 250;


		   /** How do you model stages? **/
		   int Stages = demandMean.length;
		   
		   
		   /** How do you model states? **/
		   // Choice about the indexing strategy for your arrays in the code. What does the first index represent? What does the second? And so on...
		   /**
		    * The state represents the inventory level at the beginning of a period.
		    * 
		    * Given the maximum demand, maximum inventory and the planning horizon, 
		    * the inventory level that all periods can possibly stand is ranged from [-planning Horizon * maximum Demand, maximum Inventory],
		    * and this set is considered as the state space.
		    * 
		    * We use an integer array, inventory[], to store all possible states.
		    * The index of inventory[] is a serial number, which will be used in the following computation to track back to the state. 
		    * **/
		   int[] inventory = new int [maxInventory + Stages*maxInventory+1];
		   for(int i=0;i<inventory.length;i++) {
			   inventory[i] = i+1 - (Stages*maxDemand+1);
		   }
		   
		   
		   /** Working arrays **/
		   
		   //double transitionProbabilities[][][][] = computeTransitionProbabilities(demandMean, maxInventory, maxQuantity, tail); // But do you really need this array?
		   

		   /** How do you model actions? **/
		   // Actions are a consequence of the stage, state in which you are. You need a "way" of recording or generating these actions. Could be a matrix or a function.
		   //int generateOptimalAction(int t, int inventoryLevel)
		   //int[] generateFeasibleActions(int t, int inventoryLevel)
		   //void computeExpectedTotalCost(int t, int inventoryLevel)
		   /**
		    * The action represents placing an order with quantity Q at the beginning of a period. 
		    * Q can take a value from 0 to a given maximum quantity.
		    * 
		    * For any state i, the value of feasible action a satisfies:
		    *                 -planning horizon * maximum demand <= i - demand + a <= maximum inventory.
		    * 
		    * By traversing all possible actions from 0 to the maximum, 
		    * the feasible actions can be generated by satisfying the condition above,
		    * and are directly substituted into the immediate cost.
		    * 
		    * This step is completed in the following computation.
		    * **/
		   
		   
		   
		   /** How do you model transition probabilities? **/
		   /**
		    * The transition probability represents the probability of that one state i transfers to a state j if action a is taken.
		    * It is generally denoted as P(i,j,a) = Pr(demand = i+a-j), which is related to the probability of demand.
		    * So we transform the transition probability to the demand probability in order to simplify the computation. 
		    * 
		    * Given the maximum demand, we compute the probability of each possible demand in each period, and store them in a 2-D array, demandProbabilities[][].
		    * The first index represents the possible demand value ranged from 0 to maxDemand.
		    * The second index represents the time period.
		    * 
		    * **/
		   double demandProbabilities [][] = computeDemandProbability(demandMean, maxDemand, tail);
		   
		   
		   
		   
		   /** How do you model immediate costs? **/
		   /**
		    * The immediate cost for each action includes a holding cost and a penalty cost.
		    * It is computed separately for the periods 1,...,N and the period 0, because we do not take any action in the period 0.
		    * 
		    * The computation is realized by two methods, computeImmediateCost and computeImmediateCostFirstPeriod.
		    * How these methods work is explained above.
		    * 
		    **/
		   
		   
		   
		   /** How do you model/compute expected total costs? **/
		   double totalCost[][] = new double [inventory.length][maxQuantity+1]; //Valuable expectedTotalCost for a period, to renew 
	      
		   double optimalCost[][] = new double [inventory.length][Stages]; //Valuable expectedTotalOptimalCost 
	      
		   //double expectedTotalOptimalAction[][] = new double [StateSpace][Stages]; //Valuable expectedTotalOptimalAction
		   
		   
		   double immediateCost;
		   
		   
		   /** Compute Expected Cost **/
		   
		   /** Last Period **/
		   for(int i=0;i<inventory.length; i++) {
			   for(int a=0;a<=maxQuantity;a++) {
				   
				   
				   for(int demand=0;demand<=maxDemand;demand++) {
					   
					   /** Select feasible actions according to the condition **/
					   if((inventory[i] + a - demand <= maxInventory) && (inventory[i] + a - demand >= -Stages*maxDemand)) {
						   
						   /** Computed backward, no cost for the previous period added**/
						   immediateCost = demandProbabilities[demand][Stages-1]
								   			*computeImmediateCost(inventory, i, a, demand,
																	  	  	holdingCost, penaltyCost, fixedOrderingCost, unitCost);
					   }else {
						   immediateCost = Double.POSITIVE_INFINITY;
					   }
					   totalCost[i][a] = totalCost[i][a] + immediateCost;
				   }
				   
	 
			   }
			   optimalCost[i][Stages-1] = getOptimalCost(totalCost[i]);
		   }
		   
		   
		   /** Period 3 and 2**/
		   for(int t = Stages-2; t>0; t--) {
			   for(int i=0;i<inventory.length; i++) {
				   for(int a=0;a<=maxQuantity;a++) {
					   
					   totalCost[i][a] = 0;
					   
					   for(int demand=0;demand<=maxDemand;demand++) {
						   
						   if((inventory[i] + a - demand <= maxInventory) && (inventory[i] + a - demand >= -Stages*maxDemand)) {
							   
							   immediateCost = demandProbabilities[demand][t]
									   			*computeImmediateCost(inventory,i, a, demand,
																	  holdingCost, penaltyCost, fixedOrderingCost, unitCost)
									   			+ demandProbabilities[demand][t+1]*optimalCost[i+a-demand][t+1];
						   }else {
							   immediateCost = Double.POSITIVE_INFINITY;
						   }
						   totalCost[i][a] = totalCost[i][a] + immediateCost;
					   }
					   
		 
				   }
				   optimalCost[i][t] = getOptimalCost(totalCost[i]);
			   }
		   }
		   
		   /** Period 1 **/
		   for(int i=0;i<inventory.length; i++) {
			   for(int a=0; a<=maxQuantity;a++) {
				   totalCost[i][a] = 0;
			   }
		   }
		   for(int i=Stages*maxDemand;i<inventory.length; i++) {
			   for(int demand=0;demand<=maxDemand;demand++) {
					immediateCost = demandProbabilities[demand][0]
								   		*computeImmediateCostFirstPeriod(inventory, i, demand,
		   										 holdingCost, penaltyCost)
								   		+ demandProbabilities[demand][0]*optimalCost[i-demand][1];
					for(int a=0; a<=maxQuantity;a++) {
						totalCost[i][a] = totalCost[i][a] + immediateCost;
					}
			   }
			   optimalCost[i][0] = getOptimalCost(totalCost[i]);
		   }

		   for(int i=Stages*maxDemand;i<inventory.length;i++) {
			   System.out.print((i-Stages*maxDemand)+" ");
			   for(int t=0;t<Stages;t++) {
				   System.out.print(optimalCost[i][t] + " ");
			   }System.out.println();
		   }


		   
		   
		   
	      /************* DO NOT USE ****************************************/
			/*
			SDP sdp = new SDP();
			int stateSpace = sdp.demand.getInventory().length;
			
			
			DecimalFormat df = new DecimalFormat("#.000000"); 
			
			//last stage
			ICostWithoutHistory stageFour = new CostWithoutHistory();
			double f [] = stageFour.calCostWithoutHistory(sdp.demand);
			
			
			//stage 3 and 2
			
			ICostWithHistory stageThree = new CostWithHistory();
			double answer [][] = new double [2][stateSpace];
			for(int t = Data.stage-2; t>0; t--) {
				answer[t-1] = stageThree.calCostWithHistory(sdp.demand, f, t);
				f = answer [t-1];
			}
			/*
			//for test
			ICostWithHistory stageMediem = new CostWithHistory();
			double answer [][] = new double [1][stateSpace];
			int t = 1; //period 2
			answer[t] = stageMediem.calCostWithHistory(sdp.demand, f, t+1);
			f = answer[t];
			
			
			
			//first stage
			ICostOriginalInventory stageOne = new CostOriginalInventory();
			f = stageOne.calCostOriginalInventory(sdp.demand, f);

			for(int i=0;i<f.length;i++) {
				System.out.println(f[i]);
			}
			
			//try to plot the total cost
			
		    XYSeries series = new XYSeries("SDP Plot");
		    for(int i=Data.stage*Data.maxDemand;i<Data.stage*Data.maxDemand+200;i++) {
		    	series.add(i-Data.stage*Data.maxDemand,f[i]);
		    }
		    XYDataset xyDataset = new XYSeriesCollection(series);
		    JFreeChart chart = ChartFactory.createXYLineChart("SDP Model - "+Data.stage+" period expected total cost", "Opening inventory level", "Expected total cost",
		          xyDataset, PlotOrientation.VERTICAL, false, true, false);
		    ChartFrame frame = new ChartFrame("SDP Plot",chart);
		    frame.setVisible(true);
		    frame.setSize(500,400);
		    
		    
		    //print the cost values
			for(int i=Data.stage*Data.maxDemand; i<Data.stage*Data.maxDemand+201; i++) {
				System.out.println((i-Data.stage*Data.maxDemand)+" " +df.format(f[i]));
			}
			*/

		}


}
