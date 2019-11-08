package sdp.util;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import sdp.data.Instance;

public class plotOneDimensionArray {
	
	/**plot OPTIMAL cost with a given Q**/
	public static void plotCostGivenQGivenStage(double[] value, int[] index, String x, String y, String title) {
		XYSeries series = new XYSeries("Optimal Cost with Given Q");
		for(int i = 0; i<value.length;i++) {
			series.add(index[i],value[i]);
		}
		XYDataset xyDataset = new XYSeriesCollection(series);
		JFreeChart chart = ChartFactory.createXYLineChart(title, x, y, xyDataset, PlotOrientation.VERTICAL, true, true, false);
		ChartFrame frame = new ChartFrame(title,chart);
		frame.setVisible(true);
		frame.setSize(1800,1500);
	}

}
