import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.annotation.Nullable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class PythonTransform {
    private String code;
    private String name;
    private PythonVariables pyInputs;
    private PythonVariables pyOutputs;

    public PythonTransform(String name, String code){
        this.name = name;
        this.code = code;
    }
    public PythonTransform(String name, String[] code){
        String x = "";
        for (String line: code){
            x += line + "\n";
        }
        this.name = name;
        this.code = x;
    }
    public PythonTransform(String name, List<String> code){
        String x = "";
        for (String line: code){
            x += line + "\n";
        }
        this.name = name;
        this.code = x;
    }

    public PythonTransform(String name, String code, @Nullable PythonVariables pyInputs, @Nullable PythonVariables pyOutputs){
        this.name = name;
        this.code = code;
        this.pyInputs = pyInputs;
        this.pyOutputs = pyOutputs;
    }
    public PythonTransform(String name, String[] code, @Nullable PythonVariables pyInputs, @Nullable PythonVariables pyOutputs){
        String x = "";
        for (String line: code){
            x += line + ";";
        }
        this.name = name;
        this.code = x;
        this.pyInputs = pyInputs;
        this.pyOutputs = pyOutputs;
    }
    public PythonTransform(String name, List<String> code, @Nullable PythonVariables pyInputs, @Nullable PythonVariables pyOutputs){
        String x = "";
        for (String line: code){
            x += line + ";";
        }
        this.name = name;
        this.code = x;
        this.pyInputs = pyInputs;
        this.pyOutputs = pyOutputs;
    }

    public String getName(){
        return name;
    }
    public String getCode(){
        return code;
    }

    public PythonVariables getInputs() {
        return pyInputs;
    }

    public PythonVariables getOutputs() {
        return pyOutputs;
    }

    public static PythonTransform load(String filePath) throws IOException, ParseException{
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(filePath));
        String code = (String)jsonObject.get("code");
        String name = (String)jsonObject.get("name");
        PythonVariables pyInputs;
        JSONArray inputsArr = (JSONArray) jsonObject.get("inputs");
        if (inputsArr == null){
            pyInputs = null;
        }
        else{
            pyInputs = PythonVariables.fromJSON(inputsArr);
        }
        PythonVariables pyOutputs;
        JSONArray outputsArr = (JSONArray) jsonObject.get("outputs");
        if (outputsArr == null){
            pyOutputs = null;
        }
        else{
            pyOutputs = PythonVariables.fromJSON(outputsArr);
        }
        return new PythonTransform(name, code, pyInputs, pyOutputs);
    }

    public void save(String filePath) throws IOException{
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", code);
        jsonObject.put("name", name);
        JSONArray inputs;
        if (pyInputs == null){
            inputs = null;
        }
        else{
            inputs = pyInputs.toJSON();
        }
        jsonObject.put("inputs", inputs);
        JSONArray outputs;
        if (pyOutputs == null){
            outputs = null;
        }
        else{
            outputs = pyOutputs.toJSON();
        }
        jsonObject.put("outputs", outputs);
        String jsonString = jsonObject.toJSONString();
        FileWriter fw = new FileWriter(filePath);
        fw.write(jsonString);
        fw.close();
    }
}
