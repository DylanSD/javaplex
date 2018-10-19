package edu.stanford.math.mapper;

import edu.stanford.math.plex4.api.Plex4;
import edu.stanford.math.plex4.examples.PointCloudExamples;
import edu.stanford.math.plex4.graph.AbstractWeightedUndirectedGraph;
import edu.stanford.math.plex4.graph.io.GraphDotWriter;
import edu.stanford.math.plex4.homology.barcodes.BarcodeCollection;
import edu.stanford.math.plex4.homology.chain_basis.Simplex;
import edu.stanford.math.plex4.homology.interfaces.AbstractPersistenceAlgorithm;
import edu.stanford.math.plex4.io.BarcodeWriter;
import edu.stanford.math.plex4.metric.impl.EuclideanMetricSpace;
import edu.stanford.math.plex4.metric.landmark.MaxMinLandmarkSelector;
import edu.stanford.math.plex4.streams.filter.RandomProjectionFilterFunction;
import edu.stanford.math.plex4.streams.impl.LazyWitnessStream;
import gnu.trove.TIntHashSet;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class LazyWitnessTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int n = 1000;
		//1000 in 295 ms
		//10k in 3.6 secs
        //20k in 10.4 seconds
        //30k in 22.1 secs
        //40k in 38.2 secs
        //50k in 71.6 secs
        //100k OOM

        //So my idea here is to keep recursively applying the mapper algorithm until the
        //number of points to consider is tractable.
        //This could give visualization within visualizations. Which could be interesting.
        //Basically we need to cut-up all the search space.
        //Then repeat until the number of points in a region can be clustered effectively.


        int max_dimension = 3;
        int num_landmark_points = 50;
        int nu = 1;
        int num_divisions = 1000;

        long start = System.currentTimeMillis();
		//double[][] points = new double[n][2];
		//double[][] points = getImageVectors(n, "/home/dylan/Downloads/SampleData/SampleData/random_10000_data.csv");
        double[][] points = PointCloudExamples.getRandomSpherePoints(n, max_dimension - 1);

        System.out.println("Done processing the parsing in " + (System.currentTimeMillis()-start) + " millis");
		MaxMinLandmarkSelector landmarkSelector = Plex4.createMaxMinSelector(points, num_landmark_points);

        double R = landmarkSelector.getMaxDistanceFromPointsToLandmarks();
        double maxFiltrationVal = 2 * R;

        LazyWitnessStream stream = new LazyWitnessStream(landmarkSelector.getUnderlyingMetricSpace(), landmarkSelector, max_dimension, maxFiltrationVal, nu, num_divisions);
        stream.finalizeStream();

        //var num_simplices = stream.getSize();
        //var num_simplices = 12036          # Generally between 10000 and 25000

        AbstractPersistenceAlgorithm<Simplex> persistence =
                Plex4.getModularSimplicialAlgorithm(max_dimension, 2);
        BarcodeCollection<Double> intervals = persistence.computeIntervals(stream);

        BarcodeWriter barcodeWriter = BarcodeWriter.getInstance();

        //PersistenceInvariantDescriptor<Interval<Double>, G> object,
        // int dimension,
        // double endPoint,
        // String caption,
        // String path) throws IOException {

        try {
            barcodeWriter.writeToFile(intervals, 0, maxFiltrationVal, "Dimension 1", "barcode1.png");
            barcodeWriter.writeToFile(intervals, 1, maxFiltrationVal, "Dimension 2", "barcode2.png");
            barcodeWriter.writeToFile(intervals, 2, maxFiltrationVal, "Dimension 3", "barcode3.png");
        } catch (IOException e) {
            e.printStackTrace();
        }

        long end = System.currentTimeMillis();

		System.out.println("Done processing the mapper in " + (end-start) + " millis");
	}
}
