package edu.umbc.cs.maple.testing;

import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import edu.umbc.cs.maple.config.ExperimentConfig;
import edu.umbc.cs.maple.hierarchy.framework.Task;
import edu.umbc.cs.maple.palm.agent.CrossPALMLearningAgent;
import edu.umbc.cs.maple.palm.agent.PALMLearningAgent;
import edu.umbc.cs.maple.palm.agent.PALMModelGenerator;
import edu.umbc.cs.maple.palm.rmax.agent.ExpectedRmaxModelGenerator;
//import edu.umbc.cs.maple.palm.rmax.agent.ExpertNavModelGenerator;
import edu.umbc.cs.maple.palm.rmax.agent.ExpertNavModelGenerator;
import edu.umbc.cs.maple.palm.rmax.agent.PALMRmaxModelGenerator;
import edu.umbc.cs.maple.rmaxq.agent.RmaxQLearningAgent;
import edu.umbc.cs.maple.state.hashing.bugfix.BugfixHashableStateFactory;
import edu.umbc.cs.maple.state.hashing.bugfix.FixDiscretizedHashableStateFactory;
import edu.umbc.cs.maple.utilities.BurlapConstants;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static edu.umbc.cs.maple.utilities.BurlapConstants.DEFAULT_LEARNING_RATE;
import static edu.umbc.cs.maple.utilities.BurlapConstants.DEFAULT_Q_INIT;

public enum AgentType {

    PALM("palm", "PALM"){
        @Override
        public LearningAgent getLearningAgent(Task root, HashableStateFactory hsf, ExperimentConfig config) {
            PALMModelGenerator modelGen = new PALMRmaxModelGenerator(hsf, config);
            LearningAgent agent = new PALMLearningAgent(root, modelGen, hsf, config);
            return agent;
        }
    },
    CROSS("cross", "Cross-PALM") {
        @Override
        public LearningAgent getLearningAgent(Task root, HashableStateFactory hsf, ExperimentConfig config) {
            PALMModelGenerator modelGen = new PALMRmaxModelGenerator(hsf, config);
            LearningAgent agent = new CrossPALMLearningAgent(root, modelGen, hsf, config);
            return agent;
        }
    },
    RMAXQ("rmaxq", "RMAXQ"){
        @Override
        public LearningAgent getLearningAgent(Task root, HashableStateFactory hsf, ExperimentConfig config) {
            return new RmaxQLearningAgent(root, hsf, config);
        }

    },
    KAPPA("kappa", "κ"){
        @Override
        public LearningAgent getLearningAgent(Task root, HashableStateFactory hsf, ExperimentConfig config) {
            ExpectedRmaxModelGenerator modelGen = new ExpectedRmaxModelGenerator(hsf, config);
            PALMLearningAgent agent = new PALMLearningAgent(root, modelGen, hsf, config);
            return agent;
        }

    },
    Q_LEARNING("ql", "QL"){
        @Override
        public LearningAgent getLearningAgent(Task root, HashableStateFactory hsf, ExperimentConfig config) {
            OOSADomain baseDomain = (OOSADomain) config.baseDomain;
            double qInit = DEFAULT_Q_INIT;
            double learningRate = DEFAULT_LEARNING_RATE;
            LearningAgent agent = new QLearning(baseDomain, config.gamma, hsf, qInit, learningRate);
            return agent;
        }
    },
    PALM_EXPERT_NAV_GIVEN("palmExpertWithNavGiven", "PALM-Expert w/ Nav"){
        @Override
        public LearningAgent getLearningAgent(Task root, HashableStateFactory hsf, ExperimentConfig config) {
            PALMModelGenerator modelGen = new ExpertNavModelGenerator(hsf, config);
            LearningAgent agent = new PALMLearningAgent(root, modelGen, hsf, config);
            return agent;
        }

    }


    ;

    private String type;
    private String plotterDisplayName;

    AgentType(String type, String plotterDisplayName) {
        this.type = type;
        this.plotterDisplayName = plotterDisplayName;
    }

    public String getType() {
        return type;
    }

    public String getPlotterDisplayName() {
        return plotterDisplayName;
    }

    public static List<String> getTypes() {
        return Arrays.stream(AgentType.values()).map(AgentType::getType).collect(Collectors.toList());
    }

    public abstract LearningAgent getLearningAgent(Task root, HashableStateFactory hsf, ExperimentConfig config);
    public LearningAgent getLearningAgent(Task root, ExperimentConfig config) {
        HashableStateFactory hsf = initializeHashableStateFactory(config.hashable_state_factory, config.identifier_independent);
        return getLearningAgent(root, hsf, config);
    }

    public static HashableStateFactory hsf = null;
    public static HashableStateFactory initializeHashableStateFactory(HashableStateFactory typeSource, boolean identifierIndependent) {
        if (hsf == null && typeSource == null) {
            hsf = new BugfixHashableStateFactory(identifierIndependent);
        } else if (typeSource instanceof FixDiscretizedHashableStateFactory){
            hsf = new FixDiscretizedHashableStateFactory(identifierIndependent, BurlapConstants.DEFAULT_MULTIPLE_DISCRETIZATION);
        } else {
            throw new RuntimeException("bug in initialization of hashable state factory");
        }
        return hsf;
    }

    public static AgentType getByType(String name) {
        for (AgentType type : values()) {
            if (type.type.equals(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException(name);
    }

    public static final boolean DEFAULT_IDENTIFIER_INDEPENDENT = false;
    public static LearningAgentFactory generateLearningAgentFactory(Task root, ExperimentConfig config, String agentTypeName, String agentName) {
        AgentType agentType = AgentType.getByType(agentTypeName);
        String extra = agentName.contains("hier") ? "-H" : "";
        String extra2 = agentName.contains("baseline") ? "-B" : "";
        LearningAgentFactory agent = new LearningAgentFactory() {
            @Override
            public String getAgentName() {
                return agentType.getPlotterDisplayName() + extra + extra2;
            }
            @Override
            public LearningAgent generateAgent() {
                return agentType.getLearningAgent(root, config);
            }
        };
        return agent;
    }
}