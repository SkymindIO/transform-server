import fi.iki.elonen.NanoHTTPD;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

import org.bytedeco.javacpp.*;
import static org.bytedeco.javacpp.python.*;



public class TransformServer extends NanoHTTPD{

    private Map<String, PythonTransform> transforms = new HashMap<String, PythonTransform>();
    private PythonExecutioner pythonExecutioner = new PythonExecutioner("server_executioner");
    private static JSONParser parser = new JSONParser();
    private String uploadsDirPath;
    private Map<String, String> uploads = new HashMap<String, String>();

    private void setupDirs(){
        String userDirPath = System.getProperty("user.home");
        String dl4jDirPath = Paths.get(userDirPath, ".deeplearning4j").toString();
        File dl4jDir = new File(dl4jDirPath);
        if (!(dl4jDir.exists() && dl4jDir.isDirectory())){
            dl4jDir.mkdir();
        }
        String transformServerDirPath = Paths.get(dl4jDirPath, "transform-server").toString();
        File transformServerDir = new File(transformServerDirPath);
        if (!(transformServerDir.exists()&& transformServerDir.isDirectory())){
            transformServerDir.mkdir();
        }
        uploadsDirPath = Paths.get(transformServerDirPath, "uploads").toString();
        try{
            FileUtils.deleteDirectory(new File(uploadsDirPath));
        }
        catch (IOException ioe){
            System.out.println(ioe.toString());
        }
        File uploadsDir = new File(uploadsDirPath);
        uploadsDir.mkdir();
    }

    private String getUploadsDir(String transformName){
        String dirPath = Paths.get(uploadsDirPath, transformName).toString();
        File dir = new File(dirPath);
        if (!(dir.exists() && dir.isDirectory())){
            dir.mkdir();
        }
        return dirPath;
    }
    public static void main(String args[]) throws Exception{
        new TransformServer();

    }

    public TransformServer(int port) throws IOException{
        super(port);
        setupDirs();
        start();

    }
    public TransformServer(int port, boolean start) throws IOException{
        super(port);
        setupDirs();
        if(start){
            this.start();
        }
    }

    public TransformServer(boolean start) throws IOException{
        super(8080);
        setupDirs();
        if (start){
            this.start();
        }
    }
    public TransformServer() throws IOException{
        super(8080);
        setupDirs();
        start();
    }

    public void start() throws IOException{
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("Server started at " + getListeningPort());
    }

    private String readTXT(String file) throws IOException{
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder stringBuilder = new StringBuilder();
        char[] buffer = new char[10];
        while (reader.read(buffer) != -1) {
            stringBuilder.append(new String(buffer));
            buffer = new char[10];
        }
        reader.close();
        return stringBuilder.toString();
    }

    public Response add(String name, String code, String inputStr, String outputStr){
        if (code == null){
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "No code!");
        }
        if (name == null){
            name = "default_transform";
        }
        if (name.contains(":")){
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Illegal character \":\" in transform name.");
        }
        if (uploads.containsKey(name + ":" + code)){
            try {
                code = readTXT(uploads.get(name + ":" + code));
                System.out.println("Code read from file.");
                //System.out.println(code);
            }
            catch (IOException ioe){
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "IO Error.");
            }
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
                    else if (varType.equals("list")){
                        pyInputs.addList(varName);

                    }
                    else if (varType.equals("file")){
                        pyInputs.addFile(varName);
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
                    else if (varType.equals("list")){
                        pyOutputs.addList(varName);
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

    public Response delete(String name){
        if (!transforms.containsKey(name)){
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Transform not found: " + name + ".");
        }
        transforms.remove(name);
        return newFixedLengthResponse(Response.Status.OK, "text/plain","Transform deleted");
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
                            long[] shape = new long[shapeArr.size()];
                            for(int i=0; i<shape.length; i++){
                                shape[i] = (Long)shapeArr.get(i);
                            }
                            String dtype = (String)arr.get("dtype");
                            if (dtype == null){
                                Object firstElem = dataArr.get(0);
                                if (firstElem instanceof Double){
                                    dtype = "double";
                                }
                                else if (firstElem instanceof Long){
                                    dtype = "long";
                                }
                                else{
                                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Illegal object in array: " + firstElem.toString() + ".");
                                }
                            }
                            else{
                                dtype = dtype.toLowerCase();
                            }

                            if (dtype.equals("float32") || dtype.equals("float")){
                                float[] data = new float[dataArr.size()];
                                for(int i=0; i<data.length; i++){
                                    data[i] = ((Double)dataArr.get(i)).floatValue();
                                }

                                INDArray indArray = Nd4j.create(data, shape);
                                pyInputs.addNDArray(varName, indArray);
                            }
                            else if (dtype.equals("float64") || dtype.equals("double")){
                                double[] data = new double[dataArr.size()];
                                for (int i=0; i<data.length; i++){
                                    data[i] = (Double)dataArr.get(i);
                                }

                                INDArray indArray = Nd4j.create(data, shape);
                                pyInputs.addNDArray(varName, indArray);
                            }
                            else if (dtype.equals("int16") || dtype.equals("short")){
                                short[] data = new short[dataArr.size()];
                                for (int i=0; i<data.length; i++){
                                    data[i] = ((Long)dataArr.get(i)).shortValue();
                                }

                                INDArray indArray = Nd4j.create(data, shape, DataType.SHORT);
                                pyInputs.addNDArray(varName, indArray);
                            }
                            else if (dtype.equals("int32") || dtype.equals("int")){
                                int[] data = new int[dataArr.size()];
                                for (int i=0; i<data.length; i++){
                                    data[i] = ((Long)dataArr.get(i)).intValue();
                                }

                                INDArray indArray = Nd4j.create(data, shape, DataType.INT);
                                pyInputs.addNDArray(varName, indArray);
                            }
                            else if (dtype.equals("int64") || dtype.equals("long")){
                                long[] data = new long[dataArr.size()];
                                for (int i=0; i<data.length; i++){
                                    data[i] = ((Long)dataArr.get(i));
                                }

                                INDArray indArray = Nd4j.create(data, shape, DataType.LONG);
                                pyInputs.addNDArray(varName, indArray);
                            }
                        }
                        else if (varType == PythonVariables.Type.LIST){
                            Object[] list = ((JSONArray)jsonObject.get(varName)).toArray();
                            pyInputs.addList(varName, list);
                        }
                        else if (varType == PythonVariables.Type.FILE){
                            String fileName = (String)jsonObject.get(varName);
                            String filePath = uploads.get(name + ":" + fileName);
                            if (filePath == null){
                                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "File not uploaded: " + fileName + ".");
                            }
                            pyInputs.addFile(varName, filePath);
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
                    else if (outputs.getType(varName) == PythonVariables.Type.LIST){
                        JSONArray arr = new JSONArray();
                        Object objArr[] = outputs.getListValue(varName);
                        for (Object obj: objArr){
                            arr.add(obj);
                        }
                        jsonObject.put(varName, arr);
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

    private String upload(String transformName, String fileName, String tempFilePath)throws IOException{
        // TODO: deletion policy
        String dir = getUploadsDir(transformName);
        String targetPath = Paths.get(dir, fileName).toString();
        Files.copy(Paths.get(tempFilePath), Paths.get(targetPath), StandardCopyOption.REPLACE_EXISTING);
        return targetPath;
    }
    @Override
    public Response serve(IHTTPSession session){
        String route = session.getUri().toLowerCase();
        Method method = session.getMethod();
        Map<String, String> params = session.getParms();
        int numUploaded = 0;
        if ((route.equals("/exec")|| route.equals("/upload")) && (Method.POST.equals(method) || Method.PUT.equals(method))) {
            Map<String, String> files = new HashMap<String, String>();
            try {
                session.parseBody(files);
            } catch (IOException ioe) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "IO Error");
            } catch (ResponseException re) {
                return newFixedLengthResponse(re.getStatus(), MIME_PLAINTEXT, re.getMessage());
            }
            for (Map.Entry<String, String> entry: files.entrySet()){
                String fileName = entry.getKey();
                String tempFilePath = entry.getValue();
                String name = params.get("name");
                if (name == null){
                    name = "default_transform";
                }
                String newFilePath;
                try {
                    newFilePath = upload(name, fileName, tempFilePath);
                }
                catch (IOException ioe){
                    return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "IO Error");
                }
                uploads.put(name + ":" + fileName, newFilePath);
                System.out.println(fileName + " uploaded");
                numUploaded++;
            }
        }
        if (route.equals("/add")){
            String name = params.get("name");
            String code = params.get("code");
            String inputStr = params.get("input");
            String outputStr = params.get("output");
            return add(name, code, inputStr, outputStr);
        }
        else if (route.equals("/exec")){
            String name = params.get("name");
            String inputs = params.get("input");
            return exec(name, inputs);
        }
        else if (route.equals("/delete")){
            String name = params.get("name");
            return delete(name);
        }
        else if (route.equals("/upload")){
            if (method.equals(Method.POST) || (method.equals(Method.PUT))){
                return newFixedLengthResponse(Response.Status.OK, "text/plain", String.valueOf(numUploaded) + " files uploaded.");
            }
            else{
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Use POST to upload files.");
            }
        }
        else{
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Unhandled route: " + route);
        }

    }

}

