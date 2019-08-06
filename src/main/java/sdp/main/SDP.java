package sdp.main;

import java.text.DecimalFormat;

import org.apache.commons.math3.distribution.PoissonDistribution;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import sdp.dataProcess.Data;
import sdp.dataProcess.Demand;
import sdp.stage.ICostOriginalInventory;
import sdp.stage.ICostWithHistory;
import sdp.stage.ICostWithoutHistory;
import sdp.stage.impl.CostOriginalInventory;
import sdp.stage.impl.CostWithHistory;
import sdp.stage.impl.CostWithoutHistory;

public class SDP {
	private static final int StateSpace = 0;






	/************************* 2nd Aug **************************/
	//private Demand demand = new Demand(); //DO NOT USE
   
   static void computeExpectedTotalCost(int t, int inventoryLevel) {
      
   }
   
   static double computeTransitionProbabilities(int t, int i, int a, int j, int[] demandMean, double tail) {  
      double prob;    
      //create a Poisson demand d with mean demandMean[t]
	  PoissonDistribution dist = new PoissonDistribution(demandMean[t]);
	  
	  //compute transition probability
	  prob = dist.probability(i+a-j);
	  
	  //discard small values 
	  if(prob<=tail) {
		  prob = 0;
	  }   
      return prob; // need to return d.pmf(i+a-j)
   }
   

   /** Don't need to precompute!!! **/
   static double[][][][] computeTransitionProbabilities(int[] demandMean, int maxInventory, int maxQuantity, double tail) {
      double[][][][] transitionProbabilities = new double[demandMean.length][2*maxInventory+1][maxQuantity+1][2*maxInventory+1];//[time][initial state][action][end state]
      
      for(int t = 0; t < demandMean.length; t++) { //this is the time index
         for(int i = 0; i < 2*maxInventory+1; i++) { //this is the initial state inventory level 
            for(int a = 0; a < maxQuantity+1; a++) { //this is the action
               for(int j = 0; j < 2*maxInventory+1; j++) { //this is the end state inventory level level
                  transitionProbabilities[t][i][a][j] = computeTransitionProbabilities(t,i,a,j,demandMean,tail);
               }
            }
         }
      }
      return transitionProbabilities;
   }
   
   /************************* 2nd Aug Ends **************************/
   
   
   
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
   
   /** generate feasible action for a given state (inventory level) **/
   static int[] generateFeasibleActions(int inventoryLevel, int demand, int maxInventory, int maxQuantity, int Stages, int maxDemand) {
	   int[] feasibleActions = new int [maxQuantity+1];
	   for(int a=0;a<feasibleActions.length;a++) {
		   feasibleActions[a] = a;
	   }
	   for(int a = 0;a<feasibleActions.length;a++) {
		   if ( (inventoryLevel + feasibleActions[a] -demand <= maxInventory) && (inventoryLevel + feasibleActions[a] - demand >= -Stages*maxDemand) ){
			   feasibleActions[a] = a;
		   }else {
			   feasibleActions[a] = (int) Double.POSITIVE_INFINITY;
		   }
	   }
	   return feasibleActions;
   }
   
   /** compute immediate cost **/
   static double computeImmediateCostLastPeriod(int inventoryLevel, int Actions, int demand,
		   										double holdingCost, double penaltyCost, double fixedOrderingCost, double unitCost) {
	   if(Actions == 0) {
		   return holdingCost*Math.max(0, inventoryLevel + Actions - demand) + penaltyCost *Math.max(0, demand - inventoryLevel -Actions);
	   }else {
		   return fixedOrderingCost + unitCost * Actions 
				   + holdingCost*Math.max(0, inventoryLevel + Actions - demand) + penaltyCost *Math.max(0, demand - inventoryLevel -Actions);
	   }
   }
   
   static double computeImmediateCost(int inventoryLevel, int Actions, int demand, double[][] ExpectedTotalOptimalCost,
									  double holdingCost, double penaltyCost, double fixedOrderingCost, double unitCost, int currentStageIndex) {
	   if(Actions == 0) {
		   return holdingCost*Math.max(0, inventoryLevel + Actions - demand) + penaltyCost *Math.max(0, demand - inventoryLevel -Actions)+
			   + ExpectedTotalOptimalCost[inventoryLevel+Actions-demand+1][currentStageIndex+1];
	   }else {
		   return fixedOrderingCost + unitCost * Actions 
				   + holdingCost*Math.max(0, inventoryLevel + Actions - demand) + penaltyCost *Math.max(0, demand - inventoryLevel -Actions)
				   + ExpectedTotalOptimalCost[inventoryLevel+Actions-demand+1][currentStageIndex+1];
	   }
   }
   
   static double computeImmediateCostFirstPeriod(int inventoryLevel, int demand, double [][] ExpectedTotalOptimalCost,
		   										 double holdingCost, double penaltyCost) {
	   return holdingCost*Math.max(0, inventoryLevel - demand) + penaltyCost*Math.max(0, demand - inventoryLevel)
			   + ExpectedTotalOptimalCost[inventoryLevel-demand+1][1];
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
	   
	   /** Follow the template https://github.com/gwr3n/jsdp/wiki/Introduction-to-Stochastic-Dynamic-Programming **/
	   
	   /** SDP boundary conditions **/
	   double tail = 0.0001;
	   
	   int maxDemand = 250;
	   int maxInventory = 250;
	   int maxQuantity = 250;


	   /** How do you model stages? **/
	   int Stages = demandMean.length;
	   
	   
	   /** How do you model states? 
	    * 
	    * The state s in period t represents the initial inventory level at the beginning of period t, 
	    * which takes the values in -maxInventory,...,0,...,maxInventory with possible backorders.
	    * 
	    **/
	   // Choice about the indexing strategy for your arrays in the code. What does the first index represent? What does the second? And so on...
	   int[] inventoryLevel = new int [maxInventory + Stages*maxInventory+1];
	   for(int i=0;i<inventoryLevel.length;i++) {
		   inventoryLevel[i] = i - Stages*maxDemand;
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
	   /*
	   for(int i=0;i<demandProbabilities.length;i++) {
		   for(int j=0; j<demandProbabilities[0].length;j++) {
			   System.out.print(demandProbabilities[i][j]+" ");
		   }System.out.println();
	   }*/
	   
	   /** How do you model immediate costs? **/
	   
	   /** How do you model/compute expected total costs? **/
	   double expectedTotalCost[][][] = new double [Stages][inventoryLevel.length][maxQuantity+1]; //Valuable expectedTotalCost
      
	   double expectedTotalOptimalCost[][] = new double [Stages][inventoryLevel.length]; //Valuable expectedTotalOptimalCost
      
	   //double expectedTotalOptimalAction[][] = new double [StateSpace][Stages]; //Valuable expectedTotalOptimalAction
	   
	   
	   double immediateCost;
	   
	   


	   
	   
	   
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
