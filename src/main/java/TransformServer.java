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
        List<String> code = new ArrayList<String>();
        code.add("import numpy as np");
        code.add("x = np.zeros((32, 10))");
        code.add("y = np.ones((1, 10))");
        code.add("z = x + y");
        code.add("print(z.shape)");
        pyExec.free();

    }


}

