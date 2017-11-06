package other;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.WeibullDistribution;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.jet.math.Arithmetic;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance;
import edu.uci.ics.jung.algorithms.shortestpath.PrimMinimumSpanningTree;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import umontreal.iro.lecuyer.probdist.WeibullDist;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.NormalizableDistance;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.attribute.PrincipalComponents;

final public class Methods {

	public static DoubleMatrix2D createDistanceMatrix(Instances data) {
		DoubleMatrix2D res = new DenseDoubleMatrix2D(data.numInstances(),
				data.numInstances());
		NormalizableDistance e = new EuclideanDistance(data);
		e.setDontNormalize(false);
		for (int i = 0; i < data.numInstances(); i++) {
			for (int j = i + 1; j < data.numInstances(); j++) {
				res.set(i, j, e.distance(data.instance(i), data.instance(j)));
				res.set(j, i, e.distance(data.instance(i), data.instance(j)));
			}
		}
		return res;
	}

	public static Graph<Instance, MyLink> createGraph(Instances data,
			DoubleMatrix2D distanceMatrix) {
		return createGraph(data, distanceMatrix, 0.2);
	}

	public static Graph<Instance, MyLink> createGraph(Instances data,
			DoubleMatrix2D distanceMatrix, double loadingFactor) {
		Graph<Instance, MyLink> res = new UndirectedSparseGraph<>();
		// add vertices
		for (int i = 0; i < data.numInstances(); i++) {
			res.addVertex(data.instance(i));
		}
		// add edges
		for (int i = 0; i < data.numInstances(); i++) {
			for (int j = i + 1; j < data.numInstances(); j++) {
				if (Math.random() <= loadingFactor) {
					res.addEdge(new MyLink(distanceMatrix.get(i, j)),
							data.instance(i), data.instance(j));
				}
			}
		}
		// boolean connected = true;
		// for (Instance i : res.getVertices()) {
		// if (res.degree(i) > 0) {
		// connected = true;
		// } else {
		// connected = false;
		// break;
		// }
		// }
		// System.out.println("connected :" + connected);

		Factory<UndirectedGraph<Instance, MyLink>> graphFactory = UndirectedSparseGraph
				.getFactory();
		PrimMinimumSpanningTree<Instance, MyLink> msf = new PrimMinimumSpanningTree<Instance, MyLink>(
				graphFactory, getTransformer());
		res = msf.transform(res);

		return res;
	}

	private static Transformer<MyLink, Double> getTransformer() {
		Transformer<MyLink, Double> wtTransformer = new Transformer<MyLink, Double>() {
			public Double transform(MyLink link) {
				return link.getWeight();
			}
		};
		return wtTransformer;
	}

	public static double codingCosts(DoubleMatrix2D aMatrix) {
		int oneAmount = aMatrix.cardinality();
		int totalAmount = aMatrix.columns() * aMatrix.rows();
		int zeroAmount = totalAmount - oneAmount;
		if (totalAmount == zeroAmount || totalAmount == oneAmount) {
			return 0;
		}
		double res = zeroAmount * Arithmetic.log2(totalAmount / zeroAmount)
				+ oneAmount * Arithmetic.log2(totalAmount / oneAmount);

		int n = aMatrix.columns();
		int nQ = aMatrix.columns() * aMatrix.columns();
		double temp = n * (n - 1) / 2;
		double temp1 = temp / nQ;
		return res * temp1;
	}

	public static List<Pair> createOrderedList(DoubleMatrix2D distanceMatrix) {

		List<Pair> orderOfAddedEdges = new ArrayList<>();
		for (int i = 0; i < distanceMatrix.columns(); i++) {
			for (int j = i + 1; j < distanceMatrix.rows(); j++) {
				orderOfAddedEdges.add(new Pair(i, j, distanceMatrix.get(i, j)));
			}
		}
		Collections.sort(orderOfAddedEdges);
		return orderOfAddedEdges;
	}

	public static SimpleRegression Regression(Graph<Instance, MyLink> graph,
			DoubleMatrix2D distanceMatrix, Instances data) {

		DijkstraDistance<Instance, MyLink> alg = new DijkstraDistance<Instance, MyLink>(
				graph, getTransformer(), true);
		SimpleRegression regression = new SimpleRegression();
		for (int i = 0; i < distanceMatrix.rows(); i++) {
			for (int j = i + 1; j < distanceMatrix.columns(); j++) {
				Number pathDistance = alg.getDistance(data.instance(i),
						data.instance(j));
				double realDistance = distanceMatrix.get(i, j);
				regression.addData(pathDistance.doubleValue(), realDistance);
			}
		}
		return regression;
	}

	public static Instances doPCA(Instances data) {
		PrincipalComponents p = new PrincipalComponents();
		String[] s = { "0.95", "2", "2" };
		try {
			p.setInputFormat(data);
			p.setOptions(s);
			data = Filter.useFilter(data, p);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

	public static Instances doNormalize(Instances data) {
		Normalize normalize = new Normalize();
		Instances newData = null;
		String[] options = { "-S", "1000.0", "-T", "1000.0" };
		try {
			normalize.setOptions(options);
			normalize.setInputFormat(data);
			newData = Filter.useFilter(data, normalize);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newData;
	}

	public static Instances normalize(Instances data) {
		for (int j = 0; j < data.numAttributes(); j++) {
			double min = data.attributeStats(j).numericStats.min;
			double max = data.attributeStats(j).numericStats.max;
			for (int i = 0; i < data.numInstances(); i++) {
				double oldVal = data.instance(i).value(j);
				double newVal = 1 - ((max - oldVal) / (max - min));
				data.instance(i).setValue(j, newVal);
			}
		}
		return data;
	}

	public static double baselineMatrixNormal(DoubleMatrix2D distanceMatrix) {
		int n = distanceMatrix.columns();
		int nQ = n * n;
		double temp = (n * (n - 1) / 2);
		double fact = temp / nQ;

		double[] arr = new double[(int) (nQ * fact)];
		int count = 0;
		for (int i = 0; i < distanceMatrix.columns(); i++) {
			for (int j = i + 1; j < distanceMatrix.rows(); j++) {
				arr[count] = distanceMatrix.get(i, j);
				count++;
			}
		}
		// normal
		double mean = Methods.mean(arr);
		double var = Methods.var(mean, arr);
		double sd = Math.sqrt(var);
		NormalDistribution dist = new NormalDistribution(mean, sd);
		double vac = 0;
		for (int i = 0; i < arr.length; i++) {
			double density = dist.density(arr[i]);
			if (density > 1) {
				density = 1;
			}
			if (density < 0) {
				density = 0.01;
			}
			vac += Arithmetic.log2(1 / density);
		}

		return vac;
	}

	public static double baselineMatrixWeibull(DoubleMatrix2D distanceMatrix) {
		int n = distanceMatrix.columns();
		int nQ = n * n;
		double temp = (n * (n - 1) / 2);
		double fact = temp / nQ;

		double[] arr = new double[(int) (nQ * fact)];
		int count = 0;
		for (int i = 0; i < distanceMatrix.columns(); i++) {
			for (int j = i + 1; j < distanceMatrix.rows(); j++) {
				arr[count] = distanceMatrix.get(i, j);
				count++;
			}
		}
		// weibull
		WeibullDist stat = WeibullDist.getInstanceFromMLE(arr, arr.length);
		WeibullDistribution dist = new WeibullDistribution(stat.getAlpha(),
				stat.getLambda());
		double vac = 0;
		for (int i = 0; i < arr.length; i++) {
			double density = dist.density(arr[i]);
			if (density > 1.0) {
				density = 1.0;
			}
			if (density <= 0.0) {
				density = 0.00001;
			}
			vac += Arithmetic.log2(1 / density);
		}
		return vac;
	}

	private static double mean(double[] arr) {
		double res = 0;
		for (double i : arr) {
			res += i;
		}
		return res / arr.length;
	}

	private static double var(double mean, double[] arr) {
		double res = 0;
		for (double a : arr)
			res += (mean - a) * (mean - a);
		return res / arr.length;
	}

	public static Graph<Instance, MyLink> cloneGraph(
			Graph<Instance, MyLink> graph) {
		Graph<Instance, MyLink> tempGraph = new SparseGraph<>();
		for (Instance i : graph.getVertices())
			tempGraph.addVertex(i);

		for (MyLink e : graph.getEdges())
			tempGraph.addEdge(e, graph.getIncidentVertices(e));
		return tempGraph;
	}

	public static DoubleMatrix2D fromDistanceToSimilarityMatrix(
			DoubleMatrix2D distanceAdjaMatrix, DoubleMatrix2D distanceMatrix) {
		for (int i = 0; i < distanceAdjaMatrix.columns(); i++) {
			for (int j = 0; j < distanceAdjaMatrix.rows(); j++) {
				if (distanceAdjaMatrix.get(j, i) != 0) {
					distanceAdjaMatrix.set(j, i, fromDistanceToSimilartiy1(
							distanceMatrix.get(j, i)));
				} else {
					distanceAdjaMatrix.set(j, i, 0.01);
				}
			}
		}
		return distanceAdjaMatrix;
	}

	public static double fromDistanceToSimilartiy1(double d) {
		return 1 / 1 + d;
	}

	public static double fromDistanceToSimilartiy(double d) {
		double sigma_sq = Parameters.getSIGMA() * Parameters.getSIGMA();
		double d_sq = d * d;
		double res = Math.exp(-d_sq / (2 * sigma_sq));
		return res;
	}

	public static HashMap<MyLink, Number> getWeights(
			Graph<Instance, MyLink> graph) {
		HashMap<MyLink, Number> weights = new HashMap<>();
		for (MyLink link : graph.getEdges()) {
			weights.put(link, link.getWeight());
		}
		return weights;
	}

	public static double codingCostsGraph(Graph<Instance, MyLink> graph) {
		int oneAmount = graph.getEdgeCount() * 2;
		int totalAmount = graph.getVertexCount() * graph.getVertexCount();
		int zeroAmount = totalAmount - oneAmount;
		if (totalAmount == zeroAmount || totalAmount == oneAmount) {
			return 0;
		}
		double res = zeroAmount * Arithmetic.log2(totalAmount / zeroAmount)
				+ oneAmount * Arithmetic.log2(totalAmount / oneAmount);

		int n = graph.getVertexCount();
		int nQ = n * n;
		double temp = n * (n - 1) / 2;
		double temp1 = temp / nQ;
		return res * temp1;
	}

	public static double getStd(DoubleMatrix2D distanceMatrix) {
		int n = distanceMatrix.columns();
		double mean = 0;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				mean += distanceMatrix.get(i, j);
			}
		}
		mean = mean / n;

		double var = 0;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				var += (mean - distanceMatrix.get(i, j))
						* (mean - distanceMatrix.get(i, j));
			}
		}
		var = var / n;
		return Math.sqrt(var);
	}

	public static double fromDistanceToSimilartiy2(double distance,
			double std) {
		return Math.exp(-Parameters.getBETA() * distance / std)
				+ Parameters.getEPS();
	}

	public static DoubleMatrix2D fromGraphToSimMatrix(Instances data,
			Graph<Instance, MyLink> graph, DoubleMatrix2D distanceMatrix) {
		int n = graph.getVertexCount();
		DoubleMatrix2D res = new SparseDoubleMatrix2D(n, n);
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (graph.findEdge(data.instance(i),
						data.instance(j)) != null) {
					double sim = Methods
							.fromDistanceToSimilartiy(distanceMatrix.get(i, j));
					res.set(i, j, sim);
					graph.findEdge(data.instance(i), data.instance(j))
							.setWeight(sim);
				} else {
					if (i != j) {
						res.set(i, j, 0);
					}
				}
			}
		}
		return res;
	}
}
