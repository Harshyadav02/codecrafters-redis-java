import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
public class Main {
    static Map<String, Object> config = new HashMap<>();
    public static void main(String[] args) {
        System.out.println("Server started. Logs will appear here!");
       String host  = "";
        int port = 6379;
        boolean isMaster = true;
        config.put("port",port);
        config.put("replicaof",false);
        // Parse command-line arguments
        for (int i = 0; i < args.length; i++) {
            if ("--dir".equals(args[i]) && i + 1 < args.length) {
                // dir = args[i + 1];
                config.put("dir",args[i+1]);
            }
            if ("--dbfilename".equals(args[i]) && i + 1 < args.length) {
                config.put("dbfilename",args[i+1]);
                // dbfilename = args[i + 1];
            }
            if("--port".equals(args[i])&& i+1 < args.length){
                // port = Integer.parseInt(args[i+1]);
                config.put("port",Integer.parseInt(args[i+1]));
                
            }
            if("--replicaof".equals(args[i]) && i+1 < args.length){
                config.put("replicaof",true);
                isMaster = false;
                host = args[i+1];
            }
        }
        
        
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket((Integer)config.get("port"));
            serverSocket.setReuseAddress(true); // Allow reuse of the port

            // Continuously listen for new client connections
            while (true) {
                Socket clientSocket = serverSocket.accept(); // Accept a new client
                

                // Create a new thread to handle this client
                HandleMultipleUser clientHandler = new HandleMultipleUser(clientSocket,(String)config.get("dir"),(String)config.get("dbfilename"));
                clientHandler.start(); // Start the thread
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    System.out.println("IOException: " + e.getMessage());
                }
            }
        }
    }
}
