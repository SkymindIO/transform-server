import java.io.IOException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Test;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class TestTransformServer {


    private static JSONParser parser = new JSONParser();

    private static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
    private static JSONObject json(Response response) throws IOException, ParseException{
        String str = convertStreamToString(response.getData());
        JSONObject jsonObject = (JSONObject)parser.parse(str);
        return jsonObject;
    }

    @Test
    public void TestSimpleExec() throws IOException{
        TransformServer server = new TransformServer(false);
        String code = "print('hello world')";
        server.add(null, code, null, null);
        Response response = server.exec(null, null);
        assertEquals(Status.OK, response.getStatus());
    }

    @Test
    public  void TestExecWithInputs() throws IOException{
        TransformServer server = new TransformServer(false);
        String code = "print(x + y)";
        String inputSpec = "{\"x\": \"int\", \"y\": \"int\"}";
        server.add(null, code, inputSpec, null);
        String inputs = "{\"x\": 5, \"y\": 10}";
        Response response = server.exec(null, inputs);
        assertEquals(Status.OK, response.getStatus());
    }

    @Test
    public void TestWithInputsAndOutputs() throws IOException, ParseException{
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
}

