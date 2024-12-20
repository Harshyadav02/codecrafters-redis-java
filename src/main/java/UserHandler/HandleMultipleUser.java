package UserHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import RDB_Persistence.RedisConfigCommand;
import Replication.UtlityMethods;
public class HandleMultipleUser extends Thread {
    private final UtlityMethods replicationUtlityMethods;
    private final Socket clientSocket;
    private final Map<String, Object[]> map;
    private final Map<String, Object> config;
    private final ExpiringCacheManager cacheManager;
    RedisConfigCommand configuration;
    int arrayLength = 0;

    // Constructor to accept client socket
    public HandleMultipleUser(Socket clientSocket, String dir, String dbfilename,Map<String, Object> config) {
        this.replicationUtlityMethods = new UtlityMethods(clientSocket);
        this.clientSocket = clientSocket;
        cacheManager = new ExpiringCacheManager();
        map = new ConcurrentHashMap<>();
        configuration = new RedisConfigCommand(dir, dbfilename);
        this.config = config;
    }

    @Override
    public void run() {
        try {
            // Read input from the client
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Handle multiple commands from the client
            String inputLine;
            while ((inputLine = in.readLine()) != null) {

                if (inputLine.startsWith("*")) {
                    // arrayLength define if the expiry time is given or not
                    arrayLength = Integer.parseInt(inputLine.substring(1));
                }
                // Respond to PING command
                if ("PING".equalsIgnoreCase(inputLine)) {
                    handlePingCommand();
                }
                // Handle ECHO command
                else if ("ECHO".equalsIgnoreCase(inputLine)) {
                    handleEchoCommand(in);
                }
                // Handle SET command
                else if ("SET".equalsIgnoreCase(inputLine)) {
                    handleSetCommand(in);
                }
                // Handle GET command
                else if ("GET".equalsIgnoreCase(inputLine)) {
                    handleGetCommand(in);
                }
                // Handle config command
                else if ("CONFIG".equalsIgnoreCase(inputLine)) {
                    handleConfigCommand(in);
                }
                // Handle info command
                else if ("INFO".equalsIgnoreCase(inputLine)) {
                    handleInfoCommand();
                }
                else if("REPLCONF".equalsIgnoreCase(inputLine)){
                    replicationUtlityMethods.handleMasterReplconfCommand(in);
                }
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } finally {
            try {
                // Close the client socket once done
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
        }
    }

    /* utlilty method for the command */

    public void handlePingCommand() {
        try {
            clientSocket.getOutputStream().write("+PONG\r\n".getBytes());
            clientSocket.getOutputStream().flush(); // Ensure the response is sent
        } catch (Exception e) {
            e.getMessage();
        }
    }

    private void handleEchoCommand(BufferedReader in) {
        try {
            in.readLine(); // Skip size of message
            String message = in.readLine(); // Read message
            clientSocket.getOutputStream().write(
                    String.format("$%d\r\n%s\r\n", message.length(), message).getBytes());
            clientSocket.getOutputStream().flush();
        } catch (Exception e) {
            e.getMessage();
        }
    }

    private void handleSetCommand(BufferedReader in) throws IOException {
        String keySize = in.readLine();
        if (keySize == null)
            return; // Handle unexpected input

        String key = in.readLine();
        if (key == null || key.isEmpty()) {
            clientSocket.getOutputStream().write("-ERR invalid key\r\n".getBytes());
            return; // Exit if the key is invalid
        }

        in.readLine(); // ignore value size
        String value = in.readLine(); // read value

        if (value == null || value.isEmpty()) {
            clientSocket.getOutputStream().write("-ERR invalid value\r\n".getBytes());
            return; // Exit if the value is invalid
        }
        long expiryTime = -1;
        if (arrayLength > 3) {
            in.readLine(); // ignore expiry time variable length
            in.readLine(); // ignore expiry time variable time
            in.readLine(); // ignore expiry time size
            expiryTime = Long.parseLong(in.readLine()); // read expiry time
        }
        try {
            cacheManager.setKeyWithExpiry(map, key, value, expiryTime);
        } catch (Exception e) {
            log(e.getMessage());
        }
        clientSocket.getOutputStream().write("+OK\r\n".getBytes());
        clientSocket.getOutputStream().flush(); // Ensure response is sent
    }

    private void handleGetCommand(BufferedReader in) throws IOException {
        String keySize = in.readLine(); // Read size of key
        if (keySize == null)
            return; // Handle unexpected input

        String key = in.readLine(); // Read key
        if (key == null || key.isEmpty()) {
            clientSocket.getOutputStream().write("-ERR invalid key\r\n".getBytes());
            return; // Exit if the key is invalid
        }

        Object value = cacheManager.getValue(map, key);
        if (value != null) {
            clientSocket.getOutputStream().write(
                    String.format("$%d\r\n%s\r\n", value.toString().length(), value).getBytes());
            log("GET command executed: key = " + key + ", value = " + value);
        } else {
            clientSocket.getOutputStream().write("$-1\r\n".getBytes()); // Correctly formatted null response
            log("GET command executed: key = " + key + ", value = null (not found)");
        }
        clientSocket.getOutputStream().flush(); // Ensure response is sent
    }

    private void handleConfigCommand(BufferedReader in) {

        try {
            in.readLine(); // ignore get size ;
            in.readLine();
            in.readLine();
            String configCommand = "CONFIG " + in.readLine().toLowerCase().trim(); // combining config get

            if (configCommand.contains("dir")) {
                String dir = configuration.getDir();
                clientSocket.getOutputStream().write(dir.getBytes());
            } else {
                String dbFileName = configuration.getDbFileName();
                clientSocket.getOutputStream().write(dbFileName.getBytes());
            }
        } catch (Exception e) {
            e.getMessage();
        }
    }

    private void handleInfoCommand() {

        boolean replication = (boolean)config.get("replicaof");
        String role = replication ? "slave" : "master"; // Set role dynamically based on replication flag

        String master_replid = (UUID.randomUUID().toString().replaceAll("-", "")
                + UUID.randomUUID().toString().replaceAll("-", ""))
                .substring(0, 40);
        int master_repl_offset = 0;

        // Build the response string
        String response = String.format("role:%s\r\nmaster_replid:%s\r\nmaster_repl_offset:%d\r\n",
                role, master_replid, master_repl_offset);

        // length of the response string
        int length = response.length();

        try {

            clientSocket.getOutputStream().write(
                    String.format("$%d\r\n%s\r\n", length, response).getBytes());
            clientSocket.getOutputStream().flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void log(String message) {
        System.out.println("[LOG] " + message);
    }
}
