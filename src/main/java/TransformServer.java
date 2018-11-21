import fi.iki.elonen.NanoHTTPD;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bytedeco.javacpp.*;
import static org.bytedeco.javacpp.python.*;



public class TransformServer extends NanoHTTPD{

    private Map<String, PythonTransform> transforms = new HashMap<String, PythonTransform>();
    private PythonExecutioner pythonExecutioner = new PythonExecutioner();
    private static JSONParser parser = new JSONParser();
    public TransformServer(int port) throws IOException{
        super(port);
        start();
    }
    public TransformServer(int port, boolean start) throws IOException{
        super(port);
        if(start){
            this.start();
        }
    }
    public static void testNDArray()throws Exception{
        PythonExecutioner pyExec = new PythonExecutioner();
        PythonVariables pyInputs = new PythonVariables();
        PythonVariables pyOutputs = new PythonVariables();

        pyInputs.addNDArray("x", Nd4j.zeros(2, 3));
        pyInputs.addNDArray("y", Nd4j.ones(2, 3));
        pyOutputs.addNDArray("z");

        String code = "z = x + y";

        pyExec.exec(code, pyInputs, pyOutputs);

        INDArray z = pyOutputs.getNDArrayValue("z").getND4JArray();

        //assertEquals(6.0, z.sum().getDouble(0));
        pyExec.free();

    }

    public TransformServer(boolean start) throws IOException{
        super(8080);
        if (start){
            this.start();
        }
    }
    public TransformServer() throws IOException{
        super(8080);
        start();
    }

    public void start() throws IOException{
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("Server started at " + getListeningPort());
    }

    public static void main(String args[]) throws Exception{
        testNDArray();
        TransformServer server = new TransformServer(8000);

    }

    public Response add(String name, String code, String inputStr, String outputStr){
        if (code == null){
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "No code!");
        }
        if (name == null){
            name = "default_transform";
        }
        PythonVariables pyInputs = null;
        if (inputStr != null){
            try{
                pyInputs = new PythonVariables();
                JSONObject jsonObject = (JSONObject)parser.parse(inputStr);
                String[] varNames = new String[jsonObject.size()];
                varNames = (String[])jsonObject.keySet().toArray(varNames);
                for(String varName: varNames){
                    String varType = (String)jsonObject.get(varName);
                    if (varType.equals("str")){
                        pyInputs.addStr(varName);
                    }
                    else if (varType.equals("int")){
                        pyInputs.addInt(varName);
                    }
                    else if (varType.equals("float")){
                        pyInputs.addFloat(varName);
                    }
                    else if (varType.equals("ndarray")){
                        pyInputs.addNDArray(varName);
                    }
                    else{
                        return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Unsupported python type:" + varType);
                    }
                }
            }
            catch (ParseException e){
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Bad input json: " + inputStr);
            }
        }
        PythonVariables pyOutputs = null;
        if (outputStr != null){
            try{
                pyOutputs = new PythonVariables();
                JSONObject jsonObject = (JSONObject)parser.parse(outputStr);
                String[] varNames = new String[jsonObject.size()];
                varNames = (String[])jsonObject.keySet().toArray(varNames);
                for(String varName: varNames){
                    String varType = (String)jsonObject.get(varName);
                    if (varType.equals("str")){
                        pyOutputs.addStr(varName);
                    }
                    else if (varType.equals("int")){
                        pyOutputs.addInt(varName);
                    }
                    else if (varType.equals("float")){
                        pyOutputs.addFloat(varName);
                    }
                    else if (varType.equals("ndarray")){
                        pyOutputs.addNDArray(varName);
                    }
                    else{
                        return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Unsupported python type:" + varType);
                    }
                }
            }
            catch(ParseException e){
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Bad output json: " + inputStr);
            }
        }
        PythonTransform transform = new PythonTransform(name, code, pyInputs, pyOutputs);
        if (transforms.containsKey(name)){
            transforms.put(name, transform);
            return newFixedLengthResponse(Response.Status.OK, "text/plain", "Transform updated: " + name);
        }
        else {
            transforms.put(name, transform);
            return newFixedLengthResponse(Response.Status.OK, "text/plain", "Transform added: " + name);
        }
    }

    public Response exec(@Nullable String name, @Nullable String inputStr){

        if (name == null){
            if (transforms.size() == 0){
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "No transforms added.");
            }
            name = "default_transform";
            if (!transforms.containsKey(name)){
                if (transforms.size() == 1){
                    name = (String)transforms.keySet().toArray()[0];
                }
                else {
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Multiple transforms added. Name required.");
                }
            }
        }
        PythonTransform transform = transforms.get(name);
        if (transform == null){
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Transform not found: " + name);
        }
        PythonVariables pyInputs = null;
        if (transform.getInputs() != null){
            pyInputs = new PythonVariables();
            if (inputStr != null){
                try{
                    JSONObject jsonObject = (JSONObject)parser.parse(inputStr);
                    String[] varNames = new String[jsonObject.size()];
                    varNames = (String[])jsonObject.keySet().toArray(varNames);
                    for(String varName: varNames){
                        PythonVariables.Type varType = transform.getInputs().getType(varName);
                        if (varType == null){
                            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Unexpected input:" + varName);
                        }
                        if (varType == PythonVariables.Type.STR){
                            pyInputs.addStr(varName, (String)jsonObject.get(varName));
                        }
                        else if (varType == PythonVariables.Type.INT){
                            pyInputs.addInt(varName, ((Long)jsonObject.get(varName)).intValue());
                        }
                        else if (varType == PythonVariables.Type.FLOAT){
                            pyInputs.addFloat(varName, (Double)jsonObject.get(varName));
                        }
                        else if (varType == PythonVariables.Type.NDARRAY){
                            JSONObject arr = (JSONObject)jsonObject.get(varName);
                            JSONArray dataArr = (JSONArray)arr.get("data");
                            JSONArray shapeArr = (JSONArray)arr.get("shape");
                            double[] data = new double[dataArr.size()];
                            for (int i=0; i<data.length; i++){
                                data[i] = (Double)dataArr.get(i);
                            }
                            long[] shape = new long[shapeArr.size()];
                            for(int i=0; i<shape.length; i++){
                                shape[i] = (Long)shapeArr.get(i);
                            }
                            INDArray indarray = Nd4j.create(data, shape);

                            pyInputs.addNDArray(varName, indarray);
                        }
                    }
                }
                catch (ParseException e){
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Bad input json: " + inputStr);
                }

            }
        }
        try{
            System.out.println("running python code...");
            PythonVariables outputs = pythonExecutioner.exec(transform, pyInputs);
            if (outputs != null){
                JSONObject jsonObject = new JSONObject();
                for (String varName: outputs.getVariables()){
                    if (outputs.getType(varName) == PythonVariables.Type.NDARRAY){
                        jsonObject.put(varName, outputs.getNDArrayValue(varName).toJSON());
                    }
                    else{
                        jsonObject.put(varName, outputs.getValue(varName));
                    }
                }
                return newFixedLengthResponse(Response.Status.OK, "text/plain", jsonObject.toJSONString());
            }
            else{
                return newFixedLengthResponse(Response.Status.OK, "text/plain", "");
            }
        }
        catch (Exception e){
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Execution failed: " + e.toString());
        }

    }

    @Override
    public Response serve(IHTTPSession session) {

        String route = session.getUri();
        System.out.println(route);
        Map<String, String> params = session.getParms();
        if (route.equals("/add")){
            String name = params.get("name");
            String code = params.get("code");
            String inputStr = params.get("input");
            String outputStr = params.get("output");
            return add(name, code, inputStr, outputStr);
        }
        else if (route.equals("/exec")){
            String name = params.get("name");
            String inputs = params.get("inputStr");
            return exec(name, inputs);
        }
        else{
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Unhandled route: " + route);
        }

    }

}

