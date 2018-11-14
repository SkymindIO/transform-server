import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import org.bytedeco.javacpp.*;
import static org.bytedeco.javacpp.python.*;


public class TransformServer{
    public static void main(String[] args){
        testStr();
        testInt();
    }
    public static void testStr(){

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
    public static void testInt(){
        PythonExecutioner pyExec = new PythonExecutioner();
        PythonInputs pyInputs = new PythonInputs();
        PythonOutputs pyOutputs = new PythonOutputs();

        pyInputs.addInt("x", 10);
        pyInputs.addInt("y", 20);

        List<String> code = new ArrayList<String>();
        code.add("z = x + y");

        pyOutputs.addInt("z");

        pyExec.exec(code, pyInputs, pyOutputs);

        int z = pyOutputs.getIntValue("z");

        System.out.println(z);

        pyExec.free();

    }

}

