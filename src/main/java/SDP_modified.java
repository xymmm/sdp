import org.apache.commons.math3.distribution.PoissonDistribution;

public class SDP_modified {
	
	   /** compute transition probability via computing probability of that a demand could appear in this period**/
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
	   static double computeImmediateCostLastPeriod(int [] inventory, int i, int Actions, int demand,
			   										double holdingCost, double penaltyCost, double fixedOrderingCost, double unitCost) {
		   if(Actions == 0) {
			   return holdingCost*Math.max(0, inventory[i] + Actions - demand) + penaltyCost *Math.max(0, demand - inventory[i] - Actions);
		   }else {
			   return fixedOrderingCost + unitCost * Actions 
					   + holdingCost*Math.max(0, inventory[i] + Actions - demand) + penaltyCost *Math.max(0, demand - inventory[i] -Actions);
		   }
	   }
	   
	   static double computeImmediateCost(int[] inventory, int i, int Actions, int demand, double[][] ExpectedTotalOptimalCost,
										  double holdingCost, double penaltyCost, double fixedOrderingCost, double unitCost, int currentStageIndex) {
		   if(Actions == 0) {
			   return holdingCost*Math.max(0, inventory[i] + Actions - demand) + penaltyCost *Math.max(0, demand - inventory[i] -Actions)+
				   + ExpectedTotalOptimalCost[i+Actions-demand][currentStageIndex+1];
		   }else {
			   return fixedOrderingCost + unitCost * Actions 
					   + holdingCost*Math.max(0, inventory[i] + Actions - demand) + penaltyCost *Math.max(0, demand - inventory[i] -Actions)
					   + ExpectedTotalOptimalCost[i+Actions-demand][currentStageIndex+1];
		   }
	   }
	   
	   static double computeImmediateCostFirstPeriod(int[] inventory, int i, int demand, double [][] ExpectedTotalOptimalCost,
			   										 double holdingCost, double penaltyCost) {
		   return holdingCost*Math.max(0, inventory[i] - demand) + penaltyCost*Math.max(0, demand - inventory[i])
				   + ExpectedTotalOptimalCost[i-demand][1];
	   }
	   
	   
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
		   
		   
		   /** How do you model states? 
		    **/
		   // Choice about the indexing strategy for your arrays in the code. What does the first index represent? What does the second? And so on...
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
		   
		   
		   
		   /** How do you model transition probabilities? **/
		   double demandProbabilities [][] = computeDemandProbability(demandMean, maxDemand, tail);

		   
		   
		   
		   /** How do you model immediate costs? **/
		   
		   
		   
		   /** How do you model/compute expected total costs? **/
		   double totalCost[][] = new double [inventory.length][maxQuantity+1]; //Valuable expectedTotalCost for a period, to renew v
	      
		   double optimalCost[][] = new double [inventory.length][Stages]; //Valuable expectedTotalOptimalCost f
	      
		   //double expectedTotalOptimalAction[][] = new double [StateSpace][Stages]; //Valuable expectedTotalOptimalAction
		   
		   
		   double immediateCost;
		   
		   
		   /** Compute Expected Cost **/
		   
		   /** Last Period **/
		   for(int i=0;i<inventory.length; i++) {
			   for(int a=0;a<=maxQuantity;a++) {
				   
				   
				   for(int demand=0;demand<=maxDemand;demand++) {
					   
					   if((inventory[i] + a - demand <= maxInventory) && (inventory[i] + a - demand >= -Stages*maxDemand)) {
						   
						   immediateCost = demandProbabilities[demand][Stages-1]
								   			*computeImmediateCostLastPeriod(inventory, i, a, demand,
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
							   
							   immediateCost = demandProbabilities[demand][Stages-1]
									   			*computeImmediateCost(inventory,i, a, demand, optimalCost,
																	  holdingCost, penaltyCost, fixedOrderingCost, unitCost, t);
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
					immediateCost = demandProbabilities[demand][Stages-1]
								   		*computeImmediateCostFirstPeriod(inventory, i, demand, optimalCost,
		   										 holdingCost, penaltyCost);
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
