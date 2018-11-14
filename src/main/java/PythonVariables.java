import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PythonVariables {

    enum Type{
        STR,
        INT,
        FLOAT

    }

    private Map<String, String> strVars = new HashMap<String, String>();
    private Map<String, Integer> intVars = new HashMap<String, Integer>();
    private Map<String, Double> floatVars = new HashMap<String, Double>();

    private Map<String, Type> Vars = new HashMap<String, Type>();

    private Map<Type, Map> maps = new HashMap<Type, Map>();


    public PythonVariables(){
        maps.put(Type.STR, strVars);
        maps.put(Type.INT, intVars);
        maps.put(Type.FLOAT, floatVars);

    }

    public void addStr(String name){
        Vars.put(name, Type.STR);
        strVars.put(name, null);
    }

    public void addInt(String name){
        Vars.put(name, Type.INT);
        intVars.put(name, null);
    }

    public void addFloat(String name){
        Vars.put(name, Type.FLOAT);
        floatVars.put(name, null);
    }

    public void addStr(String name, String value){
        Vars.put(name, Type.STR);
        strVars.put(name, value);
    }

    public void addInt(String name, int value){
        Vars.put(name, Type.INT);
        intVars.put(name, value);
    }

    public void addFloat(String name, double value){
        Vars.put(name, Type.FLOAT);
        floatVars.put(name, value);
    }

    public void addFloat(String name, float value){
        Vars.put(name, Type.FLOAT);
        floatVars.put(name, (double)value);
    }

    public void setValue(String name, Object value){
        Type type = Vars.get(name);
        if (type == Type.INT){
            intVars.put(name, ((Long)value).intValue());
        }
        else if (type == Type.FLOAT){
            floatVars.put(name, (Double)value);
        }
        else{
            strVars.put(name, (String)value);
        }
    }

    public Object getValue(String name){
        Type type = Vars.get(name);
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

    public Type getType(String name){
        return Vars.get(name);
    }

    public String[] getVariables() {
        String[] strArr = new String[Vars.size()];
        return Vars.keySet().toArray(strArr);
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
}
