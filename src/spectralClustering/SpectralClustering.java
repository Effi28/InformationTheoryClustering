package spectralClustering;

import java.util.ArrayList;
import java.util.List;
import cern.colt.list.IntArrayList;
import cern.colt.map.AbstractIntDoubleMap;
import cern.colt.map.OpenIntDoubleHashMap;
import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import cern.jet.math.Functions;
import other.Parameters;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class SpectralClustering {

	/**
	 * Returns the best cut of a graph w.r.t. the degree of dissimilarity
	 * between points of different partitions and the degree of similarity
	 * between points of the same partition.
	 * 
	 * @param W
	 *            the weight matrix of the graph
	 * @return an array of two elements, each of these contains the points of a
	 *         partition
	 * @throws Exception
	 */
	public int[] bestCut(final DoubleMatrix2D W) throws Exception {
		int n = W.columns();
		
		 // Builds the diagonal matrices D
		 final DoubleMatrix1D d = DoubleFactory1D.dense.make(n);
		 for (int i = 0; i < n; i++) {
		 double d_i = W.viewRow(i).zSum();
		 d.set(i, d_i);
		 }
		 final DoubleMatrix2D D = DoubleFactory2D.sparse.diagonal(d);
		 final DoubleMatrix2D X = D.copy();
		 // X = D - W
		 X.assign(W, Functions.minus);

		// Computes the eigenvalues and the eigenvectors of X
		final EigenvalueDecomposition e = new EigenvalueDecomposition(X);
		final DoubleMatrix1D lambda = e.getRealEigenvalues();
		// Selects the eigenvector z_2 associated with the second smallest
		// eigenvalue
		// Creates a map that contains the pairs <index, eigevalue>

		final AbstractIntDoubleMap map = new OpenIntDoubleHashMap(n);
		for (int i = 0; i < n; i++)
			map.put(i, Math.abs(lambda.get(i)));
		final IntArrayList list = new IntArrayList();
		// Sorts the map on the value
		map.keysSortedByValue(list);
		// Gets the index of the second smallest element
		DenseDoubleMatrix2D res = new DenseDoubleMatrix2D(n, Parameters.getNUMCLUSTERS());
		for (int i = 0; i < Parameters.getNUMCLUSTERS(); i++) {
			final DoubleMatrix1D y_2 = e.getV().viewColumn(i).copy();
			for (int j = 0; j < y_2.size(); j++) {
				res.set(j, i, y_2.get(j));
			}
		}

		FastVector atts = new FastVector();
		Attribute x = new Attribute("x", 0);
		Attribute y = new Attribute("y", 1);
		atts.addElement(x);
		atts.addElement(y);
		Instances newDataset = new Instances("Dataset", atts, n);
		List<Instance> instances = new ArrayList<Instance>();
		for (int obj = 0; obj < n; obj++) {
			instances.add(new Instance(2));
		}
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < 2; j++) {
				instances.get(i).setValue(j, res.get(i, j));
			}
		}
		for (Instance inst : instances) {
			newDataset.add(inst);
		}
		SimpleKMeans p = new SimpleKMeans();
		try {
			p.setNumClusters(Parameters.getNUMCLUSTERS());
			p.setPreserveInstancesOrder(true);
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		p.buildClusterer(newDataset);
		return p.getAssignments();
	}
}
