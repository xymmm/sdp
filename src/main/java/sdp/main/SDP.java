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
	private Demand demand = new Demand();
	
	public static void main(String[] args) {
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
		
		
		//first stage
		ICostOriginalInventory stageOne = new CostOriginalInventory();
		f = stageOne.calCostOriginalInventory(sdp.demand, f);
		
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
