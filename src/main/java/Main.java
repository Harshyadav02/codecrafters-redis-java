import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        System.out.println("Server started. Logs will appear here!");

        int port = 6379;
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true); // Allow reuse of the port

            // Continuously listen for new client connections
            while (true) {
                Socket clientSocket = serverSocket.accept(); // Accept a new client
                

                // Create a new thread to handle this client
                HandleMultipleUser clientHandler = new HandleMultipleUser(clientSocket);
//                UserHandler clientHandler = new UserHandler(clientSocket);

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
