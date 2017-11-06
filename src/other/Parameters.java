package other;

import java.io.File;

final public class Parameters {

	// data set
	private static File DATASET = new File("data/2gaussian.arff");

	public static final boolean DEBUG = false;
	
	public static boolean finished = false;

	// choose algorithm self impl = MDL; knn = KNN; e-range = ERANGE; knnImpr =
	// KNNMDL;
	private static String ALGORITHM = "knn";

	// knn
	private static int K = 0;

	// e-range
	private static double EPSILON = 0;

	// spectral clustering
	private static double SIGMA = 1;

	private static int NUMCLUSTERS = 2;

	// sim function
	private static int BETA = 2;
	private static double EPS = 10E-3;

	public static File getDATASET() {
		return DATASET;
	}

	public static void setDATASET(File dATASET) {
		DATASET = dATASET;
	}

	public static String getALGORITHM() {
		return ALGORITHM;
	}

	public static void setALGORITHM(String aLGORITHM) {
		ALGORITHM = aLGORITHM;
	}

	public static int getK() {
		return K;
	}

	public static void setK(int k) {
		K = k;
	}

	public static double getEPSILON() {
		return EPSILON;
	}

	public static void setEPSILON(double ePSILON) {
		EPSILON = ePSILON;
	}

	public static double getSIGMA() {
		return SIGMA;
	}

	public static void setSIGMA(double sIGMA) {
		SIGMA = sIGMA;
	}

	public static int getNUMCLUSTERS() {
		return NUMCLUSTERS;
	}

	public static void setNUMCLUSTERS(int nUMCLUSTERS) {
		NUMCLUSTERS = nUMCLUSTERS;
	}

	public static int getBETA() {
		return BETA;
	}

	public static void setBETA(int bETA) {
		BETA = bETA;
	}

	public static double getEPS() {
		return EPS;
	}

	public static void setEPS(double ePS) {
		EPS = ePS;
	}

}
