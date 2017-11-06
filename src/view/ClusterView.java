package view;

import java.awt.Dimension;

import javax.swing.JFrame;

import weka.core.Instances;
import weka.gui.visualize.Plot2D;
import weka.gui.visualize.PlotData2D;

public class ClusterView {

	public void visualize(Instances instances) throws Exception {
		Plot2D p = new Plot2D();
		PlotData2D plot = new PlotData2D(instances);
		p.addPlot(plot);
		p.setXindex(0);
		p.setYindex(1);
		p.setCindex(2);

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setMinimumSize(new Dimension(800, 600));
		frame.getContentPane().add(p);
		frame.setLocationRelativeTo(null);
		frame.pack();
		frame.setVisible(true);
	}
}
