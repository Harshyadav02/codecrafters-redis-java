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
                // // *5\r\n$3\r\nSET\r\n$9\r\nraspberry\r\n$6\r\nbanana\r\n$2\r\npx\r\n$3\r\n100\r\n
                else if("SET".equalsIgnoreCase(inputLine)){
                        in.readLine();                      // skipping size of key
                        String key = in.readLine();         // extracting the actual key
                        in.readLine();                      // skipping size of value
                        String value = in.readLine();       // extracting the actual value
                        String timeVariable = in.readLine();// skipping size of time variable
                        System.out.println("time variable : "+timeVariable);
                        if(timeVariable != null && "$2".equalsIgnoreCase(timeVariable.trim()))
                        {   System.out.println("time variable is not null");
                            in.readLine();                      // skipping  time variable
                            in.readLine();                      // skipping size of time in millisecond
                            Long expTime= Long.parseLong(in.readLine());   // extracting the actual time
                            GetSet.setKeyWithExpiry(map,key,value,expTime);
                        }else{
                            System.out.println("time variable is null");
                            GetSet.setKeyWithExpiry(map,key,value,null);
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
