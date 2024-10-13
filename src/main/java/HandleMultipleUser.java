import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class HandleMultipleUser extends Thread {
    private final Socket clientSocket;
    private Map<String, Object[]> map;
    private final ExpiringCacheManager cacheManager;

    // Constructor to accept client socket
    public HandleMultipleUser(Socket clientSocket) {
        this.clientSocket = clientSocket;
        cacheManager = new ExpiringCacheManager();
        map = new ConcurrentHashMap<>();
    }

    @Override
    public void run() {
        try {
            // Read input from the client
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Handle multiple commands from the client
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Input received: " + inputLine);

                // Respond to PING command
                if (inputLine.trim().equalsIgnoreCase("PING")) {
                    clientSocket.getOutputStream().write("+PONG\r\n".getBytes());
                    clientSocket.getOutputStream().flush(); // Ensure the response is sent
                }
                // Handle ECHO command
                else if ("ECHO".equalsIgnoreCase(inputLine)) {
                    in.readLine(); // Skip size of message
                    String message = in.readLine(); // Read message
                    clientSocket.getOutputStream().write(
                            String.format("$%d\r\n%s\r\n", message.length(), message).getBytes());
                    clientSocket.getOutputStream().flush();
                }
                // Handle SET command
                else if ("SET".equalsIgnoreCase(inputLine)) {
                    handleSetCommand(in);
                }
                // Handle GET command
                else if ("GET".equalsIgnoreCase(inputLine)) {
                    handleGetCommand(in);
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

    private void handleSetCommand(BufferedReader in) throws IOException {
        String keySize = in.readLine();
        if (keySize == null) return; // Handle unexpected input

        String key = in.readLine();
        if (key == null || key.isEmpty()) {
            clientSocket.getOutputStream().write("-ERR invalid key\r\n".getBytes());
            return; // Exit if the key is invalid
        }

        String valueSize = in.readLine();
        String value = in.readLine();
        if (value == null || value.isEmpty()) {
            clientSocket.getOutputStream().write("-ERR invalid value\r\n".getBytes());
            return; // Exit if the value is invalid
        }

        String sizeOfTimeVariable = in.readLine();
        long expTime = -1; // Default to -1 for no expiry
        if (sizeOfTimeVariable != null && sizeOfTimeVariable.startsWith("$")) {
            in.readLine(); // Skip time variable
            in.readLine(); // Skip size of time
            try {
                expTime = Long.parseLong(in.readLine());
            } catch (NumberFormatException e) {
                clientSocket.getOutputStream().write("-ERR invalid expiry time\r\n".getBytes());
                return; // Exit on parsing failure
            }
        }

        // Set the key-value pair with expiry
        cacheManager.setKeyWithExpiry(map, key, value, expTime);
        log("SET command executed: key = " + key + ", value = " + value + ", expiry = " + expTime);

        clientSocket.getOutputStream().write("+OK\r\n".getBytes());
        clientSocket.getOutputStream().flush(); // Ensure response is sent
    }

    private void handleGetCommand(BufferedReader in) throws IOException {
        String keySize = in.readLine(); // Read size of key
        if (keySize == null) return; // Handle unexpected input

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

    private void log(String message) {
        System.out.println("[LOG] " + message);
    }
}
