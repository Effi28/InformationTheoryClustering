package graphConstruction;

import java.util.HashMap;
import java.util.List;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import edu.uci.ics.jung.graph.Graph;
import other.Methods;
import other.MethodsKNN;
import other.MyLink;
import other.Pair;
import other.Parameters;
import weka.core.Instance;
import weka.core.Instances;

public class KNN {

	Graph<Instance, MyLink> graph;

	public DoubleMatrix2D start(Instances data, int k) {
		int n = data.numInstances();
		
		//approx of k
		if(k == 0){
			k  = (int) Math.log(n);			
		}
		
		// Create distanceMatrix of the dataset
		DoubleMatrix2D distanceMatrix = Methods.createDistanceMatrix(data);

		// Create graph from the given dataset
		graph = MethodsKNN.createGraph(data, distanceMatrix);

		// weight map
		HashMap<MyLink, Number> weights = new HashMap<>();

		for (int i = 0; i < n; i++) {
			List<Pair> orderOfAddedEdges = MethodsKNN.createOrderedList(distanceMatrix.viewColumn(i), i);
			for (int j = 0; j < k; j++) {
				int column = orderOfAddedEdges.get(j).getX();
				int row = orderOfAddedEdges.get(j).getY();
				double weight = Methods.fromDistanceToSimilartiy(orderOfAddedEdges.get(j).getValue());
				MyLink edge = new MyLink(weight);
				graph.addEdge(edge, data.instance(column), data.instance(row));
				weights.put(edge, edge.getWeight());
			}
		}
		if(Parameters.DEBUG){
			System.out.println("K" + k);			
			System.out.println("Instances " + data.numInstances());
			System.out.println("Edges " + graph.getEdgeCount());
		}

		// Constructs the adjacency matrix of the non constructed graph
		DoubleMatrix2D adjaMatrix = new SparseDoubleMatrix2D(n, n);
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (graph.findEdge(data.instance(i), data.instance(j)) != null) {
					adjaMatrix.set(i, j, Methods.fromDistanceToSimilartiy(distanceMatrix.get(i, j)));
				} else {
					if (i != j) {
						adjaMatrix.set(i, j, 0.01);
					}
				}
			}
		}
		return adjaMatrix;
	}

	public Graph<Instance, MyLink> getGraph() {
		return graph;
	}
}
