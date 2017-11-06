package view;

import javax.swing.JFrame;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class XYView {

	public void start(XYSeries series1, XYSeries series2, String x, String y, String windowName) {
		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(series1);
		dataset.addSeries(series2);
		ValueAxis xA = new NumberAxis(x);
		ValueAxis yA = new NumberAxis(y);
		XYItemRenderer renderer = new StandardXYItemRenderer();
		Plot p = new XYPlot(dataset, xA, yA, renderer);

		JFreeChart chart = new JFreeChart(p);
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));

		JFrame frame = new JFrame(windowName);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(chartPanel);
		frame.setLocationRelativeTo(null);
		frame.pack();
		frame.setVisible(true);
	}
}
