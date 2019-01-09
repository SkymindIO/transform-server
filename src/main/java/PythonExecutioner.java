import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

import org.bytedeco.javacpp.*;
import static org.bytedeco.javacpp.python.*;

import org.nd4j.linalg.api.buffer.DataType;

public class PythonExecutioner {
    private String name;
    private Pointer namePtr;
    private boolean restricted = false;
    private PyObject module;
    private PyObject globals;

    public PyObject getGlobals(){
        return globals;
    }

    public void setRestricted(boolean restricted) {
        this.restricted = restricted;
    }

    public boolean getRestricted(){
        return restricted;
    }

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
        module = PyImport_AddModule("__main__");
        globals = PyModule_GetDict(module);
    }

    public void free(){
        if (Py_FinalizeEx() < 0) {
            throw new RuntimeException("Python execution failed.");
        }
        PyMem_RawFree(namePtr);
    }



    private String inputCode(PythonVariables pyInputs)throws Exception{
        String inputCode = "loc={};";
        if (pyInputs == null){
            return inputCode;
        }
        Map<String, String> strInputs = pyInputs.getStrVariables();
        Map<String, Integer> intInputs = pyInputs.getIntVariables();
        Map<String, Double> floatInputs = pyInputs.getFloatVariables();
        Map<String, NumpyArray> ndInputs = pyInputs.getNDArrayVariables();


        String[] VarNames;


        VarNames = strInputs.keySet().toArray(new String[strInputs.size()]);
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

            String converter = "__arr_converter = lambda addr, shape, type: np.ctypeslib.as_array(ctypes.cast(addr, ctypes.POINTER(type)), shape);";
            inputCode += converter;
            for(String varName: VarNames){
                NumpyArray npArr = ndInputs.get(varName);
                String shapeStr = "(";
                for (long d: npArr.getShape()){
                    shapeStr += String.valueOf(d) + ",";
                }
                shapeStr += ")";
                String code;
                String ctype;
                if (npArr.getDType() == DataType.FLOAT){

                    ctype = "ctypes.c_float";
                }
                else if (npArr.getDType() == DataType.DOUBLE){
                    ctype = "ctypes.c_double";
                }
                else if (npArr.getDType() == DataType.SHORT){
                    ctype = "ctypes.c_int16";
                }
                else if (npArr.getDType() == DataType.INT){
                    ctype = "ctypes.c_int32";
                }
                else if (npArr.getDType() == DataType.LONG){
                    ctype = "ctypes.c_int64";
                }
                else{
                    throw new Exception("Unsupported data type: " + npArr.getDType().toString() + ".");
                }

                code = "__arr_converter(" + String.valueOf(npArr.getAddress()) + "," + shapeStr + "," + ctype + ")";
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
        try{
            for (String varName: pyOutputs.getVariables()){
                PythonVariables.Type type = pyOutputs.getType(varName);
                if (type == PythonVariables.Type.STR){
                    pyOutputs.setValue(varName, evalSTRING(varName));
                }
                else if(type == PythonVariables.Type.FLOAT){
                    pyOutputs.setValue(varName, evalFLOAT(varName));
                }
                else if(type == PythonVariables.Type.INT){
                    pyOutputs.setValue(varName, evalINTEGER(varName));
                }
                else{
                    pyOutputs.setValue(varName, evalNDARRAY(varName));
                }
            }
        }
        catch (Exception e){

        }
    }
    private String outputCode(PythonVariables pyOutputs){
        return "";
        /*
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

    */
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

    public void exec(String code, PythonVariables pyInputs, PythonVariables pyOutputs) throws Exception{
        String inputCode = inputCode(pyInputs);
        String outputCode = outputCode(pyOutputs);
        if (code.charAt(code.length() - 1) != ';'){
            code += ';';
        }
        if(restricted){
            code = RestrictedPython.getSafeCode(code);
        }
        exec(inputCode + code + outputCode);
        _readOutputs(pyOutputs);
    }

    public void exec(List<String> code, PythonVariables pyInputs, PythonVariables pyOutputs)throws Exception{
        String inputCode = inputCode(pyInputs);
        String x = "";
        for (String line: code){
            if(line.charAt(line.length() - 1) != ';'){
                x += line + ';';
            }
            else{
                x += line;
            }
        }
        if (restricted){
            x = RestrictedPython.getSafeCode(x);
        }
        exec(inputCode + x);
        _readOutputs(pyOutputs);
    }


    public void exec(String[] code, PythonVariables pyInputs, PythonVariables pyOutputs)throws Exception{
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
        if (restricted){
            x = RestrictedPython.getSafeCode(x);
        }
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

    public PythonVariables exec(PythonTransform transform, PythonVariables inputs)throws Exception{
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

    public String evalSTRING(String varName){
        PyObject xObj = PyDict_GetItemString(globals, varName);
        PyObject bytes = PyUnicode_AsEncodedString(xObj, "UTF-8", "strict");
        BytePointer bp = PyBytes_AsString(bytes);
        String ret = bp.getString();
        Py_DecRef(xObj);
        Py_DecRef(bytes);
        return ret;
    }

    public long evalINTEGER(String varName){
        PyObject xObj = PyDict_GetItemString(globals, varName);
        long ret = PyLong_AsLongLong(xObj);
        return ret;
    }

    public double evalFLOAT(String varName){
        PyObject xObj = PyDict_GetItemString(globals, varName);
        double ret = PyFloat_AsDouble(xObj);
        return ret;
    }

    public NumpyArray evalNDARRAY(String varName) throws Exception{
        PyObject xObj = PyDict_GetItemString(globals, varName);
        PyObject arrayInterface = PyObject_GetAttrString(xObj, "__array_interface__");
        PyObject data = PyDict_GetItemString(arrayInterface, "data");
        PyObject zero = PyLong_FromLong(0);
        PyObject addressObj = PyObject_GetItem(data, zero);
        long address = PyLong_AsLongLong(addressObj);
        PyObject shapeObj = PyObject_GetAttrString(xObj, "shape");
        int ndim = (int)PyObject_Size(shapeObj);
        PyObject iObj;
        long shape[] = new long[ndim];
        for (int i=0; i<ndim; i++){
            iObj = PyLong_FromLong(i);
            PyObject sizeObj = PyObject_GetItem(shapeObj, iObj);
            long size = PyLong_AsLongLong(sizeObj);
            shape[i] = size;
            Py_DecRef(iObj);
        }
        
        PyObject stridesObj = PyObject_GetAttrString(xObj, "strides");
        long strides[] = new long[ndim];
        for (int i=0; i<ndim; i++){
            iObj = PyLong_FromLong(i);
            PyObject strideObj = PyObject_GetItem(stridesObj, iObj);
            long stride = PyLong_AsLongLong(strideObj);
            strides[i] = stride;
            Py_DecRef(iObj);
        }       

        PyObject dtypeObj = PyObject_GetAttrString(xObj, "dtype");
        PyObject dtypeNameObj = PyObject_GetAttrString(dtypeObj, "name");
        PyObject bytes = PyUnicode_AsEncodedString(dtypeNameObj, "UTF-8", "strict");
        BytePointer bp = PyBytes_AsString(bytes);
        String dtypeName = bp.getString();
        DataType dtype;
        if (dtypeName.equals("float64")){
            dtype = DataType.DOUBLE;
        }
        else if (dtypeName.equals("float32")){
            dtype = DataType.FLOAT;
        }
        else if (dtypeName.equals("int16")){
            dtype = DataType.SHORT;
        }
        else if (dtypeName.equals("int32")){
            dtype = DataType.INT;
        }
        else if (dtypeName.equals("int64")){
            dtype = DataType.LONG;
        }
        else{
            throw new Exception("Unsupported array type " + dtypeName + ".");
        }
        NumpyArray ret = new NumpyArray(address, shape, strides, dtype);
        Py_DecRef(arrayInterface);
        Py_DecRef(data);
        Py_DecRef(zero);
        Py_DecRef(addressObj);
        Py_DecRef(shapeObj);
        Py_DecRef(stridesObj);


       return ret;
    }

}
