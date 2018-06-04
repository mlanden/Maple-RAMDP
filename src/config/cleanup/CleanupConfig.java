package config.cleanup;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.state.State;
import burlap.visualizer.Visualizer;
import cleanup.CleanupVisualizer;
import cleanup.state.CleanupRandomStateGenerator;
import cleanup.state.CleanupState;
import config.DomainConfig;
import config.ExperimentConfig;
import config.output.OutputConfig;
import config.planning.PlanningConfig;
import config.rmax.RmaxConfig;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import static utilities.BurlapConstants.DEFAULT_RNG_INDEX;

public class CleanupConfig extends DomainConfig {

    public double rewardGoal;
    public double rewardBase;
    public double rewardNoop;
    public double rewardPull;
    public int minX;
    public int minY;
    public int maxX;
    public int maxY;
    public int num_blocks;

    @Override
    public State generateState() {
        return (CleanupState) new CleanupRandomStateGenerator(minX, minY, maxX, maxY).getStateFor(state, num_blocks);
    }

    @Override
    public Visualizer getVisualizer(ExperimentConfig config) {
        return CleanupVisualizer.getVisualizer(config.output.visualizer.width, config.output.visualizer.height);
    }
}
