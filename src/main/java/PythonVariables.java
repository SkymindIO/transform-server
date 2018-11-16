import org.json.simple.JSONObject;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PythonVariables {

    enum Type{
        STR,
        INT,
        FLOAT,
        NDARRAY

    }

    private Map<String, String> strVars = new HashMap<String, String>();
    private Map<String, Integer> intVars = new HashMap<String, Integer>();
    private Map<String, Double> floatVars = new HashMap<String, Double>();
    private Map<String, NumpyArray> ndVars = new HashMap<String, NumpyArray>();
    private Map<String, Type> vars = new HashMap<String, Type>();

    private Map<Type, Map> maps = new HashMap<Type, Map>();


    public PythonVariables(){
        maps.put(Type.STR, strVars);
        maps.put(Type.INT, intVars);
        maps.put(Type.FLOAT, floatVars);
        maps.put(Type.NDARRAY, ndVars);
    }

    public void addStr(String name){
        vars.put(name, Type.STR);
        strVars.put(name, null);
    }

    public void addInt(String name){
        vars.put(name, Type.INT);
        intVars.put(name, null);
    }

    public void addFloat(String name){
        vars.put(name, Type.FLOAT);
        floatVars.put(name, null);
    }

    public void addNDArray(String name){
        vars.put(name, Type.NDARRAY);
        ndVars.put(name, null);
    }

    public void addStr(String name, String value){
        vars.put(name, Type.STR);
        strVars.put(name, value);
    }

    public void addInt(String name, int value){
        vars.put(name, Type.INT);
        intVars.put(name, value);
    }

    public void addFloat(String name, double value){
        vars.put(name, Type.FLOAT);
        floatVars.put(name, value);
    }

    public void addFloat(String name, float value){
        vars.put(name, Type.FLOAT);
        floatVars.put(name, (double)value);
    }

    public void addNDArray(String name, NumpyArray value){
        vars.put(name, Type.NDARRAY);
        ndVars.put(name, value);
    }

    public void addNDArray(String name, INDArray value)throws Exception{
        vars.put(name, Type.NDARRAY);
        ndVars.put(name, new NumpyArray(value));
    }

    public void setValue(String name, Object value){
        Type type = vars.get(name);
        if (type == Type.INT){
            intVars.put(name, ((Long)value).intValue());
        }
        else if (type == Type.FLOAT){
            floatVars.put(name, (Double)value);
        }
        else if (type == Type.NDARRAY){
            ndVars.put(name, new NumpyArray((JSONObject)value));
        }
        else{
            strVars.put(name, (String)value);
        }
    }

    public Object getValue(String name){
        Type type = vars.get(name);
        Map map = maps.get(type);
        return map.get(name);
    }

    public String getStrValue(String name){
        return strVars.get(name);
    }

    public int getIntValue(String name){
        return intVars.get(name);
    }

    public double getFloatValue(String name){
        return floatVars.get(name);
    }

    public NumpyArray getNDArrayValue(String name){
        return ndVars.get(name);
    }

    public Type getType(String name){
        return vars.get(name);
    }

    public String[] getVariables() {
        String[] strArr = new String[vars.size()];
        return vars.keySet().toArray(strArr);
    }


    public Map getStrVariables(){
        return strVars;
    }

    public Map getIntVariables(){
        return intVars;
    }

    public Map getFloatVariables(){
        return floatVars;
    }

    public Map getNDArrayVariables() {
        return ndVars;
    }
}
