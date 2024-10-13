import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        String hostname = "localhost";  // Assuming the server is running on localhost
        int port = 6379;

        try (Socket socket = new Socket(hostname, port)) {
            // OutputStream to send data to the server
            OutputStream outputStream = socket.getOutputStream();

            // InputStream to read response from the server
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send the SET command with expiry argument (PX) in RESP2 format
//                String setCommand = "*5\r\n$3\r\nSET\r\n$6\r\norange\r\n$6\r\nbanana\r\n$2\r\npx\r\n$3\r\n100\r\n";
//                String setCommand = "*5\r\n$3\r\nSET\r\n$6\r\norange\r\n$6\r\nbanana\r\n";
                String setCommand= "*3\r\n$3\r\nSET\r\n$5\r\ngrape\r\n$6\r\norange\r\n";

            outputStream.write(setCommand.getBytes());
            outputStream.flush();

            // Send the GET command in RESP2 format
            String getCommand = "*2\r\n$3\r\nGET\r\n$5\r\ngrape\r\n";
            outputStream.write(getCommand.getBytes());
            outputStream.flush();

            // Now start reading the server's response
            String response;
            while ((response = in.readLine()) != null) {
                System.out.println("Server Response: " + response);
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
