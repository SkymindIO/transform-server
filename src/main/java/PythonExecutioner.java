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
    private static String tempFile = "tempfile.txt";
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



    private String inputCode(PythonVariables pyInputs){
        String inputCode = "loc={};";
        if (pyInputs == null){
            return inputCode;
        }
        Map<String, String> strInputs = pyInputs.getStrVariables();
        Map<String, Integer> intInputs = pyInputs.getIntVariables();
        Map<String, Double> floatInputs = pyInputs.getFloatVariables();
        Map<String, NumpyArray> ndInputs = pyInputs.getNDArrayVariables();


        String[] VarNames = strInputs.keySet().toArray(new String[strInputs.size()]);
        for(Object varName: VarNames){
            String varValue = strInputs.get(varName);
            inputCode += varName + " = \"" + varValue + "\";";
            inputCode += "loc['" + varName + "']=" + varName + ";";
        }

        VarNames = intInputs.keySet().toArray(new String[intInputs.size()]);
        for(String varName: VarNames){
            Integer varValue = intInputs.get(varName);
            inputCode += varName + " = " + varValue.toString() + ";";
            inputCode += "loc['" + varName + "']=" + varName + ";";
        }

        VarNames = floatInputs.keySet().toArray(new String[floatInputs.size()]);
        for(String varName: VarNames){
            Double varValue = floatInputs.get(varName);
            inputCode += varName + " = " + varValue.toString() + ";";
            inputCode += "loc['" + varName + "']=" + varName + ";";
        }

        if (ndInputs.size()> 0){
            inputCode += "import ctypes; import numpy as np;";
            VarNames = ndInputs.keySet().toArray(new String[ndInputs.size()]);
            for(String varName: VarNames){
                NumpyArray npArr = ndInputs.get(varName);
                String shapeStr = "(";
                for (long d: npArr.getShape()){
                    shapeStr += String.valueOf(d) + ",";
                }
                shapeStr += ")";
                String code;
                if (npArr.getDType() == NumpyArray.DType.FLOAT32){
                    code = "np.ctypeslib.as_array(ctypes.cast(" + String.valueOf(npArr.getAddress()) + ", ctypes.POINTER(ctypes.c_float))," + shapeStr + ")";
                }
                else{
                    code = "np.ctypeslib.as_array(ctypes.cast(" + String.valueOf(npArr.getAddress()) + ", ctypes.POINTER(ctypes.c_double))," + shapeStr + ")";
                }
                code = varName + "=" + code + ";";
                inputCode += code;
                inputCode += "loc['" + varName + "']=" + varName + ";";
            }

        }
        return inputCode;
    }

    private void _readOutputs(PythonVariables pyOutputs){
        if (pyOutputs == null){
            return;
        }
        String out = read(tempFile);
        String[] VarNames = pyOutputs.getVariables();
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
    private String outputCode(PythonVariables pyOutputs){
        if (pyOutputs == null){
            return "";
        }
        String outputCode = "import json;json.dump({";
        String[] VarNames = pyOutputs.getVariables();
        boolean ndarrayHelperAdded = false;
        for (String varName: VarNames){
            if (pyOutputs.getType(varName) == PythonVariables.Type.NDARRAY){
                if (! ndarrayHelperAdded){
                    ndarrayHelperAdded = true;
                    String helper = "serialize_ndarray_metadata=lambda x:{\"address\":x.__array_interface__['data'][0]" +
                            ",\"shape\":x.shape,\"strides\":x.strides,\"dtype\":str(x.dtype)};";
                    outputCode = helper + outputCode;
                }
                outputCode += "\"" + varName + "\"" + ":serialize_ndarray_metadata(" + varName + "),";

            }
            else {
                outputCode += "\"" + varName + "\"" + ":" + varName + ",";
            }
        }
        outputCode = outputCode.substring(0, outputCode.length() - 1);
        outputCode += "},open('" + tempFile + "', 'w'));";
        return outputCode;


    }


    public static void exec(String code){
        //code = RestrictedPython.getSafeCode(code);
        System.out.println(code);
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

    public void exec(String code, PythonVariables pyInputs, PythonVariables pyOutputs){
        String inputCode = inputCode(pyInputs);
        String outputCode = outputCode(pyOutputs);
        if (code.charAt(code.length() - 1) != ';'){
            code += ';';
        }
        code = RestrictedPython.getSafeCode(code);
        exec(inputCode + code + outputCode);
        _readOutputs(pyOutputs);
    }

    public void exec(List<String> code, PythonVariables pyInputs, PythonVariables pyOutputs){
        String inputCode = inputCode(pyInputs);
        String outputCode = outputCode(pyOutputs);
        String x = "";
        for (String line: code){
            if(line.charAt(line.length() - 1) != ';'){
                x += line + ';';
            }
            else{
                x += line;
            }
        }
        x = RestrictedPython.getSafeCode(x);
        exec(inputCode + x + outputCode);
        _readOutputs(pyOutputs);
    }


    public void exec(String[] code, PythonVariables pyInputs, PythonVariables pyOutputs){
        String inputCode = inputCode(pyInputs);
        String outputCode = outputCode(pyOutputs);
        String x = "";
        for (String line: code){
            if(line.charAt(line.length() - 1) != ';'){
                x += line + ';';
            }
            else{
                x += line;
            }
        }
        x = RestrictedPython.getSafeCode(x);
        exec(inputCode + x + outputCode);
        _readOutputs(pyOutputs);
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
