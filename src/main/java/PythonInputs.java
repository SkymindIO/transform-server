import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PythonInputs {

    private Map<String, String> strInputs = new HashMap<String, String>();
    private Map<String, Integer> intInputs = new HashMap<String, Integer>();
    private Map<String, Double> floatInputs = new HashMap<String, Double>();

    public PythonInputs(){

    }

    public void addStr(String name, String value){
        strInputs.put(name, value);
    }

    public void addInt(String name, int value){
        intInputs.put(name, value);
    }

    public void addFloat(String name, double value){
        floatInputs.put(name, value);
    }

    public void addFloat(String name, float value){
        floatInputs.put(name, (double)value);
    }

    public Map getStrInputs(){
        return strInputs;
    }

    public Map getIntInputs(){
        return intInputs;
    }

    public Map getFloatInputs(){
        return floatInputs;
    }
}
