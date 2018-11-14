import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import org.bytedeco.javacpp.*;
import static org.bytedeco.javacpp.python.*;


public class TransformServer{
    private static Pointer program_name;
    public static void main(String[] args){

        PythonExecutioner pyExec = new PythonExecutioner();
        PythonInputs pyInputs = new PythonInputs();
        PythonOutputs pyOutputs = new PythonOutputs();

        pyInputs.addStr("x", "Hello");
        pyInputs.addStr("y", "World");

        List<String> code = new ArrayList<String>();
        code.add("z = x + ' ' + y");

        pyOutputs.addStr("z");

        pyExec.exec(code, pyInputs, pyOutputs);

        String z = pyOutputs.getStrValue("z");

        System.out.println(z);

        pyExec.free();

    }

}

