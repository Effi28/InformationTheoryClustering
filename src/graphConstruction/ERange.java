package graphConstruction;

import cern.colt.matrix.DoubleMatrix2D;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import other.Methods;
import other.MyLink;
import other.Parameters;
import weka.core.Instance;
import weka.core.Instances;

public class ERange {
	double e;
	Graph<Instance, MyLink> graph;

	public DoubleMatrix2D start(Instances data, double e) {
		this.e = e;
		DoubleMatrix2D dMatrix = Methods.createDistanceMatrix(data);
		Graph<Instance, MyLink> graphMinTree = Methods.createGraph(data, dMatrix, 1);
		
		//approx of e
		if (e == 0) {
			double maxweight = 0;
			for (MyLink m : graphMinTree.getEdges()) {
				if (m.getWeight() > maxweight) {
					maxweight = m.getWeight();
				}
			}
			e = (maxweight + 1) / 8;
		}
		graph = new SparseGraph<Instance, MyLink>();

		for (int i = 0; i < data.numInstances(); i++) {
			graph.addVertex(data.instance(i));
		}

		if(Parameters.DEBUG){
			System.out.println("E" + e);
			System.out.println("Instances " + data.numInstances());
			System.out.println("Edges " + graph.getEdgeCount());
		}

		for (int i = 0; i < dMatrix.columns(); i++) {
			for (int j = i + 1; j < dMatrix.rows(); j++) {
				if (dMatrix.get(i, j) < e) {
					graph.addEdge(new MyLink(dMatrix.get(i, j)), data.instance(i), data.instance(j));
				}
			}
		}

		return Methods.fromGraphToSimMatrix(data, graph, dMatrix);
	}

	public double getE() {
		return e;
	}

	public void setE(double e) {
		this.e = e;
	}

	public Graph<Instance, MyLink> getGraph() {
		return graph;
	}

	public void setGraph(Graph<Instance, MyLink> graph) {
		this.graph = graph;
	}

}
