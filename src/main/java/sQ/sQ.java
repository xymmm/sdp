package sQ;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import sdp.data.Instance;
import sdp.data.Solution;
import sdp.data.sQsolution;
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
	         int [] inventory, 
	         int i, 
	         int Actions, 
	         int demand,
	         double holdingCost, 
	         double penaltyCost, 
	         double fixedOrderingCost, 
	         double unitCost) {
		   if(Actions == 0) {
			   return holdingCost*Math.max(0, inventory[i] + Actions - demand) + penaltyCost *Math.max(0, demand - inventory[i] - Actions);
		   }else {
			   return fixedOrderingCost + unitCost * Actions 
					   + holdingCost*Math.max(0, inventory[i] + Actions - demand) + penaltyCost *Math.max(0, demand - inventory[i] - Actions);
		   }
	   }
	   
	   
	   /** get optimal cost **/
	   static double getOptimalCost(double[] expectedTotalCosts) {
			double optimalCost = expectedTotalCosts[0];
			for(int a=1;a<expectedTotalCosts.length;a++) {
				if(expectedTotalCosts[a]<optimalCost) {
					optimalCost = expectedTotalCosts[a];
				}
			}
			return optimalCost;
		}
	   
	   
	   /** get optimal action Q **/
      static double getOptimalQuantity(double[] expectedTotalCosts) {
         double min = expectedTotalCosts[0];
         double action = 1;
         for(int a=2;a<expectedTotalCosts.length;a++) {
            if(expectedTotalCosts[a]<min) {
               min = expectedTotalCosts[a];
               action = a;
            }
         }
         return action;
      }
      
      /** get optimal action from 0 or **/
      public static int[][] getOptimalAction(double[][] CostQ, double[][] CostNoAction) {
    	  int[][] optimalAction = new int[CostQ.length][CostQ[0].length];
    	  for(int t=0;t<CostQ[0].length;t++) {
    		  for(int i=0;i<CostQ.length; i++) {
    			  if(CostQ[i][t] <= CostNoAction[i][t]) {
    				  optimalAction [i][t] = 1;
    			  }else {
    				  optimalAction[i][t] = 0;
    			  }
    		  }
    	  }
    	  return optimalAction;
      }
      
      /** print results **/
      public static void printCost(Instance instance, Solution sQsolution) {
    	  /** print optimal costs **/
    	  for(int i=0;i<sQsolution.inventory.length;i++) {
    		  System.out.print((i+instance.minInventory)+" ");
    		  for(int t=0;t<instance.getStages();t++) {
    			  System.out.print(sQsolution.optimalCost[i][t] + " ");
    		  }System.out.println();
    	  }
    	  System.out.println();
      }

	  /** print optimal actions **/
      public static void printAction(Instance instance, sQsolution sQsolution) {
    	  for(int i=0;i<sQsolution.inventory.length;i++) {
    		  System.out.print((i+instance.minInventory)+" ");
    		  for(int t=0;t<instance.getStages();t++) {
    			  System.out.print(sQsolution.optimalAction[i][t] + " ");
    		  }System.out.println();
    	  }
    	  System.out.println();
      }

      
      /** Plot the expected optimal cost *
      public static void plotCosts(Instance instance, sQsolution sQsolution, int currentPeriod) {
    	  XYSeries series1 = new XYSeries("Reordering");
    	  for(int i=0-instance.minInventory;i<sQsolution.inventory.length;i++) {
    		  series1.add(0-instance.minInventory,sQsolution.CostQ[i][currentPeriod]);
    	  }
    	  XYSeries series2 = new XYSeries("No Reordering");
    	  for(int i=0-instance.minInventory;i<sQsolution.inventory.length;i++) {
    		  series2.add(0-instance.minInventory,sQsolution.CostNoAction[i][currentPeriod]);
    	  }
    	  XYSeriesCollection collection = new XYSeriesCollection();
    	  collection.addSeries(series1);
    	  collection.addSeries(series2);
    	  JFreeChart chart = ChartFactory.createXYLineChart("Expected Total Cost for Period "+ (currentPeriod+1), "Opening inventory level", "Expected total cost",
    			  collection, PlotOrientation.VERTICAL, true, true, false);
    	  ChartFrame frame = new ChartFrame("Period "+(currentPeriod+1),chart);
    	  frame.setVisible(true);
    	  frame.setSize(500,400);
      }*/
      

      
      
      
      public static sQsolution solvesQInstance(Instance instance) {
    	  
          int Stages = instance.getStages();
          
          int[] inventory = new int [instance.maxInventory - instance.minInventory + 1];
          for(int i=0;i<inventory.length;i++) {
             inventory[i] = i + instance.minInventory;
          }
         
          
          double demandProbabilities [][] = computeDemandProbability(instance.demandMean, instance.maxDemand, instance.tail);

          /** a=Q**/
          
          /** Locally used. 
           * The fist index represents the inventory level, second index represents the action quantity, and third index represents stages.
           * An entry of totalCost[][] represents the cost for an inventory level with an action at one stage.
           * **/
          double totalCost[][][] = null;
          /**
           * This array stores the optimal cost for an inventory level among all possible actions.
           * Every entry is obtained by searching the minimum entry for the local totalCost[i][:].
           * **/
          double CostQ[][] = new double [inventory.length][Stages];
          
          /**
           * This array stores the quantity corresponding to the optimal cost for an inventory level at a stage.
           * 
          double optimalQuantity[][] = new double [inventory.length][Stages];
          **/
          
          
          /** a=0 **/
          double CostNoAction [][] = new double [inventory.length][Stages];
          
          
          double immediateCost;
          
          
          /** The Optimal Action **/
          int OptimalAction[][][] = new int[inventory.length][instance.maxQuantity][instance.getStages()];
          
          

          /** Cost Computation a=0 **/
          for(int t=instance.getStages()-1;t>=0;t--) { // Time       	                
        	  for(int i=0;i<inventory.length;i++) { // Inventory 	   
        		  for(int d=0;d<demandProbabilities[t].length;d++) { // Demand
        			  if((inventory[i] - d <= instance.maxInventory) && (inventory[i] - d >= instance.minInventory)) {
        				  immediateCost = demandProbabilities[t][d]*(
        						  computeImmediateCost(inventory,i, 0, d, instance.holdingCost, instance.penaltyCost, instance.fixedOrderingCost, instance.unitCost)
        						  + ((t==Stages-1) ? 0 : CostNoAction[i-d][t+1]) );
        			  }else {
        				  immediateCost = Double.POSITIVE_INFINITY;
        			  }
        			  CostNoAction[i][t] = CostNoAction[i][t] + immediateCost;
        		  }
        	  }
          }
          
          
          /** Cost Computation a = Q, single Q for all periods **/
          for(int a=1; a<=instance.maxQuantity;a++) { //for a possible quantity, start from Q=1 to maxQuantity
              for(int t=instance.getStages()-1;t>=0;t--) { // Time       	                
        		  for(int i=0;i<inventory.length;i++) { // Inventory
        			  totalCost = new double [inventory.length][t == 0 ? 1 : instance.maxQuantity+1][instance.getStages()];
        			  for(int d=0;d<demandProbabilities[t].length;d++) { // Demand
        				  if((inventory[i] + a - d <= instance.maxInventory) && (inventory[i] + a - d >= instance.minInventory)) {
        					  immediateCost = demandProbabilities[t][d]*(
        							  computeImmediateCost(inventory,i, a, d, instance.holdingCost, instance.penaltyCost, instance.fixedOrderingCost, instance.unitCost)
        							  + ((t==Stages-1) ? 0 : totalCost[i+a-d][a-1][t+1]) );
        				  }else {
        					  immediateCost = Double.POSITIVE_INFINITY;
        				  }
        				  totalCost[i][a-1][t] = totalCost[i][a-1][t] + immediateCost;
        			  }
        			  if(totalCost[i][a-1][t]<=CostNoAction[i][t]) {
        				  OptimalAction[i][a-1][t] = 1;
        			  }else {
        				  OptimalAction [i][a-1][t] = 0;
        			  }
        		  }
        	  }
              System.out.println("Q = "+a+" .");
          }

    	  return new sQsolution(totalCost, CostNoAction, inventory, OptimalAction);
    	  
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
    	  int maxQuantity = 250;


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

    	  sQsolution sQsolution = solvesQInstance(instance);
    	  
          for(int i=0;i<sQsolution.inventory.length;i++) {
        	  System.out.print((i+instance.minInventory)+" ");
        	  for(int t=0;t<instance.getStages();t++) {
        		  System.out.print(sQsolution.CostNoAction[i][t]+" ");
        	  }System.out.println();
          }

      }
      

		
		
	
	
	

}
