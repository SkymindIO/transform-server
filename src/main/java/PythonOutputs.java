import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PythonOutputs {

    enum Type{
        STR,
        INT,
        FLOAT;

    }

    private Map<String, String> strOutputs = new HashMap<String, String>();
    private Map<String, Integer> intOutputs = new HashMap<String, Integer>();
    private Map<String, Double> floatOutputs = new HashMap<String, Double>();

    private Map<String, Type> outputs;

    private Map<Type, Map> maps = new HashMap<Type, Map>();


    public PythonOutputs(){
        maps.put(Type.STR, strOutputs);
        maps.put(Type.INT, intOutputs);
        maps.put(Type.FLOAT, floatOutputs);

    }

    public void addStr(String name){
        outputs.put(name, Type.STR);
        strOutputs.put(name, null);
    }

    public void addInt(String name){
        outputs.put(name, Type.INT);
        intOutputs.put(name, null);
    }

    public void addFloat(String name){
        outputs.put(name, Type.FLOAT);
        floatOutputs.put(name, null);
    }

    public void setValue(String name, Object value){
        Map map = maps.get(outputs.get(name));
        map.put(name, value);
    }

    public Object getValue(String name){
        Type type = outputs.get(name);
        Map map = maps.get(type);
        return map.get(name);
    }

    public String getStrValue(String name){
        return strOutputs.get(name);
    }

    public int getIntValue(String name){
        return intOutputs.get(name);
    }

    public double getFloatValue(String name){
        return floatOutputs.get(name);
    }

    public Type getType(String name){
        return outputs.get(name);
    }

    public String[] getOutputs(){
        return (String[])outputs.keySet().toArray();
    }
}
