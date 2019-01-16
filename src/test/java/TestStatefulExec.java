import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import static org.junit.jupiter.api.Assertions.assertEquals;



public class TestStatefulExec {

    @Test
    public void testStatefulExec() throws Exception{
            List<String> code = new ArrayList<String>();

            code.add("#<SETUP>");

            code.add("import tensorflow as tf");
            code.add("from tensorflow.keras import Sequential");
            code.add("from tensorflow.keras.layers import Dense");
            code.add("model = Sequential()");
            code.add("model.add(Dense(10, input_dim=10))");
            code.add("model.add(Dense(5))");

            code.add("#</SETUP>");

            code.add("y = model.predict(x)");

            PythonVariables pyInputs = new PythonVariables();
            pyInputs.addNDArray("x");

            PythonVariables pyOutputs = new PythonVariables();
            pyOutputs.addNDArray("y");

            PythonTransform transform = new PythonTransform("keras-transform", code, pyInputs, pyOutputs);

            PythonExecutioner pyExec = new PythonExecutioner();

            pyInputs.setValue("x", Nd4j.ones(32, 10));
            pyOutputs = pyExec.exec(transform, pyInputs);
            pyInputs.setValue("x", Nd4j.zeros(32, 10));
            pyOutputs = pyExec.exec(transform, pyInputs);
            assertEquals(0, pyOutputs.getNDArrayValue("y").getND4JArray().sum().getDouble());

    }

}
