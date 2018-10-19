package edu.stanford.math.mapper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import edu.stanford.math.plex4.examples.PointCloudExamples;
import edu.stanford.math.plex4.graph.AbstractWeightedUndirectedGraph;
import edu.stanford.math.plex4.graph.io.GraphDotWriter;
import edu.stanford.math.plex4.metric.impl.EuclideanMetricSpace;
import edu.stanford.math.plex4.streams.filter.KernelDensityFilterFunction;
import edu.stanford.math.plex4.streams.filter.RandomProjectionFilterFunction;
import gnu.trove.TIntHashSet;

public class MapperTest {

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


		long start = System.currentTimeMillis();
		//double[][] points = new double[n][2];
		//double[][] points = getImageVectors(n, "/home/dylan/Downloads/SampleData/SampleData/random_10000_data.csv");
        double[][] points = PointCloudExamples.getRandomCirclePoints(n);

        System.out.println("Done processing the parsing in " + (System.currentTimeMillis()-start) + " millis");
		EuclideanMetricSpace metricSpace = new EuclideanMetricSpace(points);
		//RandomProjectionFilterFunction filter = new RandomProjectionFilterFunction(points);
        KernelDensityFilterFunction filter = new KernelDensityFilterFunction(metricSpace, 1.0);
		edu.stanford.math.mapper.MapperSpecifier specifier = MapperSpecifier.create().numIntervals(12).overlap(0.2).numHistogramBuckets(20);
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
