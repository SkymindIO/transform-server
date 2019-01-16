import fi.iki.elonen.NanoHTTPD;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPersistence {
    private static JSONParser parser = new JSONParser();

    private static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
    private static JSONObject json(NanoHTTPD.Response response) throws ParseException {
        String str = convertStreamToString(response.getData());
        JSONObject jsonObject = (JSONObject)parser.parse(str);
        return jsonObject;
    }
    @Test
    public void testSimpleExec() throws Exception{
        TransformServer server = new TransformServer(false);
        String code = "print('hello world')";
        server.add(null, code, null, null);
        NanoHTTPD.Response response = server.exec(null, null);
        assertEquals(NanoHTTPD.Response.Status.OK, response.getStatus(), convertStreamToString(response.getData()));

        server = new TransformServer(false);
        response = server.exec(null, null);
        assertEquals(NanoHTTPD.Response.Status.OK, response.getStatus(), convertStreamToString(response.getData()));
    }

    @Test
    public  void testExecWithInputs() throws Exception{
        TransformServer server = new TransformServer(false);
        String code = "print(x + y)";
        String inputSpec = "{\"x\": \"int\", \"y\": \"int\"}";
        server.add(null, code, inputSpec, null);
        String inputs = "{\"x\": 5, \"y\": 10}";
        NanoHTTPD.Response response = server.exec(null, inputs);
        assertEquals(NanoHTTPD.Response.Status.OK, response.getStatus());

        server = new TransformServer(false);
        response = server.exec(null, inputs);
        assertEquals(NanoHTTPD.Response.Status.OK, response.getStatus());

    }

    @Test
    public void testExecWithInputsAndOutputs() throws Exception{
        TransformServer server = new TransformServer(false);
        String code = "y = x + 10";
        String inputSpec = "{\"x\": \"int\"}";
        String outputSpec = "{\"y\": \"int\"}";
        String inputs = "{\"x\": 5}";
        server.add(null, code, inputSpec, outputSpec);
        NanoHTTPD.Response response = server.exec(null, inputs);
        assertEquals(NanoHTTPD.Response.Status.OK, response.getStatus());
        assertEquals(15, ((Long)json(response).get("y")).intValue());

        server = new TransformServer(false);
        response = server.exec(null, inputs);
        assertEquals(NanoHTTPD.Response.Status.OK, response.getStatus());
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
        NanoHTTPD.Response response = server.exec(null, inputs);
        assertEquals(NanoHTTPD.Response.Status.OK, response.getStatus());
        Object output[] = ((JSONArray)(json(response).get("y"))).toArray();
        assertEquals(5L, output.length);
        for (int i=0; i < 5; i++){
            assertEquals(i +1, ((Long)output[i]).intValue());
        }

        server = new TransformServer(false);
        response = server.exec(null, inputs);
        assertEquals(NanoHTTPD.Response.Status.OK, response.getStatus());
        output = ((JSONArray)(json(response).get("y"))).toArray();
        assertEquals(5L, output.length);
        for (int i=0; i < 5; i++){
            assertEquals(i +1, ((Long)output[i]).intValue());
        }
    }
    @Test
    public void testNDArrays() throws Exception{
        TransformServer server = new TransformServer(false);
        String code = "z = x + y * 2.";
        String inputSpec = "{\"x\": \"ndarray\", \"y\": \"ndarray\"}";
        String outputSpec = "{\"z\": \"ndarray\"}";
        server.add(null, code, inputSpec, outputSpec);
        String inputs = "{\"x\": {\"data\": [1.0, 2.0, 3.0, 4.0], \"shape\": [2, 2]}, \"y\": {\"data\": [4.0, 3.0, 2.0, 1.0], \"shape\": [2, 2]}}";
        NanoHTTPD.Response response = server.exec(null, inputs);
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

        server = new TransformServer(false);
        response = server.exec(null, inputs);
        z = (JSONObject) json(response).get("z");
        zData = (JSONArray)z.get("data");
        zShape = (JSONArray)z.get("shape");
        for (int i=0; i<data.length; i++){
            data[i] = (Double)zData.get(i);
        }
        for(int i=0; i<shape.length; i++){
            shape[i] = ((Long) zShape.get(i)).intValue();
        }
        arr = Nd4j.create(data, shape);
        assertEquals(30, (int)arr.sum().getDouble(0));

    }
}
