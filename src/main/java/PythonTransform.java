import javax.annotation.Nullable;

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
            x += line + ";";
        }
        this.name = name;
        this.code = x;
    }
    public PythonTransform(String name, List<String> code){
        String x = "";
        for (String line: code){
            x += line + ";";
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

    public String getCode(){
        return code;
    }

    public PythonVariables getInputs() {
        return pyInputs;
    }

    public PythonVariables getOutputs() {
        return pyOutputs;
    }
}
