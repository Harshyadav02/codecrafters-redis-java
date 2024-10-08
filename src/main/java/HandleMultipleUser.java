import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Map;
import java.util.HashMap;


public class HandleMultipleUser extends Thread {
    private final Socket clientSocket;
    private  Map<String,Object> map;
    // Constructor to accept client socket
    public HandleMultipleUser(Socket clientSocket)
    {
        this.clientSocket = clientSocket;
        map = new HashMap<>();
    }

    @Override
    public void run() {
        try {
            // Read input from the client
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Handle multiple commands from the client
            String inputLine;
            while ((inputLine = in.readLine()) != null) {

                System.out.println("InputLine is --> "+inputLine);
                // Respond to PING command
                if (inputLine.trim().equalsIgnoreCase("PING")) {
                    clientSocket.getOutputStream().write("+PONG\r\n".getBytes());
                    clientSocket.getOutputStream().flush(); // Ensure the response is sent
                }
                else if ("ECHO".equalsIgnoreCase(inputLine)) {
                    in.readLine();
                    String message = in.readLine();

                    clientSocket.getOutputStream().write(
                            String.format("$%d\r\n%s\r\n", message.length(), message)
                                    .getBytes());
                    clientSocket.getOutputStream().flush();

                }

                else if("SET".equalsIgnoreCase(inputLine)){
                        in.readLine();
                        String key = in.readLine();
                        in.readLine();
                        String value = in.readLine();
                        GetSet.setKey(map,key,value);
                        clientSocket.getOutputStream().write("OK\r\n".getBytes());
                        clientSocket.getOutputStream().flush();
//                    System.out.println(map);
                }
                else if("GET".equalsIgnoreCase(inputLine)){
                    in.readLine();
                    String key = in.readLine();
                    Object value = GetSet.getValue(map,key);
                    if(value!=null){
                        clientSocket.getOutputStream().write(
                                String.format("$%d\r\n%s\r\n",value.toString().length(),value).getBytes());
                        clientSocket.getOutputStream().flush();
                    }
//                    System.out.println(map);
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
}
