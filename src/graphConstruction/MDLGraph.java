package graphConstruction;

import java.util.List;
import cern.colt.matrix.DoubleMatrix2D;
import edu.uci.ics.jung.graph.Graph;
import other.Methods;
import other.MyLink;
import other.Pair;
import other.Parameters;
import weka.core.Instance;
import weka.core.Instances;

public class MDLGraph {

	private Graph<Instance, MyLink> graph;
	private DoubleMatrix2D distanceMatrix;
	private double baselineCosts;

	public DoubleMatrix2D start(Instances data) {
		//calculate distance matrix
		distanceMatrix = Methods.createDistanceMatrix(data);
		//calculate baselinecosts from distance matrix
		baselineCosts = Methods.baselineMatrixNormal(distanceMatrix);
		//generate MST graph from distance matrix
		graph = Methods.createGraph(data, distanceMatrix, 1);
		//initial regression
		double initialError = Methods.Regression(graph, distanceMatrix, data).getSumSquaredErrors();
		if (Parameters.DEBUG) {
			System.out.println("BaselineCosts: " + baselineCosts);
			System.out.println("InitialError: " + initialError);
			System.out.println("Instances " + data.numInstances());
			System.out.println("Edges " + graph.getEdgeCount());
		}
		//used heuristic
		nearestDistHeuristic(data, initialError);
		//transform graph to similarity matrix
		return Methods.fromGraphToSimMatrix(data, graph, distanceMatrix);
	}

	private Graph<Instance, MyLink> nearestDistHeuristic(Instances data, double initialError) {

		// Construct a ordererd List with the edges which are next inserted
		List<Pair> orderedEdges = Methods.createOrderedList(distanceMatrix);
		double error = initialError;
		double graphCosts = Methods.codingCostsGraph(graph);
		for (int i = 0; (graphCosts + error) < baselineCosts && i < orderedEdges.size()
				&& graph.getEdgeCount() < graph.getVertices().size() * 4.5; i++) {
			int column = orderedEdges.get(i).getX();
			int row = orderedEdges.get(i).getY();
			if (graph.findEdge(data.instance(column), data.instance(row)) == null) {
				MyLink edge = new MyLink(orderedEdges.get(i).getValue());
				graph.addEdge(edge, data.instance(column), data.instance(row));
				if (i % 4 == 0) {
					double regressionError = Methods.Regression(graph, distanceMatrix, data).getSumSquaredErrors();
					if (regressionError < error) {
						if (Parameters.DEBUG) {
							System.out.println("Fehler: " + error + " Kanten: " + graph.getEdgeCount()
									+ " Graphkosten: " + graphCosts);
						}
						error = regressionError;
					} else {
						graph.removeEdge(edge);
					}
					graphCosts = Methods.codingCostsGraph(graph);
				}
			}
		}
		return graph;
	}

	public Graph<Instance, MyLink> getGraph() {
		return graph;
	}
}
