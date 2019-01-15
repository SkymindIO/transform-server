import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.*;

public class PythonVariables {

    enum Type{
        BOOL,
        STR,
        INT,
        FLOAT,
        NDARRAY,
        LIST,
        FILE

    }

    private Map<String, String> strVars = new HashMap<String, String>();
    private Map<String, Integer> intVars = new HashMap<String, Integer>();
    private Map<String, Double> floatVars = new HashMap<String, Double>();
    private Map<String, Boolean> boolVars = new HashMap<String, Boolean>();
    private Map<String, NumpyArray> ndVars = new HashMap<String, NumpyArray>();
    private Map<String, Object[]> listVars = new HashMap<String, Object[]>();
    private Map<String, String> fileVars = new HashMap<String, String>();

    private Map<String, Type> vars = new HashMap<String, Type>();

    private Map<Type, Map> maps = new HashMap<Type, Map>();


    public PythonVariables(){
        maps.put(Type.BOOL, boolVars);
        maps.put(Type.STR, strVars);
        maps.put(Type.INT, intVars);
        maps.put(Type.FLOAT, floatVars);
        maps.put(Type.NDARRAY, ndVars);
        maps.put(Type.LIST, listVars);
        maps.put(Type.FILE, fileVars);

    }

    public void addBool(String name){
        vars.put(name, Type.BOOL);
        boolVars.put(name, null);
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

    public void addList(String name){
        vars.put(name, Type.LIST);
        listVars.put(name, null);
    }

    public void addFile(String name){
        vars.put(name, Type.FILE);
        fileVars.put(name, null);
    }
    public void addBool(String name, boolean value){
        vars.put(name, Type.BOOL);
        boolVars.put(name, value);
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

    public void addNDArray(String name, INDArray value){
        vars.put(name, Type.NDARRAY);
        ndVars.put(name, new NumpyArray(value));
    }

    public void addList(String name, Object[] value){
        vars.put(name, Type.LIST);
        listVars.put(name, value);
    }

    public void addFile(String name, String value){
        vars.put(name, Type.FILE);
        fileVars.put(name, value);
    }

    public void setValue(String name, Object value) throws Exception{
        Type type = vars.get(name);
        if (type == Type.BOOL){
            boolVars.put(name, (Boolean)value);
        }
        else if (type == Type.INT){
            intVars.put(name, ((Long)value).intValue());
        }
        else if (type == Type.FLOAT){
            floatVars.put(name, (Double)value);
        }
        else if (type == Type.NDARRAY){
            if (value instanceof  NumpyArray){
                ndVars.put(name, (NumpyArray)value);
            }
            else if (value instanceof  INDArray){
                ndVars.put(name, (NumpyArray)value);
            }
            else{
                ndVars.put(name, new NumpyArray((JSONObject)value));
            }
        }
        else if (type == Type.LIST){
            listVars.put(name, (Object[]) value);
        }
        else if (type == Type.FILE){
            fileVars.put(name, (String)value);
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

    public Object[] getListValue(String name){
        return listVars.get(name);
    }

    public String getFileValue(String name){
        return fileVars.get(name);
    }

    public Type getType(String name){
        return vars.get(name);
    }

    public String[] getVariables() {
        String[] strArr = new String[vars.size()];
        return vars.keySet().toArray(strArr);
    }


    public Map<String, Boolean> getBoolVariables(){
        return boolVars;
    }
    public Map<String, String> getStrVariables(){
        return strVars;
    }

    public Map<String, Integer> getIntVariables(){
        return intVars;
    }

    public Map<String, Double> getFloatVariables(){
        return floatVars;
    }

    public Map<String, NumpyArray> getNDArrayVariables(){
        return ndVars;
    }

    public Map<String, Object[]> getListVariables(){
        return listVars;
    }

    public Map<String, String> getFileVariables(){
        return fileVars;
    }

    public JSONArray toJSON(){
        JSONArray arr = new JSONArray();
        for (String varName: getVariables()){
            JSONObject var = new JSONObject();
            var.put("name", varName);
            String varType = getType(varName).toString();
            var.put("type", varType);
            arr.add(var);
        }
        return arr;
    }

    public static PythonVariables fromJSON(JSONArray jsonArray){
        PythonVariables pyvars = new PythonVariables();
        for (int i=0; i<jsonArray.size(); i++){
            JSONObject input = (JSONObject) jsonArray.get(i);
            String varName = (String)input.get("name");
            String varType = (String)input.get("type");
            if (varType.equals("BOOL")){
                pyvars.addBool(varName);
            }
            else if (varType.equals("INT")){
                pyvars.addInt(varName);
            }
            else if (varType.equals("FlOAT")){
                pyvars.addFloat(varName);
            }
            else if (varType.equals("STR")){
                pyvars.addStr(varName);
            }
            else if (varType.equals("LSIT")){
                pyvars.addList(varName);
            }
            else if (varType.equals("FILE")){
                pyvars.addFile(varName);
            }
            else if (varType.equals("NDARRAY")){
                pyvars.addNDArray(varName);
            }
        }

        return pyvars;
    }
}
