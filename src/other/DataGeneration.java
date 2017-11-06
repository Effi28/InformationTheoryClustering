package other;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.distribution.MultivariateNormalDistribution;

import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Attribute;
import weka.core.converters.ArffSaver;

public class DataGeneration {

	public static final int sampleSize = 100;
	public static final int dimensions = 2;
	public static final double balancingFactor = 0.5;

	public static void main(String[] args) {

		List<double[]> meansOfDistributions = new ArrayList<>();
		double means[] = { 2.0, 2.0 };
		double means1[] = { -2.0, -2.0 };
		meansOfDistributions.add(means);
		meansOfDistributions.add(means1);

		List<MultivariateNormalDistribution> distributions = caclulateDistributions(meansOfDistributions);
		FastVector atts = new FastVector();
		Attribute x = new Attribute("x", 0);
		Attribute y = new Attribute("y", 1);
		Attribute class1 = new Attribute("class", 2);

		atts.addElement(x);
		atts.addElement(y);
		atts.addElement(class1);

		List<Instance> instances = new ArrayList<Instance>();

		for (int obj = 0; obj < sampleSize; obj++) {
			instances.add(new Instance(dimensions + 1));
		}

		// Fill the value of dimension "dim" into each object
		for (int obj = 0; obj < sampleSize; obj++) {

			if (Math.random() > balancingFactor) {
				instances.get(obj).setValue(0, distributions.get(0).sample()[0]);
				instances.get(obj).setValue(1, distributions.get(0).sample()[1]);
				instances.get(obj).setValue(2, 1.0);
			} else {
				instances.get(obj).setValue(0, distributions.get(1).sample()[0]);
				instances.get(obj).setValue(1, distributions.get(1).sample()[1]);
				instances.get(obj).setValue(2, 0.0);
			}
		}

		for (int obj = sampleSize; obj < sampleSize + 14; obj++) {
			instances.add(new Instance(dimensions + 1));
		}
				
		instances.get(101).setValue(0, -2.9);
		instances.get(101).setValue(1, 4);
		instances.get(101).setValue(2, 2);
		
		instances.get(102).setValue(0, -4);
		instances.get(102).setValue(1, 2.8);
		instances.get(102).setValue(2, 2);
		
		instances.get(103).setValue(0, -0.9);
		instances.get(103).setValue(1, 4);
		instances.get(103).setValue(2, 2);
		
		instances.get(104).setValue(0, 3.7);
		instances.get(104).setValue(1, 4.9);
		instances.get(104).setValue(2, 2);
		
		instances.get(105).setValue(0, 4.9);
		instances.get(105).setValue(1, -2.2);
		instances.get(105).setValue(2, 2);
		
		instances.get(106).setValue(0, 3.1);
		instances.get(106).setValue(1, 2.0);
		instances.get(106).setValue(2, 2);
		
		instances.get(107).setValue(0, 4.9);
		instances.get(107).setValue(1, -2.2);
		instances.get(107).setValue(2, 2);
		
		instances.get(108).setValue(0, 4.5);
		instances.get(108).setValue(1, 4.5);
		instances.get(108).setValue(2, 2);
		
		instances.get(109).setValue(0, -2.5);
		instances.get(109).setValue(1, 3);
		instances.get(109).setValue(2, 2);
		
		instances.get(110).setValue(0, 0.3);
		instances.get(110).setValue(1, -4);
		instances.get(110).setValue(2, 2);
		
		instances.get(111).setValue(0, 0.4);
		instances.get(111).setValue(1, -4.2);
		instances.get(111).setValue(2, 2);
		
		instances.get(112).setValue(0, 0.4);
		instances.get(112).setValue(1, -3);
		instances.get(112).setValue(2, 2);

		instances.get(112).setValue(0, 2);
		instances.get(112).setValue(1, -3);
		instances.get(112).setValue(2, 2);
		
		// Create new dataset
		Instances newDataset = new Instances("Dataset", atts, instances.size());
		newDataset.setClassIndex(2);

		// Fill in data objects
		for (Instance inst : instances)
			newDataset.add(inst);

		ArffSaver saver = new ArffSaver();
		saver.setInstances(newDataset);
		try {
			saver.setFile(new File("./data/2gaussianNoise.arff"));
			saver.writeBatch();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static List<MultivariateNormalDistribution> caclulateDistributions(List<double[]> meansOfDistributions) {
		List<MultivariateNormalDistribution> distributions = new ArrayList<>();
		for (int i = 0; i < meansOfDistributions.size(); i++) {
			int length = meansOfDistributions.get(i).length;
			double var[][] = new double[length][length];
			var[0][0] = 1;
			var[0][1] = 0;
			var[1][0] = 0;
			var[1][1] = 1;
			MultivariateNormalDistribution dist = new MultivariateNormalDistribution((meansOfDistributions.get(i)),
					var);
			distributions.add(dist);
		}
		return distributions;
	}
}
