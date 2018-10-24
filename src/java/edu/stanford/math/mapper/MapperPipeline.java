package edu.stanford.math.mapper;

import edu.stanford.math.clustering.DisjointSetSystem;
import edu.stanford.math.clustering.HierarchicalClustering;
import edu.stanford.math.clustering.SLINK;
import edu.stanford.math.clustering.SingleLinkageClustering;
import edu.stanford.math.plex4.graph.AbstractWeightedUndirectedGraph;
import edu.stanford.math.plex4.graph.UndirectedWeightedListGraph;
import edu.stanford.math.plex4.homology.barcodes.Interval;
import edu.stanford.math.plex4.homology.utility.HomologyUtility;
import edu.stanford.math.plex4.metric.interfaces.AbstractIntMetricSpace;
import edu.stanford.math.plex4.streams.filter.IntFilterFunction;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapperPipeline {

    private static ExecutorService executorService = Executors.newCachedThreadPool();

	public static List<TIntHashSet> producePartialClustering(IntFilterFunction filter, AbstractIntMetricSpace metricSpace, edu.stanford.math.mapper.MapperSpecifier specifier) {

        List<TIntHashSet> mapperCover = new ArrayList<TIntHashSet>();
        Iterable<Interval<Double>> rangeCover = RangeCoverUtility.createUniformIntervalCover(filter, specifier.numIntervals, specifier.getOverlap());

        SetCover1D domainCover = new SetCover1D(filter, rangeCover);
        int count = 0;
        for (TIntHashSet set : domainCover) {
            count++;
        }
        //System.out.println("Processing " + count + " sets in parallel");
        //CountDownLatch cdl = new CountDownLatch(count);
        for (TIntHashSet set : domainCover) {
//		    Runnable runnable = new Runnable() {
//                @Override
            //               public void run() {
// construct sub-metric space
            AbstractIntMetricSpace subMetricSpace = edu.stanford.math.mapper.MetricUtility.createSubMetricSpace(metricSpace, set);

            // run clustering on subset
            SingleLinkageClustering clustering = new SingleLinkageClustering(subMetricSpace);
            //SLINK clustering = new SLINK(subMetricSpace);

            // get merge times for clustering tree
            double[] mergeTimes = clustering.getMergedDistances();

            // construct histogram on merge times
            HistogramCreator histogram = new HistogramCreator(mergeTimes, specifier.numHistogramBuckets);

            // select last bin for which histogram is zero
            int lastZeroBinIndex = histogram.getLastZeroBinIndex();

            // obtain distance cutoff corresponding to zero-bin
            double distanceCutoff = histogram.getBinStartPoint(lastZeroBinIndex);

            // get clusters by thresholding linkage tree
            DisjointSetSystem setSystem = clustering.thresholdByDistance(distanceCutoff);

            // convert clusters to list of sets
            List<TIntHashSet> clusters = HierarchicalClustering.getImpliedClustersTrove(setSystem);
            //System.out.println("Clusters: "  + clusters.size());
            for (TIntHashSet subCluster : clusters) {
                //System.out.println("Cluster size: "  + subCluster.size());
                synchronized (mapperCover) {
                    mapperCover.add(MapperPipeline.pullUpIndices(subCluster, set));
                }
            }
            //cdl.countDown();
        }
        System.out.println("Mapper cover size: "  + mapperCover.size());
        return mapperCover;
	}

	public static AbstractWeightedUndirectedGraph intersectionGraph(List<TIntHashSet> sets) {
		int n = sets.size();

		UndirectedWeightedListGraph graph = new UndirectedWeightedListGraph(n);

		for (int i = 0; i < n; i++) {
			TIntHashSet x = sets.get(i);
			for (int j = 0; j < i; j++) {
				TIntHashSet y = sets.get(j);

				TIntHashSet intersection = HomologyUtility.computeIntersection(x, y);

				if (intersection.size() > 0)
					graph.addEdge(i, j, intersection.size());
			}
		}

		return graph;
	}

	protected static TIntHashSet pullUpIndices(TIntHashSet subIndices, TIntHashSet indices) {
		TIntHashSet result = new TIntHashSet();

		TIntIterator iterator = subIndices.iterator();

		int[] indicesArray = indices.toArray();

		while (iterator.hasNext()) {
			int subIndex = iterator.next();
			result.add(indicesArray[subIndex]);
		}

		return result;
	}
}
