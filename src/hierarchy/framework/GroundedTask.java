package hierarchy.framework;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.oo.OOSADomain;

public class GroundedTask {

	/**
	 * specific action in a task
	 */
	private Action action;
	
	/**
	 * the general task node which this grounded task is part of
	 */
	private Task t;
	
	/**
	 * each grounded task has an action and task
	 * @param a the parameterization of the task
	 * @param t the general task this is a part of
	 */
	public GroundedTask(Action a, Task t){
		this.action = a;
		this.t = t;
	}
	
	/**
	 * gets the action this task wraps around 
	 * @return the grounded task's action
	 */
	public Action getAction(){
		return action;
	}
	
	/**
	 * get the domain of the grounded task
	 * @return the domain which this task defines
	 */
	public OOSADomain getDomain(){
		return t.getDomain();
	}
	
	/**
	 * given a learned model, this builds a domain with the subtasks as actions 
	 * @param model the model to included in the domain
	 * @return a complete learned domain for the grounded task
	 */
	public OOSADomain getDomain(FullModel model){
		OOSADomain d = new OOSADomain();
		d.setModel(model);
		
		Task[] children = t.getChildren();
		for(Task child : children){
			d.addActionType(child.getActionType());
		}
		return d;
	}
	
	/**
	 * gets all executable tasks that are children of the task 
	 * @param s the current task
	 * @return list of all groundings of child tasks valid in the state
	 */
	public List<GroundedTask> getGroundedChildTasks(State s){
		Task[] children = t.getChildren();
		List<GroundedTask> gts = new ArrayList<GroundedTask>();
		for(Task t : children){
			gts.addAll(t.getAllGroundedTasks(s));
		}
		return gts;
	}

	/**
	 * Determines if this grounded task is terminated
	 * @param s the current state
	 * @return if this grounded task is terminal in s
	 */
	public boolean isFailure(State s){
		return t.isFailure(s, action);
	}
    
	/**
	 * pass the given state through the task's abstraction function
	 * @param s the base state 
	 * @return the abstracted state
	 */
	public State mapState(State s){
		return t.mapState(s);
	}
	
	/**
	 * test if the task is in the base domain
	 * @return whether this grounded represents an action in the base domain
	 */
	public boolean isPrimitive(){
		return t.isPrimitive();
	}
	
	/**
	 * each grounded task has a specific reward function
	 * this returns the reward of a transition into the given state 
	 * @param s the result of the transition
	 * @return the grounded task's reward of a transition to s
	 */
	public double getReward(State s){
		if(!t.isPrimitive()){
			NonprimitiveTask npt = (NonprimitiveTask) t;
			return npt.reward(s, action);
		}
		throw new RuntimeException("Only applicable for nonprimitive tasks");
	}
	
	@Override
	public String toString(){
		return action.actionName();
	}
	
	/**
	 * return if the grounded task is complete in the given state
	 * @param s the state to test
	 * @return whether the grounded task is complete in s
	 */
	public boolean isComplete(State s){
		return t.isComplete(s, action);
	}

    //default methods for lookup of grounded tasks
	@Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof GroundedTask)) {
            return false;
        }

        GroundedTask o = (GroundedTask) other;
        if(!this.action.actionName().equals(o.action.actionName())){
            return false; 
        }
        
        return true;
    }
     
    @Override
    public int hashCode() {
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder(31, 7);
        hashCodeBuilder.append(action.actionName());
        return hashCodeBuilder.toHashCode();
    }
}
