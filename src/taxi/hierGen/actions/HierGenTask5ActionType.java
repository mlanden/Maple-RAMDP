package taxi.hierGen.actions;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import hierarchy.framework.StringFormat;
import taxi.hierGen.Task5.state.TaxiHierGenTask5State;
import taxi.hierarchies.interfaces.PassengerLocationParameterizable;

import java.util.ArrayList;
import java.util.List;

import static taxi.TaxiConstants.ACTION_TASK_5;

public class HierGenTask5ActionType implements ActionType {

    @Override
    public String typeName() {
        return ACTION_TASK_5;
    }

    @Override
    public Action associatedAction(String strRep) {
        String[] params = StringFormat.split(strRep);
        int goalX = Integer.parseInt(params[1]);
        int goalY = Integer.parseInt(params[2]);

        return new HierGenTask5Action(goalX, goalY);
    }

    @Override
    public List<Action> allApplicableActions(State s) {
        List<Action> actions = new ArrayList<Action>();
        PassengerLocationParameterizable st = (PassengerLocationParameterizable) s;

        for(String pname : st.getPassengers()){
            int goalX = st.getLocationX(pname);
            int goalY = st.getLocationY(pname);
            actions.add(new HierGenTask5Action(goalX, goalY));
        }
        return actions;
    }
}
