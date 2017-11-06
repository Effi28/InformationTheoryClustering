package graphConstruction;

import java.util.List;
import weka.core.Instance;
import weka.core.Instances;
import cern.colt.matrix.DoubleMatrix2D;
import edu.uci.ics.jung.graph.Graph;
import other.Methods;
import other.MethodsKNN;
import other.MyLink;
import other.Pair;
import other.Parameters;

public class MDLKNN {
	Graph<Instance, MyLink> graph;
	public DoubleMatrix2D start(Instances data) {

		// Calculate baseline codingcosts of a dataset
		DoubleMatrix2D distanceMatrix = Methods.createDistanceMatrix(data);
		double baselineCosts = Methods.baselineMatrixNormal(distanceMatrix);
		// Create graph from the given dataset
		graph = Methods.createGraph(data, distanceMatrix, 1.0);

		if (Parameters.DEBUG) {
			System.out.println("BaselineCosts: " + baselineCosts);
			System.out.println("Instances " + data.numInstances());
			System.out.println("Edges " + graph.getEdgeCount());
		}
		// Calculates the codingCosts of the adjacencymatrix
		double graphCodingCosts = Methods.codingCostsGraph(graph);
		double regressionError = Methods.Regression(graph, distanceMatrix, data).getSumSquaredErrors();
		int k = (int) Math.log(data.numInstances());
		while ((graphCodingCosts + regressionError) * 1.5 < baselineCosts) {
			for (int i = 0; i < distanceMatrix.columns(); i++) {
				List<Pair> orderOfAddedEdges = MethodsKNN.createOrderedList(distanceMatrix.viewColumn(i), i);
				for (int j = 0; j < k; j++) {
					int column = orderOfAddedEdges.get(j).getX();
					int row = orderOfAddedEdges.get(j).getY();
					graph.addEdge(new MyLink(distanceMatrix.get(i, j)), data.instance(column), data.instance(row));
				}
			}
			//update
			regressionError = Methods.Regression(graph, distanceMatrix, data).getSumSquaredErrors();
			graphCodingCosts = Methods.codingCostsGraph(graph);
			if (Parameters.DEBUG) {
				System.out.println("Graphkodierungskosten: " + graphCodingCosts + " k " + k + " coding "
						+ regressionError);	
			}
			k++;
		}
		return Methods.fromGraphToSimMatrix(data, graph, distanceMatrix);
	}
	public Graph<Instance, MyLink> getGraph() {
		return graph;
	}
}
