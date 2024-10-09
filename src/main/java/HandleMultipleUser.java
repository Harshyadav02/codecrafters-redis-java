import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Map;
import java.util.HashMap;


public class HandleMultipleUser extends Thread {
    private final Socket clientSocket;
    private  Map<String,Object[]> map;
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

                System.out.println("Received: " + inputLine);
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
                        in.readLine();                      // skipping size of key --> $9
                        String key = in.readLine();         // extracting the actual key --> raspberry
                        in.readLine();                      // skipping size of value  --> $6
                        String value = in.readLine();       // extracting the actual value --> banana
                        System.out.println("GOing to read time varaible size");
                        String sizeOfTimeVariable = in.readLine();// skipping size of time variable  --> $2
                        System.out.println("Time variable size is "+sizeOfTimeVariable);
                    if(sizeOfTimeVariable !=null && sizeOfTimeVariable.startsWith("$"))

                    {
                        in.readLine();                      // skipping  time variable --> px
                        in.readLine();                      // skipping size of time in millisecond --> $3

                        try{
                            long expTime= Long.parseLong(in.readLine());   // extracting the actual time
                            GetSet.setKeyWithExpiry(map,key,value,expTime);
                        }catch (Exception e){
                            System.out.println(e.getMessage());
                        }
                    }
                    else{
//                        System.out.println("Yes no time expiry provided");
                        GetSet.setKeyWithExpiry(map,key,value,-1);
                    }
                        clientSocket.getOutputStream().write("+OK\r\n".getBytes());
                        clientSocket.getOutputStream().flush();
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
                    // if entry not found in map send null to client
                    else{
                        clientSocket.getOutputStream().write(
                                String.format("$%d\r\n",-1).getBytes()
                        );
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
