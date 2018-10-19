package edu.stanford.math.clustering;

import edu.stanford.math.plex4.metric.interfaces.AbstractIntMetricSpace;

// The SLINK algorithm for Agglomerative Hierarchical Clustering in O(n^2) time and O(n) space.
public class SLINK extends HierarchicalClustering {

    public SLINK(AbstractIntMetricSpace subMetricSpace) {
        super(subMetricSpace);
    }

    @Override
    public void performClustering() {

    }

    public class SLINKClusteringResult {
        public final int[] height;
        public final int[] parent;
        public SLINKClusteringResult(int[] height, int[] parent) {
            this.height = height;
            this.parent = parent;
        }
    }

    public SLINKClusteringResult slink(A[] data) {
        int size = data.length;
        double[] height = new double[size];
        int[] parent = new int[size];
        int[] distanceN = new int[size];
        double[] distanceNd = new double[size];
        for (int n = 0; n < size; n++) {
            // Step 1
            parent[n] = n;
            height[n] = Integer.MAX_VALUE;
            // Step 2
            for (int i = 0; i < n; i++) {
                distanceN[i] = n;
                distanceNd[i] = metricSpace.distance(i, n);
            }
            // Step 3
            for (int i = 0; i < n; i++) {
                if (height[i] >= distanceN[i]) {
                    distanceN[parent[i]] = Math.min(distanceN[parent[i]], height[i]);
                    height[i] = distanceN[i];
                    parent[i] = n;
                } else {
                    distanceN[parent[i]] = Math.min(distanceN[parent[i]], distanceN[i]);
                }
            }
            // Step 4
            for (int i = 0; i < n; i++) {
                if (height[i] >= height[parent[i]]) {
                    parent[i] = n;
                }
            }
        }
        return new SLINKClusteringResult(height, parent);
    }
}
