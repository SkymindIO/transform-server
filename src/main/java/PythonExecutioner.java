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



    private void _exec_inputs(PythonVariables pyInputs){
        if (pyInputs == null){
            return;
        }
        Map<String, String> strInputs = pyInputs.getStrVariables();
        Map<String, Integer> intInputs = pyInputs.getIntVariables();
        Map<String, Double> floatInputs = pyInputs.getFloatVariables();

        String setcode = "";
        String[] VarNames = strInputs.keySet().toArray(new String[strInputs.size()]);
        for(Object varName: VarNames){
            String varValue = strInputs.get(varName);
            setcode += varName + " = \"" + varValue + "\";";
        }
        exec(setcode);

        setcode = "";
        VarNames = intInputs.keySet().toArray(new String[intInputs.size()]);
        for(String varName: VarNames){
            Integer varValue = intInputs.get(varName);
            setcode += varName + " = " + varValue.toString() + ";";
        }
        exec(setcode);

        setcode = "";
        VarNames = floatInputs.keySet().toArray(new String[floatInputs.size()]);
        for(String varName: VarNames){
            Double varValue = floatInputs.get(varName);
            setcode += varName + " = " + varValue.toString() + ";";
        }
        exec(setcode);
    }

    private void _exec_outputs(PythonVariables pyOutputs){
        if (pyOutputs == null){
            return;
        }
        String getcode = "import json;json.dump({";
        String[] VarNames = pyOutputs.getVariables();
        for (String varName: VarNames){
            getcode += "\"" + varName + "\"" + ":" + varName + ",";
        }
        getcode = getcode.substring(0, getcode.length() - 1);
        getcode += "},open('" + tempFile + "', 'w'));";

        exec(getcode);

        String out = read(tempFile);


        JSONParser parser = new JSONParser();
        try{
            JSONObject jsObject = (JSONObject) parser.parse(out);
            for (String varName: VarNames){
                Object varValue = jsObject.get(varName);
                pyOutputs.setValue(varName, varValue);

            }
        }
        catch (ParseException e){
            System.out.println(e);
        }


    }
    public void exec(String code, PythonVariables pyInputs, PythonVariables pyOutputs){
        _exec_inputs(pyInputs);
        exec(code);
        _exec_outputs(pyOutputs);
    }

    public void exec(List<String> code, PythonVariables pyInputs, PythonVariables pyOutputs){
        _exec_inputs(pyInputs);
        String x = "";
        for (String line: code){
            x += line + ";";
        }
        exec(x);
        _exec_outputs(pyOutputs);
    }


    public void exec(String[] code, PythonVariables pyInputs, PythonVariables pyOutputs){
        _exec_inputs(pyInputs);
        String x = "";
        for (String line: code){
            x += line + ";";
        }
        exec(x);
        _exec_outputs(pyOutputs);
    }

    public PythonVariables exec(PythonTransform transform) throws Exception{
        if (transform.getInputs() != null){
            throw new Exception("Required inputs not provided.");
        }
        exec(transform.getCode(), null, transform.getOutputs());
        return transform.getOutputs();
    }

    public PythonVariables exec(PythonTransform transform, PythonVariables inputs){
        exec(transform.getCode(), inputs, transform.getOutputs());
        return transform.getOutputs();
    }


    private static String read(String path){
        try{
            File file = new File(path);
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();
            String str = new String(data, "UTF-8");
            return str;
        }
        catch (Exception e){
            return "";
        }

    }



}
