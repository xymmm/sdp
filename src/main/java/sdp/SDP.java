package sdp;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import sdp.data.Instance;
import sdp.data.Solution;
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
	   static double[][] computePurchasingCost(double[][] totalCost, double fixedOrderingCost, double unitCost) {
		   for(int i=0;i<totalCost.length;i++) {
			   for(int a=0;a<totalCost[0].length;a++) {
				   if(a==0) {
					   totalCost[i][a] = 0;
				   }else {
					   totalCost[i][a] = fixedOrderingCost + a*unitCost;
				   }
			   }
		   }
		   return totalCost;
	   }
	   
	   /** compute cumulative probability for scenario normalization**/
	   static double computeCumulativeProb(int currentStage, 
			   							   int inventoryLevel, 
			   							   int action, 
			   							   int maxInventory, 
			   							   int minInventory, 
			   							   double[][] demandProbabilities) {
		   double cumulativeProb = 0;
		   for(int d=0;d<demandProbabilities[currentStage].length;d++) {
			   if((inventoryLevel + action - d <= maxInventory) && (inventoryLevel + action - d >= minInventory)) {
				   cumulativeProb = cumulativeProb + demandProbabilities[currentStage][d];
			   }
		   }
		   return cumulativeProb;
	   }
	   
	   /** get optimal cost
	    * 
	    * **/
	   static double getOptimalCost(double[] expectedTotalCosts) {
			double min = expectedTotalCosts[0];
			for(int a=1;a<expectedTotalCosts.length;a++) {
				if(expectedTotalCosts[a]<min) {
					min = expectedTotalCosts[a];
				}
			}
			return min;
		}
	   
	   /** get optimal action
	    * 
	    */
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
	   
	   public static Solution solveInstance(Instance instance) {
	      
	      /** How do you model stages? **/
         int Stages = instance.getStages();
         
         
         /** How do you model states? 
          * 
          * The state represents the inventory level at the beginning of a period.
          * 
          * Given the maximum demand, maximum inventory and the planning horizon, 
          * the inventory level that all periods can possibly stand is ranged from [-planning Horizon * maximum Demand, maximum Inventory],
          * and this set is considered as the state space.
          * 
          * We use an integer array, inventory[], to store all possible states.
          * The index of inventory[] is a serial number, which will be used in the following computation to track back to the state. 
          * **/
         int[] inventory = new int [instance.maxInventory - instance.minInventory + 1];
         for(int i=0;i<inventory.length;i++) {
            inventory[i] = i + instance.minInventory;
         }
         
         /** How do you model actions? 
          * 
          * The action represents placing an order with quantity Q at the beginning of a period. 
          * Q can take a value from 0 to a given maximum quantity.
          * 
          * For any state i, the value of feasible action a satisfies:
          *                 -planning horizon * maximum demand <= i - demand + a <= maximum inventory.
          * 
          * By traversing all possible actions from 0 to the maximum, 
          * the feasible actions can be generated by satisfying the condition above,
          * and are directly substituted into the immediate cost.
          * **/
         double optimalAction[][] = new double [inventory.length][Stages];
               
         /** How do you model transition probabilities?
         *
          * The transition probability represents the probability of that one state i transfers to a state j if action a is taken.
          * It is generally denoted as P(i,j,a) = Pr(demand = i+a-j), which is related to the probability of demand.
          * So we transform the transition probability to the demand probability in order to simplify the computation. 
          * 
          * Given the maximum demand, we compute the probability of each possible demand in each period, and store them in a 2-D array, demandProbabilities[][].
          * The first index represents the possible demand value ranged from 0 to maxDemand.
          * The second index represents the time period.
          * **/
         double demandProbabilities [][] = computeDemandProbability(instance.demandMean, instance.maxDemand, instance.tail);
      
         /** How do you model/compute expected total costs? 
         *
          * A 2D array is created to store the expected total cost for a period.
          * The first index represents the possible states, and the second index represents the possible actions.
          * 
          * For a state, we go through all possible actions; 
          *    for a state and a possible action, we judge if the action is feasible, and then compute the immediate cost.
          *    if the action is feasible, we multiply the demand probability to the immediate cost to obtain expected cost cumulatively,
          *    and the cost of previous periods is added when it is applicable.
          * This value is stored in totalCost[state][action] locally.
          *    
          * A 2D array is created to store the optimal cost.
          * The first index represents the possible states, and the second index represents the stages.
          * Each entry in the array optimalCost[state][stage] is obtained by selecting the minimum value of array totalCost[state] within each period.
          * **/
         double totalCost[][] = null;
         double optimalCost[][] = new double [inventory.length][Stages]; 
         
         
         /** Compute Expected Cost **/
         
         for(int t=Stages-1;t>=0;t--) { // Time
        	 totalCost = new double [inventory.length][t == 0 ? 1 : instance.maxQuantity+1];
			 //compute purchasing cost
			 totalCost = computePurchasingCost(totalCost, instance.fixedOrderingCost, instance.unitCost);
        	 
        	 for(int i=0;i<inventory.length;i++) { // Inventory
        		 for(int a = 0; a <= ((t==0) ? 0 : instance.maxQuantity);a++) { //Actions

        			 //initialize cumulative probability for scenario normalization
        			 double scenarioProb = computeCumulativeProb(t, inventory[i], a, instance.maxInventory, instance.minInventory,demandProbabilities);

        			 for(int d=0;d<demandProbabilities[t].length;d++) { // Demand
        				 double immediateCost;
        				 
        				 if((inventory[i] + a - d <= instance.maxInventory) && (inventory[i] + a - d >= instance.minInventory)) {
        					 //compute immdediate cost
        					 immediateCost = demandProbabilities[t][d]*(
        							 			computeImmediateCost(inventory[i], a, d, instance.holdingCost, instance.penaltyCost, instance.fixedOrderingCost, instance.unitCost)
        							 			+ ((t==Stages-1) ? 0 : optimalCost[i+a-d][t+1]) );
        					 //update total cost for feasible demand value
            				 totalCost[i][a] = totalCost[i][a] + immediateCost;
        				 }//else, we do nothing.
        			 }
        			 //normalization on scenarios
        			 totalCost[i][a] = totalCost[i][a]/scenarioProb;
        			 
        			 //if((t==3)&&(i==0)&&(a==0)) {System.out.println(scenarioProb);}
        			 //if((t==3)&&(i==251)&&(a==0)) {System.out.println(scenarioProb);}
        		 }
        		 optimalCost[i][t] = getOptimalCost(totalCost[i]);
        		 optimalAction[i][t] = getOptimalAction(totalCost[i]);
        	 }
         }
 
         
         return new Solution(optimalAction, optimalCost, inventory);

	   }
	   
	   public static void printSolution(Instance instance, Solution solution) {
         /** print optimal costs **/
         for(int i=0;i<solution.inventory.length;i++) {
            System.out.print((i+instance.minInventory)+" ");
            for(int t=0;t<instance.getStages();t++) {
               System.out.print(solution.optimalCost[i][t] + " ");
            }System.out.println();
         }
         System.out.println();
         
         /** print optimal actions *
         for(int i=0;i<solution.inventory.length;i++) {
             System.out.print((i+instance.minInventory)+" ");
             for(int t=0;t<instance.getStages();t++) {
               System.out.print(solution.optimalAction[i][t] + " ");
             }System.out.println();
           }
         System.out.println();
         */
         
       /** Plot the expected optimal cost **/
       XYSeries series = new XYSeries("SDP Plot");
       for(int i=0-instance.minInventory;i<solution.inventory.length;i++) {
          series.add(i+instance.minInventory,solution.optimalCost[i][0]);
         }
         XYDataset xyDataset = new XYSeriesCollection(series);
         JFreeChart chart = ChartFactory.createXYLineChart("SDP Model", "Opening inventory level", "Expected total cost",
               xyDataset, PlotOrientation.VERTICAL, false, true, false);
         ChartFrame frame = new ChartFrame("SDP Plot",chart);
         frame.setVisible(true);
         frame.setSize(500,400);
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
		   
		   Solution solution = solveInstance(instance);
		   
		   printSolution(instance, solution);
		}

}
