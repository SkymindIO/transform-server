import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class TestSandbox {
    @Test
    public void testSandbox() throws Exception{
        String codeCreateList = "x=[]";
        String codeAppend = "x.append(0)";
        String output = "y=len(x)";

        PythonVariables pyOutputs = new PythonVariables();
        pyOutputs.addInt("y");
        PythonExecutioner pyExec = new PythonExecutioner();

        pyExec.setInterpreter("interpreter1");

        pyExec.exec(codeCreateList);

        pyExec.setInterpreter("interpreter2");

        pyExec.exec(codeCreateList);

        for (int i=0; i < 7; i++){
            pyExec.setInterpreter("interpreter1");
            pyExec.exec(codeAppend);
            pyExec.setInterpreter("interpreter2");
            pyExec.exec(codeAppend);
        }


        pyExec.setInterpreter("interpreter1");

        pyExec.exec(output, null, pyOutputs);
        int interpreter1_out = pyOutputs.getIntValue("y");
        pyExec.setInterpreter("interpreter2");
        pyExec.exec(output, null, pyOutputs);
        int interpreter2_out = pyOutputs.getIntValue("y");

        assertEquals(7, interpreter1_out);
        assertEquals(7, interpreter2_out);

    }
}
