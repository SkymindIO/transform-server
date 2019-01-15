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

    private static String readTXT(String file) throws IOException{
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

    public static PythonTransform load(String filePath) throws IOException, ParseException{
        String jsonString = readTXT(filePath);
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(jsonString);
        String code = (String)jsonObject.get("code");
        String name = (String)jsonObject.get("name");
        PythonVariables pyInputs = PythonVariables.fromJSON((JSONArray) jsonObject.get("inputs"));
        PythonVariables pyOutputs = PythonVariables.fromJSON((JSONArray) jsonObject.get("outputs"));
        return new PythonTransform(name, code, pyInputs, pyOutputs);
    }

    public void save(String filePath) throws IOException{
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", code);
        jsonObject.put("name", name);
        JSONArray inputs = pyInputs.toJSON();
        jsonObject.put("inputs", inputs);
        JSONArray outputs = pyOutputs.toJSON();
        jsonObject.put("outputs", outputs);
        String jsonString = jsonObject.toJSONString();
        FileWriter fw = new FileWriter(filePath);
        fw.write(jsonString);
    }
}
