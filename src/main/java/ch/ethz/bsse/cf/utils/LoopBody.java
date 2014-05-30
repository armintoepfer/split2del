package ch.ethz.bsse.cf.utils;

/**
 * Parallel Loop Body Interface
 */
public interface LoopBody<T> {

    void run(T p);
}

