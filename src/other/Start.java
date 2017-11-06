package other;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import cern.colt.matrix.DoubleMatrix2D;
import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;
import graphConstruction.MDLGraph;
import graphConstruction.ERange;
import graphConstruction.KNN;
import graphConstruction.MDLKNN;
import spectralClustering.SpectralClustering;
import view.ClusterView;
import view.GraphView;
import weka.classifiers.evaluation.ConfusionMatrix;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.evaluation.TwoClassStats;
import weka.core.Instance;
import weka.core.Instances;

public class Start implements Runnable {
	static Instances data;
	static Graph<Instance, MyLink> graph;

	public void run() {
		data = read(Parameters.getDATASET());

		if (data.numAttributes() > 3) {
			data = Methods.doPCA(data);
		}
		try {
			// Calc graph
			DoubleMatrix2D ajdaMatr = calculateGraph(Parameters.getALGORITHM());

			// Do Spectral clustering
			SpectralClustering sp = new SpectralClustering();
			int[] ass = sp.bestCut(ajdaMatr);

			Instances clusteredData = new Instances(data);
			String[] classes = new String[Parameters.getNUMCLUSTERS()];
			for (int i = 0; i < classes.length; i++) {
				classes[i] = i + "";
			}
			for (int i = 0; i < clusteredData.numInstances(); i++) {
				clusteredData.instance(i).setClassValue(ass[i]);
			}

			// Cluster View
			ClusterView clView = new ClusterView();
			clView.visualize(clusteredData);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Evaluation graph
		// evaluateGraph();
		// Evaluation cluster
		// evaluateCluster(ass, clusteredData);
	}

	private void evaluateGraph() {
		double rightOnes = 0;
		double falseOnes = 0;
		double falseSimWeight = 0;
		double rightSimWeight = 0;

		for (MyLink edge : graph.getEdges()) {
			Pair<Instance> pair = graph.getEndpoints(edge);
			if (pair.getFirst().classValue() == pair.getSecond().classValue()) {
				rightOnes++;
				rightSimWeight += Methods
						.fromDistanceToSimilartiy(edge.getWeight());
			} else {
				falseOnes++;
				falseSimWeight += Methods
						.fromDistanceToSimilartiy(edge.getWeight());
			}
		}

		System.out.println(System.lineSeparator());
		System.out.println("GRAPH EVALUATION");

		System.out.println(falseOnes + " edges between classes");
		System.out.println(rightOnes + " edges within classes");
		System.out.println(falseSimWeight + " weights between classes");
		System.out.println(rightSimWeight + " weights within classes");
		WeakComponentClusterer<Instance, MyLink> w = new WeakComponentClusterer<>();
		Graph<Instance, MyLink> graphCopy = graph;

		System.out.println(
				"connected components" + w.transform(graphCopy).size());
	}

	private static void evaluateCluster(int[] ass, Instances newData)
			throws Exception {
		boolean changeClassNewData = false;
		for (int i = 0; i < newData.numInstances(); i++) {
			if (newData.instance(i).classValue() == Parameters
					.getNUMCLUSTERS()) {
				changeClassNewData = true;
			}
		}

		boolean changeClassOldData = false;
		for (int i = 0; i < data.numInstances(); i++) {
			if (data.instance(i).classValue() == Parameters.getNUMCLUSTERS()) {
				changeClassOldData = true;
			}
		}

		if (changeClassNewData) {
			for (int i = 0; i < newData.numInstances(); i++) {
				newData.instance(i)
						.setClassValue(newData.instance(i).classValue() - 1);
			}
		}

		if (changeClassOldData) {
			for (int i = 0; i < data.numInstances(); i++) {
				data.instance(i)
						.setClassValue(data.instance(i).classValue() - 1);
			}
		}

		String[] classes = new String[Parameters.getNUMCLUSTERS()];
		for (int i = 0; i < classes.length; i++) {
			classes[i] = i + "";
		}
		// for (int i = 0; i < data.numInstances(); i++) {
		// if (data.instance(i).classValue() == 0.0) {
		// data.instance(i).setClassValue(1.0);
		// } else {
		// data.instance(i).setClassValue(0.0);
		// }
		// }

		ConfusionMatrix m = new ConfusionMatrix(classes);
		for (int i = 0; i < newData.numInstances(); i++) {
			newData.instance(i).setClassValue(ass[i]);
			double prediction = newData.instance(i).classValue();
			double actual = data.instance(i).classValue();
			double[] distri = NominalPrediction.makeDistribution(prediction,
					Parameters.getNUMCLUSTERS());
			NominalPrediction nP = new NominalPrediction(actual, distri);
			m.addPrediction(nP);
		}

		System.out.println();
		System.out.println("CLUSTER EVALUATION");
		System.out.println();
		for (int i = 0; i < m.size(); i++) {
			TwoClassStats stats = m.getTwoClassStats(i);
			System.out.println("Class " + i);
			System.out.println();
			System.out.println("Recall " + stats.getRecall());
			System.out.println("Precision " + stats.getPrecision());
			System.out.println("FMeasure " + stats.getFMeasure());

		}
	}

	public static DoubleMatrix2D calculateGraph(String alg) throws Exception {
		GraphView gui = new GraphView();
		DoubleMatrix2D matrix = null;
		if ("MDL".equals(alg)) {
			MDLGraph cogfdm = new MDLGraph();
			matrix = cogfdm.start(data);
			graph = cogfdm.getGraph();
		} else if ("KNN".equals(alg)) {
			KNN knn = new KNN();
			matrix = knn.start(data, Parameters.getK());
			graph = knn.getGraph();
		} else if ("ERANGE".equals(alg)) {
			ERange eRange = new ERange();
			matrix = eRange.start(data, Parameters.getEPSILON());
			graph = eRange.getGraph();
		} else if ("KNNMDL".equals(alg)) {
			MDLKNN knn = new MDLKNN();
			matrix = knn.start(data);
			graph = knn.getGraph();
		}

		if (graph == null) {
			throw new Exception("GRAPH IS NULL");
		}
		gui.visualize(graph);
		return matrix;
	}

	public static Instances read(File path) {
		BufferedReader reader;
		Instances data = null;
		try {
			reader = new BufferedReader(new FileReader(path));
			data = new Instances(reader);
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		data.setClassIndex(data.numAttributes() - 1);
		return data;
	}

	// private static void startClustering1(Instances data2) throws Exception {
	// CKVWSpectralClustering06 sc = new CKVWSpectralClustering06();
	// Properties prop = new Properties();
	// int n = data2.numInstances();
	// double[][] data = new double[n][2];
	// for (int i = 0; i < n; i++) {
	// Instance inst = data2.instance(i);
	// data[i][0] = data2.instance(i).value(0);
	// data[i][1] = data2.instance(i).value(1);
	// }
	// prop.put("edu.ucla.sspace.clustering.CKVWSpectralClustering03" +
	// ".useKMeans", true);
	// Matrix matrix = new ArrayMatrix(data);
	// Assignments ass = sc.cluster(matrix, Parameters.NUMCLUSTERS, prop);
	//
	// Instances newData = new Instances(data2);
	// for (int i = 0; i < n; i++) {
	// newData.instance(i).setValue(2, ass.get(i).assignments()[0]);
	// }
	// VisualizePanel p = new VisualizePanel();
	// PlotData2D plot = new PlotData2D(newData);
	// p.addPlot(plot);
	// p.setXIndex(0);
	// p.setYIndex(1);
	//
	// p.setColourIndex(2);
	//
	// JFrame frame = new JFrame();
	// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	// frame.getContentPane().add(p);
	// frame.setLocationRelativeTo(null);
	// frame.pack();
	// frame.setVisible(true);
	//
	// }
	// private static void startClustering(Instances data) throws Exception {
	// SpectralClusterer cl = new SpectralClusterer();
	// try {
	// cl.setAlphaStar(Parameters.ALPHASTAR);
	// cl.setR(Parameters.R);
	// cl.setSigma(Parameters.SIGMA);
	// cl.buildClusterer(data);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// int[] result = cl.cluster;
	// System.out.println("Cluster amount found:" + cl.numOfClusters);
	//
	// Instances newData = new Instances(data);
	// for (int i = 0; i < data.numInstances(); i++) {
	// newData.instance(i).setValue(2, result[i]);
	// }
	// VisualizePanel p = new VisualizePanel();
	// PlotData2D plot = new PlotData2D(newData);
	// p.addPlot(plot);
	// p.setXIndex(0);
	// p.setYIndex(1);
	//
	// p.setColourIndex(2);
	//
	// JFrame frame = new JFrame();
	// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	// frame.getContentPane().add(p);
	// frame.setLocationRelativeTo(null);
	// frame.pack();
	// frame.setVisible(true);
	// }
}
