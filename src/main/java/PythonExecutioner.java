import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bytedeco.javacpp.*;
import static org.bytedeco.javacpp.python.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;



public class PythonExecutioner {
    private String name;
    private static String tempFile = "temp.txt";
    private Pointer namePtr;

    public PythonExecutioner(String name){
        this.name = name;
        init();
    }


    public  PythonExecutioner(){
        this.name = PythonExecutioner.class.getSimpleName();
        init();
    }

    private void init(){
        namePtr = Py_DecodeLocale(name, null);
        Py_SetProgramName(namePtr);
        Py_Initialize();
    }

    public void free(){
        if (Py_FinalizeEx() < 0) {
            throw new RuntimeException("Python execution failed.");
        }
        PyMem_RawFree(namePtr);
    }

    public static void exec(String code){
        PyRun_SimpleStringFlags(code, null);
    }


    public void exec(List<String> code){
        String x = "";
        for (String line: code){
            x += line + ";";
        }
        exec(x);
    }


    public void exec(String[] code){
        String x = "";
        for (String line: code){
            x += line + ";";
        }
        exec(x);
    }



    public void exec(String code, PythonInputs pyInputs, PythonOutputs pyOutputs) throws ParseException{
        Map<String, String> strInputs = pyInputs.getStrInputs();
        Map<String, Integer> intInputs = pyInputs.getIntInputs();
        Map<String, Double> floatInputs = pyInputs.getFloatInputs();

        String setcode = "";
        String[] VarNames = (String[])strInputs.keySet().toArray();
        for(String varName: VarNames){
            String varValue = strInputs.get(varName);
            setcode += varName + " + " + varValue + ";";
        }
        exec(setcode);

        setcode = "";
        VarNames = (String[])intInputs.keySet().toArray();
        for(String varName: VarNames){
            Integer varValue = intInputs.get(varName);
            setcode += varName + " + " + varValue.toString() + ";";
        }
        exec(setcode);

        setcode = "";
        VarNames = (String[])floatInputs.keySet().toArray();
        for(String varName: VarNames){
            Double varValue = floatInputs.get(varName);
            setcode += varName + " + " + varValue.toString() + ";";
        }
        exec(setcode);

        exec(code);


        String getcode = "import json;json.dump{";
        VarNames = pyOutputs.getOutputs();
        for (String varName: VarNames){
            getcode += "\"" + varName + "\"" + ":" + varName + ",";
        }
        getcode = getcode.substring(0, getcode.length() - 1);
        getcode += "},open('" + tempFile + "', 'w'));";

        exec(getcode);

        String out = read(tempFile);

        JSONParser parser = new JSONParser();
        JSONObject jsObject = (JSONObject) parser.parse(out);

        for (String varName: VarNames){
            Object varValue = jsObject.get(varName);
            pyOutputs.setValue(varName, varValue);
        }


    }

    private static String read(String file){
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[10];
            StringBuilder sb = new StringBuilder();
            while (fis.read(buffer) != -1) {
                sb.append(new String(buffer));
                buffer = new byte[10];
            }
            fis.close();

            String content = sb.toString();

            return content;
        }
        catch (Exception e){
            return "";
        }
    }



}
