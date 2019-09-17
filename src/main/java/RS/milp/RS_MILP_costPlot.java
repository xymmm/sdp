package RS.milp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import sQ.sdp.sQsolution;
import sdp.data.Instance;


public class RS_MILP_costPlot {
	
	/**
	 * Read costs of RS by MILP/CPLEX from text file.
	 * This method returns cost values as double.
	 * **/
	private static double[][] getFile(String pathName) throws Exception {
        File file = new File(pathName);
        if (!file.exists())
            throw new RuntimeException("Not File!");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String str;
        List<double[]> list = new ArrayList<double[]>();
        while ((str = br.readLine()) != null) {
            int s = 0;
            String[] arr = str.split(",");
            double[] dArr = new double[arr.length];
            for (String ss : arr) {
                if (ss != null) {
                    dArr[s++] = Double.parseDouble(ss);
                }
            }
            list.add(dArr);
        }
        int max = 0;
        for (int i = 0; i < list.size(); i++) {
            if (max < list.get(i).length)
                max = list.get(i).length;
        }
        double[][] array = new double[list.size()][max];
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < list.get(i).length; j++) {
                array[i][j] = list.get(i)[j];
            }
        }
        return array;
    }
	
	/** Plot costs of RS by MILP**/
	static void plotRScostMILP(double[] RScostMILP, int minInventory) {
		XYSeries series = new XYSeries("RS-MILP plot");
		for(int i=0;i<RScostMILP.length;i++) {
			series.add((i+minInventory),RScostMILP[i]);
		}
		XYDataset xyDataset = new XYSeriesCollection(series);
		JFreeChart chart = ChartFactory.createXYLineChart("Cost of (R,S) policy by MILP", "initial inventory level", "Expected total cost",
				xyDataset, PlotOrientation.VERTICAL, true, true, false);
		ChartFrame frame = new ChartFrame("RS-MILP plot",chart);
		frame.setVisible(true);
		frame.setSize(1500,1200);
	}
    
    public static void main(String[] args) throws Exception{
    	
    	int minInventory = -500;
    	int maxInventory = 500;
    	int inventoryLength = maxInventory-minInventory+1;
    	
        double[][] read = getFile("F:/EclipseInstall/sdp/src/main/java/RS/milp/ofile1.txt");
        double[] RScostMILP = new double[inventoryLength];
        for(int i=0; i<RScostMILP.length;i++) {
        	RScostMILP[i] = read[i][0];
        }
        
        plotRScostMILP(RScostMILP, minInventory);


    }
    
    

	


}
