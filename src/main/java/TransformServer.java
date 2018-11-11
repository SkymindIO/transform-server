import java.util.ArrayList;
import java.util.List;
import org.bytedeco.javacpp.*;
import static org.bytedeco.javacpp.python.*;

public class TransformServer {
    private static Pointer program_name;
    public static void main(String[] args) {
        init();
        List<String> code = new ArrayList<String>();
        code.add("import numpy as np");
        code.add("x = np.zeros((32, 10))");
        code.add("y = np.ones((1, 10))");
        code.add("z = x + y");
        code.add("print(z.shape)");
        exec(code);
        free();
    }


    public static void exec(String code){
        PyRun_SimpleStringFlags(code, null);
        if (Py_FinalizeEx() < 0) {
            throw new RuntimeException("Python execution failed.");
        }
    }

    public static void exec(List<String> code){
        String x = "";
        for (String line: code){
            x += line + ";";
        }
        exec(x);
    }

    public static  void exec(String[] code){
        String x = "";
        for (String line: code){
            x += line + ";";
        }
        exec(x);
    }

    public static void init(){
        program_name = Py_DecodeLocale(TransformServer.class.getSimpleName(), null);
        Py_SetProgramName(program_name);
        Py_Initialize();
    }

    public static void free(){
        PyMem_RawFree(program_name);
    }
}

