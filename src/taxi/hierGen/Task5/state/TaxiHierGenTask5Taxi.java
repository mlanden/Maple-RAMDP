package taxi.hierGen.Task5.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

import static taxi.TaxiConstants.ATT_X;
import static taxi.TaxiConstants.ATT_Y;

public class TaxiHierGenTask5Taxi extends MutableObject{

    private final static List<Object> keys = Arrays.<Object>asList(
            ATT_X,
            ATT_Y);

    public TaxiHierGenTask5Taxi(String name, int x, int y){
        this(name, (Object) x, (Object) y);
    }

    private TaxiHierGenTask5Taxi(String name, Object x, Object y){
        this.set(ATT_X, x);
        this.set(ATT_Y, y);
        this.setName(name);
    }

    @Override
    public String className() {
        return TaxiHierGenTask5State.CLASS_ROOT_Taxi;
    }

    @Override
    public ObjectInstance copyWithName(String objectName) {
        return new TaxiHierGenTask5Taxi(
                objectName,
                get(ATT_X),
                get(ATT_Y)
        );
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

    @Override
    public TaxiHierGenTask5Taxi copy() {
        return (TaxiHierGenTask5Taxi) copyWithName(name());
    }
}
