import java.io.IOException;

import com.sun.prism.PixelFormat;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Test;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class TestTransformServer {


    private static JSONParser parser = new JSONParser();

    private static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
    private static JSONObject json(Response response) throws ParseException{
        String str = convertStreamToString(response.getData());
        JSONObject jsonObject = (JSONObject)parser.parse(str);
        return jsonObject;
    }

    @Test
    public void testSimpleExec() throws Exception{
        TransformServer server = new TransformServer(false);
        String code = "print('hello world')";
        server.add(null, code, null, null);
        Response response = server.exec(null, null);
        assertEquals(Status.OK, response.getStatus(), convertStreamToString(response.getData()));
    }

    @Test
    public  void testExecWithInputs() throws Exception{
        TransformServer server = new TransformServer(false);
        String code = "print(x + y)";
        String inputSpec = "{\"x\": \"int\", \"y\": \"int\"}";
        server.add(null, code, inputSpec, null);
        String inputs = "{\"x\": 5, \"y\": 10}";
        Response response = server.exec(null, inputs);
        assertEquals(Status.OK, response.getStatus());
    }

    @Test
    public void testExecWithInputsAndOutputs() throws IOException, ParseException{
        TransformServer server = new TransformServer(false);
        String code = "y = x + 10";
        String inputSpec = "{\"x\": \"int\"}";
        String outputSpec = "{\"y\": \"int\"}";
        String inputs = "{\"x\": 5}";
        server.add(null, code, inputSpec, outputSpec);
        Response response = server.exec(null, inputs);
        assertEquals(Status.OK, response.getStatus());
        assertEquals(15, ((Long)json(response).get("y")).intValue());
    }

    @Test
    public void TestList() throws Exception{
        TransformServer server = new TransformServer(false);
        String inputSpec = "{\"x\": \"list\"}";
        String outputSpec = "{\"y\": \"list\"}";
        String code = "x.append(5); y=x";
        String inputs = "{\"x\": [1, 2, 3, 4]}";
        server.add(null, code, inputSpec, outputSpec);
        Response response = server.exec(null, inputs);
        assertEquals(Status.OK, response.getStatus());
        Object output[] = ((JSONArray)(json(response).get("y"))).toArray();
        assertEquals(5L, output.length);
        for (int i=0; i < 5; i++){
            assertEquals(i +1, ((Long)output[i]).intValue());
        }
    }

    @Test
    public void testNDArrays() throws IOException, ParseException{
        TransformServer server = new TransformServer(false);
        String code = "z = x + y * 2.";
        String inputSpec = "{\"x\": \"ndarray\", \"y\": \"ndarray\"}";
        String outputSpec = "{\"z\": \"ndarray\"}";
        server.add(null, code, inputSpec, outputSpec);
        String inputs = "{\"x\": {\"data\": [1.0, 2.0, 3.0, 4.0], \"shape\": [2, 2]}, \"y\": {\"data\": [4.0, 3.0, 2.0, 1.0], \"shape\": [2, 2]}}";
        Response response = server.exec(null, inputs);
        JSONObject z = (JSONObject) json(response).get("z");
        JSONArray zData = (JSONArray)z.get("data");
        JSONArray zShape = (JSONArray)z.get("shape");
        double[] data = new double[zData.size()];
        for (int i=0; i<data.length; i++){
            data[i] = (Double)zData.get(i);
        }
        int[] shape = new int[zShape.size()];
        for(int i=0; i<shape.length; i++){
            shape[i] = ((Long) zShape.get(i)).intValue();
        }
        INDArray arr = Nd4j.create(data, shape);
        assertEquals(30, (int)arr.sum().getDouble(0));
    }

    @Test
    public void testFloat() throws IOException, ParseException{
        TransformServer server = new TransformServer(false);
        String code = "z = x + y * 2.";
        String inputSpec = "{\"x\": \"ndarray\", \"y\": \"ndarray\"}";
        String outputSpec = "{\"z\": \"ndarray\"}";
        server.add(null, code, inputSpec, outputSpec);
        String inputs = "{\"x\": {\"data\": [1.0, 2.0, 3.0, 4.0], \"shape\": [2, 2], \"dtype\": \"FLOAT\"}, \"y\": {\"data\": [4.0, 3.0, 2.0, 1.0], \"shape\": [2, 2], \"dtype\": \"FLOAT\"}}";
        Response response = server.exec(null, inputs);
        JSONObject z = (JSONObject) json(response).get("z");
        JSONArray zData = (JSONArray)z.get("data");
        JSONArray zShape = (JSONArray)z.get("shape");
        String dtype = (String)z.get("dtype");
        assertEquals("FLOAT", dtype);
        double[] data = new double[zData.size()];
        for (int i=0; i<data.length; i++){
            data[i] = (Double)zData.get(i);
        }
        int[] shape = new int[zShape.size()];
        for(int i=0; i<shape.length; i++){
            shape[i] = ((Long) zShape.get(i)).intValue();
        }
        INDArray arr = Nd4j.create(data, shape);
        assertEquals(30, (int)arr.sum().getDouble(0));
    }

    @Test
    public void testDouble() throws IOException, ParseException{
        TransformServer server = new TransformServer(false);
        String code = "z = x + y * 2.";
        String inputSpec = "{\"x\": \"ndarray\", \"y\": \"ndarray\"}";
        String outputSpec = "{\"z\": \"ndarray\"}";
        server.add(null, code, inputSpec, outputSpec);
        String inputs = "{\"x\": {\"data\": [1.0, 2.0, 3.0, 4.0], \"shape\": [2, 2], \"dtype\": \"DOUBLE\"}, \"y\": {\"data\": [4.0, 3.0, 2.0, 1.0], \"shape\": [2, 2], \"dtype\": \"DOUBLE\"}}";
        Response response = server.exec(null, inputs);
        JSONObject z = (JSONObject) json(response).get("z");
        JSONArray zData = (JSONArray)z.get("data");
        JSONArray zShape = (JSONArray)z.get("shape");
        String dtype = (String)z.get("dtype");
        assertEquals("DOUBLE", dtype);
        double[] data = new double[zData.size()];
        for (int i=0; i<data.length; i++){
            data[i] = (Double)zData.get(i);
        }
        int[] shape = new int[zShape.size()];
        for(int i=0; i<shape.length; i++){
            shape[i] = ((Long) zShape.get(i)).intValue();
        }
        INDArray arr = Nd4j.create(data, shape);
        assertEquals(30, (int)arr.sum().getDouble(0));
    }

    @Test
    public void testShort() throws IOException, ParseException{
        TransformServer server = new TransformServer(false);
        String code = "z = x + y * 2";
        String inputSpec = "{\"x\": \"ndarray\", \"y\": \"ndarray\"}";
        String outputSpec = "{\"z\": \"ndarray\"}";
        server.add(null, code, inputSpec, outputSpec);
        String inputs = "{\"x\": {\"data\": [1, 2, 3, 4], \"shape\": [4], \"dtype\": \"SHORT\"}, \"y\": {\"data\": [4, 3, 2, 1], \"shape\": [4], \"dtype\": \"SHORT\"}}";
        Response response = server.exec(null, inputs);
        JSONObject z = (JSONObject) json(response).get("z");
        JSONArray zData = (JSONArray)z.get("data");
        JSONArray zShape = (JSONArray)z.get("shape");
        String dtype = (String)z.get("dtype");
        assertEquals("INT", dtype);
        short[] data = new short[zData.size()];
        for(int i=0; i<data.length; i++){
            data[i] = ((Long)zData.get(i)).shortValue();
        }
        long[] shape = new long[zShape.size()];
        for(int i=0; i<shape.length; i++){
            shape[i] = ((Long) zShape.get(i)).intValue();
        }
        INDArray arr = Nd4j.create(data, shape, DataType.INT);
        assertEquals(30, (int)arr.sum().getDouble(0));
    }

    @Test
    public void testInt() throws IOException, ParseException{
        TransformServer server = new TransformServer(false);
        String code = "z = x + y * 2";
        String inputSpec = "{\"x\": \"ndarray\", \"y\": \"ndarray\"}";
        String outputSpec = "{\"z\": \"ndarray\"}";
        server.add(null, code, inputSpec, outputSpec);
        String inputs = "{\"x\": {\"data\": [1, 2, 3, 4], \"shape\": [4], \"dtype\": \"INT\"}, \"y\": {\"data\": [4, 3, 2, 1], \"shape\": [4], \"dtype\": \"INT\"}}";
        Response response = server.exec(null, inputs);
        JSONObject z = (JSONObject) json(response).get("z");
        JSONArray zData = (JSONArray)z.get("data");
        JSONArray zShape = (JSONArray)z.get("shape");
        String dtype = (String)z.get("dtype");
        assertEquals("INT", dtype);
        int[] data = new int[zData.size()];
        for(int i=0; i<data.length; i++){
            data[i] = ((Long)zData.get(i)).intValue();
        }
        long[] shape = new long[zShape.size()];
        for(int i=0; i<shape.length; i++){
            shape[i] = ((Long) zShape.get(i)).intValue();
        }
        INDArray arr = Nd4j.create(data, shape, DataType.INT);
        assertEquals(30, (int)arr.sum().getDouble(0));
    }


    @Test
    public void testLong() throws IOException, ParseException{
        TransformServer server = new TransformServer(false);
        String code = "z = x + y * 2";
        String inputSpec = "{\"x\": \"ndarray\", \"y\": \"ndarray\"}";
        String outputSpec = "{\"z\": \"ndarray\"}";
        server.add(null, code, inputSpec, outputSpec);
        String inputs = "{\"x\": {\"data\": [1, 2, 3, 4], \"shape\": [2, 2], \"dtype\": \"LONG\"}, \"y\": {\"data\": [4, 3, 2, 1], \"shape\": [2, 2], \"dtype\": \"LONG\"}}";
        Response response = server.exec(null, inputs);
        JSONObject z = (JSONObject) json(response).get("z");
        JSONArray zData = (JSONArray)z.get("data");
        JSONArray zShape = (JSONArray)z.get("shape");
        String dtype = (String)z.get("dtype");
        assertEquals("LONG", dtype);
        long[] data = new long[zData.size()];
        for (int i=0; i<data.length; i++){
            data[i] = (Long)zData.get(i);
        }
        long[] shape = new long[zShape.size()];
        for(int i=0; i<shape.length; i++){
            shape[i] = ((Long) zShape.get(i));
        }
        INDArray arr = Nd4j.create(data, shape, DataType.LONG);
        assertEquals(30, (int)arr.sum().getDouble(0));
    }

}

