package liftCopter;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;
import liftCopter.state.LiftCopterState;
import static liftCopter.LiftCopterConstants.*;

public class LiftCopterRewardFunction implements RewardFunction {

    /**
     * the rewardTotal for taking a action
     */
    private double stepReward;

    /**
     * the rewardTotal for a impossible pickup or dropoff action
     */
    private double illegalActionReward;

    /**
     * the rewardTotal for completing the goal
     */
    private double goalReward;

    /**
     * the liftCopter terminal function
     */
    private TerminalFunction tf;

    /**
     * use the default rewards
     */
    public LiftCopterRewardFunction() {
        // scaled to fit goal at 1.0
        stepReward = -0.05;//-1;
        illegalActionReward = -0.5;//-10;
        goalReward = 1.0;//20;
        tf = new LiftCopterTerminalFunction();
    }

    /**
     * use custom rewards
     *
     * @param stepR    the rewardTotal for a action
     * @param illegalR the rewardTotal for a impossible pickup or dropoff
     * @param goalR    the rewardTotal for completing the goal
     */
    public LiftCopterRewardFunction(double stepR, double illegalR, double goalR) {
        stepReward = stepR;
        illegalActionReward = illegalR;
        goalReward = goalR;
        tf = new LiftCopterTerminalFunction();
    }

    @Override
    public double reward(State s, Action a, State sprime) {
        LiftCopterState state = (LiftCopterState) s;

        //goal rewardTotal when state is terminal
        if (tf.isTerminal(sprime))
            return goalReward + stepReward;

//		boolean LiftCopterOccupied = (boolean) state.getLiftCopterAtt(ATT_LiftCopter_OCCUPIED);
        boolean LiftCopterOccupied = state.isLiftCopterOccupied();
        double tx = (double) state.getCopter().get(ATT_X);
        double ty = (double) state.getCopter().get(ATT_Y);

        //illegal pickup when no cargo at liftCopter's location
        if (a.actionName().equals(ACTION_PICKUP)) {

            boolean cargoAtLiftCopter = false;
            for (ObjectInstance cargo : state.objectsOfClass(CLASS_CARGO)) {
                double px = (double) cargo.get(ATT_X);
                double py = (double) cargo.get(ATT_Y);
                double ph = (double) cargo.get(ATT_H);
                double pw = (double) cargo.get(ATT_W);
                boolean inLiftCopter = (boolean) cargo.get(ATT_PICKED_UP);
                if (px + pw >= tx && px <= tx && py + ph >= ty && py <= ty) {
                    cargoAtLiftCopter = true;
                    break;
                }
            }

            if (!cargoAtLiftCopter)
                return stepReward + illegalActionReward;
        }
        //illegal dropoff if not at depot or cargo not in liftCopter
        else if (a.actionName().startsWith(ACTION_PUTDOWN)) {
            if (!LiftCopterOccupied)
                return stepReward + illegalActionReward;

            // if liftCopter/cargo is not at depot
            boolean LiftCopterAtDepot = false;
            for (ObjectInstance location : state.objectsOfClass(CLASS_LOCATION)) {
                double lx = (double) location.get(ATT_X);
                double ly = (double) location.get(ATT_Y);
                double lh = (double) location.get(ATT_H);
                double lw = (double) location.get(ATT_W);
                if (lx + lw >= tx && lx <= tx && ly + lh >= ty && ly <= ty) {
                    LiftCopterAtDepot = true;
                    break;
                }
            }

            if (!LiftCopterAtDepot)
                return stepReward + illegalActionReward;
        }

        return stepReward;
    }
}
