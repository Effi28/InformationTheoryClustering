package other;

import java.util.List;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.jfree.data.xy.XYSeries;
import cern.colt.matrix.DoubleMatrix2D;
import edu.uci.ics.jung.algorithms.matrix.GraphMatrixOperations;
import edu.uci.ics.jung.graph.Graph;
import view.XYView;
import weka.core.Instance;
import weka.core.Instances;

public class COGFDM {
	public XYSeries graphCosts = new XYSeries("GraphCosts+Error");
	public XYSeries nearestDist = new XYSeries("NearestDist");
	public XYSeries noHeur = new XYSeries("NoHeuristic");
	public XYSeries baselineCosts = new XYSeries("baselinecosts");
	double maxIter;
	Graph<Instance, MyLink> graph;

	public DoubleMatrix2D start(Instances data) {

		DoubleMatrix2D distanceMatrix = Methods.createDistanceMatrix(data);
		maxIter = distanceMatrix.columns() * 1.5;
		double baselineCosts = Methods.baselineMatrixNormal(distanceMatrix);

		graph = Methods.createGraph(data, distanceMatrix);
		double initialError = Methods.Regression(graph, distanceMatrix, data)
				.getSumSquaredErrors();

		if (Parameters.DEBUG) {
			System.out.println("BaselineCosts: " + baselineCosts);
			System.out.println("InitialError: " + initialError);
			System.out.println("Instances " + data.numInstances());
			System.out.println("Edges " + graph.getEdgeCount());
		}

		DoubleMatrix2D adjacencyM = GraphMatrixOperations
				.graphToSparseMatrix(graph);
		DoubleMatrix2D adjacencyMNoHeur = adjacencyM.copy();

		double graphCosts = Methods.codingCostsGraph(graph);
		this.graphCosts.add(graph.getEdgeCount(), graphCosts + initialError);

		graph = nearestDistHeuristic(data, baselineCosts, distanceMatrix, graph,
				initialError, adjacencyM);

		for (int i = 0; i < graph.getEdgeCount() + 1; i++) {
			this.baselineCosts.add(i, baselineCosts);
		}

		Graph<Instance, MyLink> graphNoHeur = Methods.createGraph(data,
				distanceMatrix);
		graphNoHeur = noHeuristic(data, baselineCosts, distanceMatrix,
				graphNoHeur, initialError, adjacencyMNoHeur);
		XYView viewError = new XYView();
		viewError.start(this.noHeur, this.nearestDist, "EdgesConnected",
				"RegressionError", "RegressionError");
		XYView viewCosts = new XYView();
		viewCosts.start(this.baselineCosts, this.graphCosts, "Points/Edges",
				"CodingCosts", "CodingCosts");

		return adjacencyM;
	}

	private Graph<Instance, MyLink> noHeuristic(Instances data,
			double baselineCosts, DoubleMatrix2D distanceMatrix,
			Graph<Instance, MyLink> graph, double initialError,
			DoubleMatrix2D adjacencyM) {
		double graphCosts = Methods.codingCostsGraph(graph);
		double error = initialError;
		noHeur.add(graph.getEdgeCount(), initialError);
		while (graphCosts + error < baselineCosts) {
			if (graph.getEdgeCount() >= maxIter) {
				return graph;
			}
			Pair p = searchHighestErrorReducer(data, distanceMatrix, graph,
					adjacencyM, error);
			if (p != null) {
				graph.addEdge(new MyLink(1), data.instance(p.getX()),
						data.instance(p.getY()));
				adjacencyM.set(p.getX(), p.getY(), 1);
				adjacencyM.set(p.getY(), p.getX(), 1);
				graphCosts = Methods.codingCosts(adjacencyM);
				System.out.println("noheuristic Fehler: " + p.getValue()
						+ " Kanten: " + graph.getEdgeCount() + " Graphkosten: "
						+ graphCosts);
				noHeur.add(graph.getEdgeCount(), p.getValue());
				error = p.getValue();
			} else {
				return graph;
			}
		}
		return graph;
	}

	private Pair searchHighestErrorReducer(Instances data,
			DoubleMatrix2D distanceMatrix, Graph<Instance, MyLink> graph,
			DoubleMatrix2D adjacencyM, double maxError) {
		Graph<Instance, MyLink> tempGraph = Methods.cloneGraph(graph);
		DoubleMatrix2D tempMatrix = adjacencyM.copy();

		Pair p = null;
		for (int i = 0; i < data.numInstances(); i++) {
			for (int j = i + 1; j < data.numInstances(); j++) {
				if (tempMatrix.get(i, j) == 0.0) {
					MyLink tempEdge = new MyLink(1);
					tempGraph.addEdge(tempEdge, data.instance(i),
							data.instance(j));
					tempMatrix.set(i, j, 1);
					tempMatrix.set(j, i, 1);

					double error = Methods
							.Regression(tempGraph, distanceMatrix, data)
							.getSumSquaredErrors();
					if (error < maxError) {
						p = new Pair(i, j, error);
						maxError = error;
					}
					tempGraph = Methods.cloneGraph(graph);
					tempMatrix = adjacencyM.copy();
				}
			}
		}
		return p;
	}

	private Graph<Instance, MyLink> nearestDistHeuristic(Instances data,
			double baselineCosts, DoubleMatrix2D distanceMatrix,
			Graph<Instance, MyLink> graph, double initialError,
			DoubleMatrix2D adjacencyM) {

		// Construct a ordererd List with the edges which are next inserted
		List<Pair> orderedEdges = Methods.createOrderedList(distanceMatrix);
		double error = initialError;
		double graphCosts = Methods.codingCostsGraph(graph);
		this.nearestDist.add(graph.getEdgeCount(), initialError);
		this.graphCosts.add(graph.getEdgeCount(), graphCosts + initialError);

		for (int i = 0; graphCosts + error < baselineCosts
				&& i < orderedEdges.size(); i++) {

			int column = orderedEdges.get(i).getX();
			int row = orderedEdges.get(i).getY();
			MyLink edge = new MyLink(orderedEdges.get(i).getValue());
			if (graph.getEdgeCount() >= maxIter) {
				return graph;
			}
			if (!graph.isNeighbor(data.instance(column), data.instance(row))) {
				graph.addEdge(edge, data.instance(column), data.instance(row));
				adjacencyM.set(row, column, 1);
				adjacencyM.set(column, row, 1);
				SimpleRegression regressionNew = Methods.Regression(graph,
						distanceMatrix, data);
				if (regressionNew.getSumSquaredErrors() < error) {
					graphCosts = Methods.codingCosts(adjacencyM);

					this.graphCosts.add(graph.getEdgeCount(),
							graphCosts + error);
					this.nearestDist.add(graph.getEdgeCount(),
							regressionNew.getSumSquaredErrors());
					System.out.println("Fehler: " + error + " Kanten: "
							+ graph.getEdgeCount() + " Graphkosten: "
							+ graphCosts);
					error = regressionNew.getSumSquaredErrors();
				} else {
					adjacencyM.set(row, column, 0);
					adjacencyM.set(column, row, 0);
					graph.removeEdge(edge);
				}
			}
		}
		return graph;
	}

	public Graph<Instance, MyLink> getGraph() {
		return graph;
	}
}
