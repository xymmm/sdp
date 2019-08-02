package sdp.main;

import java.text.DecimalFormat;

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
	private Demand demand = new Demand(); //DO NOT USE
   
   static void computeExpectedTotalCost(int t, int inventoryLevel) {
      
   }
   
   static double computeTransitionProbabilities(int t, int i, int a, int j, int[] demandMean) {
      //create a Poisson demand d with mean demandMean[t]
      return 0.0; // need to return d.pmf(i+a-j)
   }
	
   /** Don't need to precompute!!! **/
   static double[][][][] computeTransitionProbabilities(int[] demandMean) {
      double[][][][] transitionProbabilities = new double[10][10][10][10];
      for(int t = 0; t < 10; t++) { //this is the time index
         for(int i = 0; i < 10; i++) { //this is the initial state inventory level 
            for(int a = 0; a < 10; a++) { //this is the action
               for(int j = 0; j < 10; j++) { //this is the end state inventory level level
                  transitionProbabilities[t][i][a][j] = computeTransitionProbabilities(t,i,a,j,demandMean);
               }
            }
         }
      }
      return null;
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
	   
	   int maxDemand = 200;
	   int maxInventory = 250;
	   int maxQuantity = 250;
	   
	   /** Working arrays **/
	   
	   double transitionProbabilities[][][][] = computeTransitionProbabilities(demandMean); // But do you really need this array?
	   
	   double expectedTotalCost[][][] = null; //Valuable expectedTotalCost[t][i][a]
	         
      double expectedTotalOptimalCost[][] = null; //Valuable expectedTotalOptimalCost[t][i]
            
      double expectedTotalOptimalAction[][] = null; //Valuable expectedTotalOptimalAction[t][i]
	                   
	   
	   /** How do you model stages? **/
	   // # stages = demandMean.length
	   
	   /** How do you model states? **/
	   // Choice about the indexing strategy for your arrays in the code. What does the first index represent? What does the second? And so on...
	   
	   /** How do you model actions? **/
	   // Actions are a consequence of the stage, state in which you are. YOu need a "way" of recording or generating these actions. Could be a matrix or a function.
	   //int generateOptimalAction(int t, int inventoryLevel)
	   //int[] generateFeasibleActions(int t, int inventoryLevel)
	   //void computeExpectedTotalCost(int t, int inventoryLevel)
	   
	   /** How do you model transition probabilities? **/
	   
	   /** How do you model immediate costs? **/
	   
	   /** How do you model/compute expected total costs? **/
      
      
      
      
      /************* DO NOT USE ****************************************/
		
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
		*/
		
		
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
		

	}

}
