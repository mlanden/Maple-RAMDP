package config.rmax;

import burlap.behavior.singleagent.auxiliary.performance.PerformanceMetric;

public class RmaxConfig {
    public double vmax;
    public int threshold;
    public double max_delta;
    public double max_delta_rmaxq;
    public int max_iterations_in_model;
    public PerformanceMetric[] metrics;
}
