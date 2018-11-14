import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TransformServer extends NanoHTTPD{

    private Map<String, PythonTransform> transforms = new HashMap<String, PythonTransform>();
    private PythonExecutioner pythonExecutioner = new PythonExecutioner();
    public TransformServer(int port) throws IOException{
        super(port);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("Server started at " + String.format("%d", port));
    }
    public TransformServer() throws IOException{
        super(8080);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("Server started at 8080");
    }

    public static void main(String args[]) throws IOException{
        TransformServer server = new TransformServer(8000);
    }

    @Override
    public Response serve(IHTTPSession session) {
        String route = session.getUri();
        System.out.println(route);
        Map<String, String> params = session.getParms();
        if (route.equals("/add")){
            System.out.println("adding transform");
            String code = params.get("code");
            if (code == null){
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "No code!");
            }
            System.out.println("Code received:");
            System.out.println(code);
            String name = params.get("name");
            if (name == null){
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "No name!");
            }
            System.out.println(name);
            PythonTransform transform = new PythonTransform(name, code);
            if (transforms.containsKey(name)){
                transforms.put(name, transform);
                return newFixedLengthResponse(Response.Status.OK, "text/plain", "Transform updated: " + name);
            }
            else {
                transforms.put(name, transform);
                return newFixedLengthResponse(Response.Status.OK, "text/plain", "Transform added: " + name);
            }
        }
        else if (route.equals("/exec")){
            System.out.println("executing transform");
            String name = params.get("name");
            if (name == null){
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "No name!");
            }
            PythonTransform transform = transforms.get(name);
            if (transform == null){
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Transform not found: " + name);
            }
            try{
                System.out.println("running code...");
                pythonExecutioner.exec(transform);
            }
            catch (Exception e){
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Execution failed: " + e.toString());
            }

        }
        else{
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Unhandled route: " + route);
        }

    }



}

