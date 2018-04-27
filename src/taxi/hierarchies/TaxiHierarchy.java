package taxi.hierarchies;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.auxiliary.common.IdentityStateMapping;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.oo.OOSADomain;
import hierarchy.framework.*;
import taxi.PickupActionType;
import taxi.PutdownActionType;
import taxi.Taxi;
import taxi.functions.amdp.*;
import taxi.hierGen.Task5.state.Task5StateMapper;
import taxi.hierGen.Task7.state.Task7StateMapper;
import taxi.hierGen.Task7.state.TaxiHierGenTask7State;
import taxi.hierGen.actions.HierGenDropoffActiontype;
import taxi.hierGen.actions.HierGenPickupActiontype;
import taxi.hierGen.actions.HierGenTask5ActionType;
import taxi.hierGen.functions.FailureFunction;
import taxi.hierGen.functions.HierGenRootCompleted;
import taxi.hierGen.functions.HierGenTask5Completed;
import taxi.hierGen.functions.HierGenTask7Completed;
import taxi.hierGen.root.state.HierGenRootStateMapper;
import taxi.hierGen.root.state.TaxiHierGenRootState;
import taxi.hierarchies.tasks.NavigateActionType;
import taxi.hierarchies.tasks.bringon.TaxiBringonDomain;
import taxi.hierarchies.tasks.bringon.state.BringonStateMapper;
import taxi.hierarchies.tasks.dropoff.TaxiDropoffDomain;
import taxi.hierarchies.tasks.dropoff.state.DropoffStateMapper;
import taxi.hierarchies.tasks.get.TaxiGetDomain;
import taxi.hierarchies.tasks.get.state.GetStateMapper;
import taxi.hierarchies.tasks.nav.TaxiNavDomain;
import taxi.hierarchies.tasks.nav.state.NavStateMapper;
import taxi.hierarchies.tasks.put.TaxiPutDomain;
import taxi.hierarchies.tasks.put.state.PutStateMapper;
import taxi.hierarchies.tasks.root.GetActionType;
import taxi.hierarchies.tasks.root.PutActionType;
import taxi.hierarchies.tasks.root.TaxiRootDomain;
import taxi.hierarchies.tasks.root.state.RootStateMapper;
import taxi.rmaxq.functions.BaseRootPF;

import static taxi.hierarchies.tasks.get.TaxiGetDomain.ACTION_NAV;
import static taxi.hierarchies.tasks.root.TaxiRootDomain.ACTION_GET;
import static taxi.hierarchies.tasks.root.TaxiRootDomain.ACTION_PUT;

public class TaxiHierarchy {

	/**
	 * the full base taxi domain
	 */
	private static OOSADomain baseDomain;
	
	/***
	 * creates the standards taxi hierarchy and returns the root task
	 * @param correctMoveprob the transitionProbability that a movement action will work as expected
	 * @param fickleProbability the transitionProbability that a passenger in the taxi will change goals
	 * @return the root task of the taxi hierarchy
	 */
	public static Task createAMDPHierarchy(double correctMoveprob, double fickleProbability, boolean plan){
		// Setup taxi domain
		Taxi taxiDomain;
		if(fickleProbability == 0){
			taxiDomain = new Taxi(false, fickleProbability, correctMoveprob);
		}else{
			taxiDomain = new Taxi(true, fickleProbability, correctMoveprob);
		}

		// Domains
		OOSADomain rootDomain = (new TaxiRootDomain()).generateDomain();
		OOSADomain getDomain = (new TaxiGetDomain()).generateDomain();
		OOSADomain putDomain = (new TaxiPutDomain()).generateDomain();
		OOSADomain navDomain = (new TaxiNavDomain()).generateDomain();
		OOSADomain bringonDomain = (new TaxiBringonDomain()).generateDomain();
		OOSADomain dropoffDomain = (new TaxiDropoffDomain()).generateDomain();
		baseDomain = taxiDomain.generateDomain();

		// Navigate Tasks (Primitives used for Put Nav and Get Nav later)
		ActionType aNorth = navDomain.getAction(Taxi.ACTION_NORTH);
		PrimitiveTask north = new PrimitiveTask(aNorth, baseDomain);
		ActionType aEast = navDomain.getAction(Taxi.ACTION_EAST);
		PrimitiveTask east = new PrimitiveTask(aEast, baseDomain);
		ActionType aSouth = navDomain.getAction(Taxi.ACTION_SOUTH);
		PrimitiveTask south = new PrimitiveTask(aSouth, baseDomain);
		ActionType aWest = navDomain.getAction(Taxi.ACTION_WEST);
		PrimitiveTask west = new PrimitiveTask(aWest, baseDomain);
		Task[] navTasks = {north, east, south, west};

		double defaultReward = NonprimitiveTask.DEFAULT_REWARD;
		double noopReward = NonprimitiveTask.NOOP_REWARD;

		// Nav (Task used by Get and Put)
		ActionType aNavigate = putDomain.getAction(TaxiPutDomain.ACTION_NAV);
		NonprimitiveTask navigate = new NonprimitiveTask(
				navTasks,
				aNavigate,
				navDomain,
				new NavStateMapper(),
				new NavFailurePF(),
				new NavCompletedPF(),
				defaultReward,
				noopReward
		);
		if (plan) { setupKnownTFRF(navigate); }

		// Pickup (Primitive used by Bringon)
		ActionType aPickup = bringonDomain.getAction(Taxi.ACTION_PICKUP);
		PrimitiveTask pickup = new PrimitiveTask(aPickup, baseDomain);

		// Bringon (Task used by Get)
		ActionType aBringon = getDomain.getAction(TaxiGetDomain.ACTION_BRINGON);
		Task[] bringonTasks = {pickup};
		NonprimitiveTask bringon = new NonprimitiveTask(
				bringonTasks,
				aBringon,
				bringonDomain,
				new BringonStateMapper(),
				new BringonFailurePF(),
				new BringonCompletedPF(),
				defaultReward,
				noopReward
		);
		if (plan) { setupKnownTFRF(bringon); }

		// Get (Task used by Root)
		ActionType aGet = rootDomain.getAction(ACTION_GET);
		Task[] getTasks = {bringon, navigate};
		NonprimitiveTask get = new NonprimitiveTask(
				getTasks,
				aGet,
				getDomain,
				new GetStateMapper(),
				new GetFailurePF(),
				new GetCompletedPF(),
				defaultReward,
				noopReward
		);
		if (plan) { setupKnownTFRF(get); }

		// Putdown (Primitive used by Dropoff)
		ActionType aPutdown = dropoffDomain.getAction(Taxi.ACTION_PUTDOWN);
		PrimitiveTask putdown = new PrimitiveTask(aPutdown, baseDomain);

		// Dropoff (Task used by Put)
		ActionType aDropoff = putDomain.getAction(TaxiPutDomain.ACTION_DROPOFF);
		Task[] dropoffTasks = {putdown};
		NonprimitiveTask dropoff = new NonprimitiveTask(
				dropoffTasks,
				aDropoff,
				dropoffDomain,
				new DropoffStateMapper(),
				new DropoffFailurePF(),
				new DropoffCompletedPF(),
				defaultReward,
				noopReward
		);
		if (plan) { setupKnownTFRF(dropoff); }

		// Put (Task used by Root)
		ActionType aPut = rootDomain.getAction(ACTION_PUT);
		Task[] putTasks = {dropoff, navigate};
		NonprimitiveTask put = new NonprimitiveTask(
				putTasks,
				aPut,
				putDomain,
				new PutStateMapper(),
				new PutFailurePF(),
				new PutCompletedPF(),
				defaultReward,
				noopReward
		);
		if (plan) { setupKnownTFRF(put); }

		// Root
		ActionType aSolve = new SolveActionType();
		Task[] rootTasks = {get, put};
		NonprimitiveTask root = new NonprimitiveTask(
				rootTasks,
				aSolve,
				rootDomain,
				new RootStateMapper(),
				new RootFailurePF(),
				new RootCompletedPF(),
				defaultReward,
				noopReward
		);
		if (plan) { setupKnownTFRF(root); }

		return root;
	}

	private static void setupKnownTFRF(NonprimitiveTask task) {
		GoalFailTF tf = task.getGoalFailTF();
		GoalFailRF rf = task.getGoalFailRF();
		FactoredModel model = (FactoredModel) task.getDomain().getModel();
		model.setTf(tf);
		model.setRf(rf);
	}

	/**
	 * creates a taxi hierarchy with no abstractions 
	 * @param correctMoveprob the transitionProbability that a movement action will work as expected
	 * @param fickleProbability the transitionProbability that a passenger in the taxi will change goals
	 * @return the root task of the taxi hierarchy
	 */
	public static Task createRMAXQHierarchy(double correctMoveprob, double fickleProbability){
		Taxi taxiDomain;

		if(fickleProbability == 0){
			taxiDomain = new Taxi(false, fickleProbability, correctMoveprob);
		}else{
			taxiDomain = new Taxi(true, fickleProbability, correctMoveprob);
		}

		//action type domain - not for tasks
		baseDomain = taxiDomain.generateDomain();

		ActionType aNorth = baseDomain.getAction(Taxi.ACTION_NORTH);
		ActionType aEast = baseDomain.getAction(Taxi.ACTION_EAST);
		ActionType aSouth = baseDomain.getAction(Taxi.ACTION_SOUTH);
		ActionType aWest = baseDomain.getAction(Taxi.ACTION_WEST);
		ActionType aPickup = new PickupActionType(Taxi.ACTION_PICKUP, new String[]{Taxi.CLASS_PASSENGER});
		ActionType aPutdown = new PutdownActionType(Taxi.ACTION_PUTDOWN, new String[]{Taxi.CLASS_PASSENGER});
		ActionType aNavigate = new NavigateActionType(ACTION_NAV, new String[]{Taxi.CLASS_LOCATION});
		ActionType aGet = new GetActionType(ACTION_GET, new String[]{Taxi.CLASS_PASSENGER});
		ActionType aPut = new PutActionType(ACTION_PUT, new String[]{Taxi.CLASS_PASSENGER});
		ActionType aSolve = new SolveActionType();

		//tasks
		PrimitiveTask north = new PrimitiveTask(aNorth, baseDomain);
		PrimitiveTask east = new PrimitiveTask(aEast, baseDomain);
		PrimitiveTask south = new PrimitiveTask(aSouth, baseDomain);
		PrimitiveTask wast = new PrimitiveTask(aWest, baseDomain);
		PrimitiveTask pickup = new PrimitiveTask(aPickup, baseDomain);
		PrimitiveTask dropoff = new PrimitiveTask(aPutdown, baseDomain);

		Task[] navTasks = new Task[]{north, east, south, wast};
//		Task[] bringonTasks = new Task[]{pickup};
//		Task[] dropoffTasks = new Task[]{dropoff};

		double defaultReward = NonprimitiveTask.DEFAULT_REWARD;
		double noopReward = NonprimitiveTask.NOOP_REWARD;

        PropositionalFunction navFailPF = new NavFailurePF();
		PropositionalFunction navCompPF = new NavCompletedPF();
		OOSADomain navDomain = taxiDomain.generateNavigateDomain();
		navDomain.setModel(null);
		NonprimitiveTask navigate = new NonprimitiveTask(navTasks, aNavigate, navDomain,
				new NavStateMapper(), navFailPF, navCompPF, defaultReward, noopReward);

		Task[] getTasks = new Task[]{pickup, navigate};
		Task[] putTasks = new Task[]{navigate, dropoff};

		PropositionalFunction getFailPF = new GetFailurePF();
		PropositionalFunction getCompPF = new GetCompletedPF();
		OOSADomain getDomain = taxiDomain.generateDomain();
		getDomain.setModel(null);
		NonprimitiveTask get = new NonprimitiveTask(
				getTasks,
				aGet,
				getDomain,
				new GetStateMapper(),
				getFailPF,//new GetFailurePF(),
				getCompPF,//new GetCompletedPF(),
				defaultReward,
				noopReward
		);

		PropositionalFunction putFailPF = new PutFailurePF();
		PropositionalFunction putCompPF = new PutCompletedPF();
		OOSADomain putDomain = taxiDomain.generateDomain();
		putDomain.setModel(null);
		NonprimitiveTask put = new NonprimitiveTask(
				putTasks,
				aPut,
				putDomain, new PutStateMapper(),
				putFailPF,//new PutFailurePF(),
				putCompPF,//new PutCompletedPF(),
				defaultReward,
				noopReward
		);

		Task[] rootTasks = {get, put};
		PropositionalFunction rootPF = new BaseRootPF();
		OOSADomain rootDomain = taxiDomain.generateDomain();
		rootDomain.setModel(null);
//		baseActual.clearActionTypes();
//		baseActual.addActionTypes(
//				aNorth,
//				aEast,
//				aSouth,
//				aWest,
//				aPickup,
//				aPutdown
//		);
		Task root = new NonprimitiveTask(rootTasks, aSolve, rootDomain, new RootStateMapper(), rootPF, rootPF, defaultReward, noopReward);

		return root;
//		throw new RuntimeException("need to be reimplemented");
		
	}
	
	/**
	 * get base taxi domain
	 * @return full base taxi domain
	 */
	public static OOSADomain getBaseDomain(){
		return baseDomain;
	}

	/***
	 * creates the hiergen taxi hierarchy and returns the root task
	 * @param correctMoveprob the transitionProbability that a movement action will work as expected
	 * @param fickleProbability the transitionProbability that a passenger in the taxi will change goals
	 * @return the root task of the taxi hierarchy
	 */
	public static Task createHierGenHierarchy(double correctMoveprob, double fickleProbability) {
		Taxi taxiDomain;
		if (fickleProbability == 0) {
			taxiDomain = new Taxi(false, fickleProbability, correctMoveprob);
		} else {
			taxiDomain = new Taxi(true, fickleProbability, correctMoveprob);
		}

		baseDomain = taxiDomain.generateDomain();

		ActionType aNorth = baseDomain.getAction(Taxi.ACTION_NORTH);
		ActionType aEast = baseDomain.getAction(Taxi.ACTION_EAST);
		ActionType aSouth = baseDomain.getAction(Taxi.ACTION_SOUTH);
		ActionType aWest = baseDomain.getAction(Taxi.ACTION_WEST);
		ActionType aPickup = new HierGenPickupActiontype(Taxi.ACTION_PICKUP, new String[]{TaxiHierGenTask7State.CLASS_TASK7_PASSENGER});
		ActionType aPutdown = new HierGenDropoffActiontype(Taxi.ACTION_PUTDOWN, new String[]{TaxiHierGenRootState.CLASS_ROOT_PASSENGER});
		ActionType aTask5 = new HierGenTask5ActionType();;
		ActionType aTask7 = new UniversalActionType("task_7");
		ActionType asolve = new SolveActionType();

		//state mapper
		StateMapping task5Map = new Task5StateMapper();
		StateMapping task7Map = new Task7StateMapper();
		StateMapping rootMap = new HierGenRootStateMapper();

		//tasks
		PrimitiveTask north = new PrimitiveTask(aNorth, baseDomain);
		PrimitiveTask east = new PrimitiveTask(aEast, baseDomain);
		PrimitiveTask south = new PrimitiveTask(aSouth, baseDomain);
		PrimitiveTask wast = new PrimitiveTask(aWest, baseDomain);
		PrimitiveTask pickup = new PrimitiveTask(aPickup, baseDomain);
		PrimitiveTask dropoff = new PrimitiveTask(aPutdown, baseDomain);

		double defaultReward = NonprimitiveTask.DEFAULT_REWARD;
		double noopReward = NonprimitiveTask.NOOP_REWARD;

		Task[] task5Children = {north, east, south, wast};
		PropositionalFunction task5CompletedPF = new HierGenTask5Completed();
		PropositionalFunction task5FailPF = new FailureFunction();
		NonprimitiveTask task5 = new NonprimitiveTask(
				task5Children,
				aTask5,
				baseDomain,
				task5Map,
				task5FailPF,
				task5CompletedPF,
				defaultReward,
				noopReward
		);

		Task[] task7Children = {task5, pickup};
		PropositionalFunction task7CompletedPF = new HierGenTask7Completed();
		PropositionalFunction task7FailPF = new FailureFunction();
		NonprimitiveTask task7 = new NonprimitiveTask(
				task7Children,
				aTask7,
				baseDomain,
				task7Map,
				task7FailPF,
				task7CompletedPF,
				defaultReward,
				noopReward
		);

		Task[] rootChildren = {task7, dropoff, task5};
		PropositionalFunction rootCompletedPF = new HierGenRootCompleted();
		PropositionalFunction rootFailPF = new FailureFunction();
		NonprimitiveTask root = new NonprimitiveTask(
				rootChildren,
				asolve,
				baseDomain,
				rootMap,
				rootFailPF,
				rootCompletedPF,
				defaultReward,
				noopReward
		);

		return root;
	}
}