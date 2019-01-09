import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class TestPythonExecutioner {

    @Test
    public void testStr() throws Exception{

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
    public void testInt()throws Exception{
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

    @Test
    public void testList() throws Exception{
        PythonExecutioner pyExec = new PythonExecutioner();
        PythonVariables pyInputs = new PythonVariables();
        PythonVariables pyOutputs = new PythonVariables();

        Object[] x = new Object[]{1L, 2L, 3L, "a", "b", "c"};
        Object[] y = new Object[]{4L, 5L, 6L, "d", "e", "f"};

        pyInputs.addList("x", x);
        pyInputs.addList("y", y);

        String code = "z = x + y";

        pyOutputs.addList("z");

        pyExec.exec(code, pyInputs, pyOutputs);

        Object[] z = pyOutputs.getListValue("z");

        pyExec.free();

        assertEquals(z.length, x.length + y.length);

        for (int i=0; i < x.length; i++){
            assertEquals(x[i], z[i]);
        }
        for (int i=0; i<y.length; i++){
            assertEquals(y[i], z[x.length + i]);
        }

    }

    @Test
    public void testNDArrayFloat()throws Exception{
        PythonExecutioner pyExec = new PythonExecutioner();
        PythonVariables pyInputs = new PythonVariables();
        PythonVariables pyOutputs = new PythonVariables();

        pyInputs.addNDArray("x", Nd4j.zeros(DataType.FLOAT, 2, 3));
        pyInputs.addNDArray("y", Nd4j.ones(DataType.FLOAT, 2, 3));
        pyOutputs.addNDArray("z");

        String code = "z = x + y";

        pyExec.exec(code, pyInputs, pyOutputs);

        INDArray z = pyOutputs.getNDArrayValue("z").getND4JArray();

        assertEquals(6.0, z.sum().getDouble(0));
        pyExec.free();

    }

    @Test
    public void testNDArrayDouble()throws Exception{
        PythonExecutioner pyExec = new PythonExecutioner();
        PythonVariables pyInputs = new PythonVariables();
        PythonVariables pyOutputs = new PythonVariables();

        pyInputs.addNDArray("x", Nd4j.zeros(DataType.DOUBLE, 2, 3));
        pyInputs.addNDArray("y", Nd4j.ones(DataType.DOUBLE, 2, 3));
        pyOutputs.addNDArray("z");

        String code = "z = x + y";

        pyExec.exec(code, pyInputs, pyOutputs);

        INDArray z = pyOutputs.getNDArrayValue("z").getND4JArray();

        assertEquals(6.0, z.sum().getDouble(0));
        pyExec.free();

    }

    @Test
    public void testNDArrayShort()throws Exception{
        PythonExecutioner pyExec = new PythonExecutioner();
        PythonVariables pyInputs = new PythonVariables();
        PythonVariables pyOutputs = new PythonVariables();

        pyInputs.addNDArray("x", Nd4j.zeros(DataType.SHORT, 2, 3));
        pyInputs.addNDArray("y", Nd4j.ones(DataType.SHORT, 2, 3));
        pyOutputs.addNDArray("z");

        String code = "z = x + y";

        pyExec.exec(code, pyInputs, pyOutputs);

        INDArray z = pyOutputs.getNDArrayValue("z").getND4JArray();

        assertEquals(6.0, z.sum().getDouble(0));
        pyExec.free();

    }


    @Test
    public void testNDArrayInt()throws Exception{
        PythonExecutioner pyExec = new PythonExecutioner();
        PythonVariables pyInputs = new PythonVariables();
        PythonVariables pyOutputs = new PythonVariables();

        pyInputs.addNDArray("x", Nd4j.zeros(DataType.INT, 2, 3));
        pyInputs.addNDArray("y", Nd4j.ones(DataType.INT, 2, 3));
        pyOutputs.addNDArray("z");

        String code = "z = x + y";

        pyExec.exec(code, pyInputs, pyOutputs);

        INDArray z = pyOutputs.getNDArrayValue("z").getND4JArray();

        assertEquals(6.0, z.sum().getDouble(0));
        pyExec.free();

    }

    @Test
    public void testNDArrayLong()throws Exception{
        PythonExecutioner pyExec = new PythonExecutioner();
        PythonVariables pyInputs = new PythonVariables();
        PythonVariables pyOutputs = new PythonVariables();

        pyInputs.addNDArray("x", Nd4j.zeros(DataType.LONG, 2, 3));
        pyInputs.addNDArray("y", Nd4j.ones(DataType.LONG, 2, 3));
        pyOutputs.addNDArray("z");

        String code = "z = x + y";

        pyExec.exec(code, pyInputs, pyOutputs);

        INDArray z = pyOutputs.getNDArrayValue("z").getND4JArray();

        assertEquals(6.0, z.sum().getDouble(0));
        pyExec.free();

    }

}
