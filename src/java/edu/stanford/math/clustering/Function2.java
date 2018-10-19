package edu.stanford.math.clustering;

import java.io.Serializable;

/**
 * A two-argument function that takes arguments of type T1 and T2 and returns an R.
 */
@FunctionalInterface
public interface Function2<T1, T2, R> extends Serializable {
    R call(T1 v1, T2 v2);
}
