import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class TestPythonExecutioner {

    @Test
    public void testStr(){

        PythonExecutioner pyExec = new PythonExecutioner();
        PythonVariables pyInputs = new PythonVariables();
        PythonVariables pyOutputs = new PythonVariables();

        pyInputs.addStr("x", "Hello");
        pyInputs.addStr("y", "World");

        List<String> code = new ArrayList<String>();
        code.add("z = x + ' ' + y");

        pyOutputs.addStr("z");

        pyExec.exec(code, pyInputs, pyOutputs);

        String z = pyOutputs.getStrValue("z");

        System.out.println(z);

        pyExec.free();

        assertEquals("Hello World", z);

    }

    @Test
    public void testInt(){
        PythonExecutioner pyExec = new PythonExecutioner();
        PythonVariables pyInputs = new PythonVariables();
        PythonVariables pyOutputs = new PythonVariables();

        pyInputs.addInt("x", 10);
        pyInputs.addInt("y", 20);

        List<String> code = new ArrayList<String>();
        code.add("z = x + y");

        pyOutputs.addInt("z");

        pyExec.exec(code, pyInputs, pyOutputs);

        int z = pyOutputs.getIntValue("z");

        pyExec.free();

        assertEquals(30, z);

    }
}
