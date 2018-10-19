package edu.stanford.math.mapper;

import edu.stanford.math.plex4.api.Plex4;
import edu.stanford.math.plex4.examples.PointCloudExamples;
import edu.stanford.math.plex4.graph.AbstractWeightedUndirectedGraph;
import edu.stanford.math.plex4.graph.io.GraphDotWriter;
import edu.stanford.math.plex4.metric.impl.EuclideanMetricSpace;
import edu.stanford.math.plex4.metric.interfaces.AbstractSearchableMetricSpace;
import edu.stanford.math.plex4.metric.landmark.MaxMinLandmarkSelector;
import edu.stanford.math.plex4.streams.filter.RandomProjectionFilterFunction;
import gnu.trove.TIntHashSet;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class MapperWitnessTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int nPts = 100000;
		//1000 in 295 ms
		//10k in 3.6 secs
        //20k in 10.4 seconds
        //30k in 22.1 secs
        //40k in 38.2 secs
        //50k in 71.6 secs
        //100k OOM

        int num_landmark_points = 100;
		long start = System.currentTimeMillis();
		//double[][] points = new double[n][2];
		//double[][] points = getImageVectors(n, "/home/dylan/Downloads/SampleData/SampleData/random_10000_data.csv");
        double[][] points = PointCloudExamples.getRandomCirclePoints(nPts);

        MaxMinLandmarkSelector landmarkSelector = Plex4.createMaxMinSelector(points, num_landmark_points);
        double[][] landMarkPoints = extractLandMarkPoints(landmarkSelector, points);

        System.out.println("Done processing the parsing in " + (System.currentTimeMillis()-start) + " millis");
        EuclideanMetricSpace metricSpace = new EuclideanMetricSpace(landMarkPoints);

		//RandomProjectionFilterFunction filter = new RandomProjectionFilterFunction(metricSpace);
		RandomProjectionFilterFunction filter = new RandomProjectionFilterFunction(metricSpace);
		MapperSpecifier specifier = MapperSpecifier.create().numIntervals(6).overlap(0.4).numHistogramBuckets(8);
		List<TIntHashSet> partialClustering = MapperPipeline.producePartialClustering(filter, metricSpace, specifier);
		AbstractWeightedUndirectedGraph graph = MapperPipeline.intersectionGraph(partialClustering);
		GraphDotWriter writer = new GraphDotWriter();
		long end = System.currentTimeMillis();
		try {
			writer.writeToFile(graph, "out.dot");
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Done processing the mapper in " + (end-start) + " millis");
	}

    private static double[][] extractLandMarkPoints(MaxMinLandmarkSelector landmarkSelector, double[][] points) {
        double[][] newPoints = new double[landmarkSelector.getLandmarkPoints().length][points[0].length];
        int row = 0;
	    for (int landmarkPoint : landmarkSelector.getLandmarkPoints()) {
            newPoints[row] = points[landmarkPoint];
            row++;
        }
        return newPoints;
    }

    public static double[][] getImageVectors(int n, String fileName) {
        double[][] points = new double[n][(28*28)+1];
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        int row = 0;
        try {
            br = new BufferedReader(new FileReader(fileName));
            while ((line = br.readLine()) != null) {
                String[] image = line.split(cvsSplitBy);
                for (int j = 0; j < image.length; j++) {
                    double dEntry = Double.parseDouble(image[j]);
                    points[row][j] = dEntry;
                }
                row++;
                if (row == n) {
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return points;
    }

}
