package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import javax.swing.JFrame;
import org.apache.commons.collections15.Transformer;
import weka.core.Instance;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.PluggableGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;
import other.MyLink;

public class GraphView {
	public void visualize(Graph<Instance, MyLink> graph) {

		Transformer<Instance, Point2D> locationTransformer = new Transformer<Instance, Point2D>() {
			@Override
			public Point2D transform(Instance vertex) {
				double y = vertex.value(1);
				double x = vertex.value(0);

				return new Point2D.Double(y, x);
			}
		};

		StaticLayout<Instance, MyLink> layout = new StaticLayout<Instance, MyLink>(graph, locationTransformer);

		layout.setSize(new Dimension(600, 600));
		VisualizationViewer<Instance, MyLink> vv = new VisualizationViewer<Instance, MyLink>(layout);
		Dimension di = layout.getSize();
		Point2D center = new Point2D.Double(di.width / 2, di.height / 2);
		vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT).rotate(Math.PI, center);

		vv.setPreferredSize(new Dimension(600, 600));
		Transformer<Instance, Paint> vertexColor = new Transformer<Instance, Paint>() {
			public Paint transform(Instance i) {
				if (i.classValue() == 0.0)
					return Color.GREEN;
				else if (i.classValue() == 1)
					return Color.BLUE;
				else if (i.classValue() == 2)
					return Color.BLACK;
				else if (i.classValue() == 3)
					return Color.YELLOW;
				else if (i.classValue() == 4)
					return Color.RED;
				else if (i.classValue() == 5)
					return Color.CYAN;
				else if (i.classValue() == 6)
					return Color.MAGENTA;
				else if (i.classValue() == 7)
					return Color.PINK;
				else if (i.classValue() == 8)
					return Color.ORANGE;
				else {
					return Color.WHITE;
				}
			}
		};

		Transformer<Instance, Shape> vertexSize = new Transformer<Instance, Shape>() {
			public Shape transform(Instance i) {
				Ellipse2D circle = new Ellipse2D.Double(-15, -15, 30, 30);
				return AffineTransform.getScaleInstance(0.20, 0.20).createTransformedShape(circle);
			}
		};

		vv.getRenderContext().setVertexFillPaintTransformer(vertexColor);
		vv.getRenderContext().setVertexShapeTransformer(vertexSize);

		vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT).rotate(Math.PI / 2, center);

		PluggableGraphMouse gm = new PluggableGraphMouse();
		gm.add(new TranslatingGraphMousePlugin(MouseEvent.BUTTON1_MASK));
		gm.add(new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0, 1.1f, 0.9f));

		vv.setGraphMouse(gm);
		vv.setBackground(Color.WHITE);

		JFrame frame = new JFrame("Graph");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(vv);
		// frame.setLocationRelativeTo(null);
		frame.pack();
		frame.setVisible(true);
	}
}
