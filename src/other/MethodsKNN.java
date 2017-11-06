package other;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import weka.core.Instance;
import weka.core.Instances;

public class MethodsKNN {
	public static List<Pair> createOrderedList(DoubleMatrix1D column, int i) {

		LinkedHashSet<Pair> orderOfAddedEdges = new LinkedHashSet<>();

		for (int j = 0; j < column.size(); j++) {
			if (i != j) {
				orderOfAddedEdges.add(new Pair(i, j, column.get(j)));
			}
		}

		ArrayList<Pair> orderdListofEdges = new ArrayList<>();
		orderdListofEdges.addAll(orderOfAddedEdges);

		Collections.sort(orderdListofEdges);
		return orderdListofEdges;
	}

	public static Graph<Instance, MyLink> createGraph(Instances data, DoubleMatrix2D distanceMatrix) {
		Graph<Instance, MyLink> res = new SparseGraph<>();
		// add vertices
		for (int i = 0; i < data.numInstances(); i++) {
			res.addVertex(data.instance(i));
		}
		return res;
	}
}
