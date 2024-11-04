import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import Replication.HandShake;
import UserHandler.HandleMultipleUser;

public class Main {
    static Map<String, Object> config = new HashMap<>();

    public static void main(String[] args) {
        HandShake handshake = new HandShake();
        System.out.println("Server started. Logs will appear here!");
        int port = 6379;  // Default port
        config.put("port", port);
        config.put("replicaof", false);

        // Parse command-line arguments
        for (int i = 0; i < args.length; i++) {
            if ("--dir".equals(args[i]) && i + 1 < args.length) {
                config.put("dir", args[i + 1]);
            }
            if ("--dbfilename".equals(args[i]) && i + 1 < args.length) {
                config.put("dbfilename", args[i + 1]);
            }
            if ("--port".equals(args[i]) && i + 1 < args.length) {
                config.put("port", Integer.parseInt(args[i + 1]));
                port = (Integer) config.get("port");
            }
            // for slave branch
            if ("--replicaof".equals(args[i]) && i + 1 < args.length) {
                config.put("replicaof", true);
                String[] masterDetails = args[++i].split("\\s+");
                config.put("Master-HOST", masterDetails[0]);
                config.put("Master-PORT", masterDetails[1]);

                // Trigger replica handshake with master
                handshake.replicaHandshake(
                    (String) config.get("Master-HOST"),
                    Integer.parseInt((String) config.get("Master-PORT")),
                    port
                );
            }
        }

        // Start the server and handle incoming connections
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true); // Allow reuse of the port

            while (true) {
                Socket clientSocket = serverSocket.accept(); // Accept a new client

                // Handle multiple clients in separate threads
                HandleMultipleUser clientHandler = new HandleMultipleUser(
                    clientSocket,
                    (String) config.get("dir"),
                    (String) config.get("dbfilename"),
                    config
                );
                clientHandler.start();
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
