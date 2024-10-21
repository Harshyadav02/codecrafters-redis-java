package Replication;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class HandShake {

    public void threeWayHandshake(String host, int port, int replicaPort) {
        try (Socket masterSocket = new Socket(host, port)) {
            
            // Sending the PING command as a RESP Array: *1\r\n$4\r\nPING\r\n
            String pingCommand = "*1\r\n$4\r\nPING\r\n";
            
            String replconfListeningPortCommand = String.format("*3\r\n$8\r\nREPLCONF\r\n$14\r\nlistening-port\r\n$%d\r\n%d\r\n", String.valueOf(replicaPort).length(), replicaPort);
            
            // Second REPLCONF: REPLCONF capa psync2
            String replconfCapaCommand = "*3\r\n$8\r\nREPLCONF\r\n$4\r\ncapa\r\n$6\r\npsync2\r\n";

            OutputStream out = masterSocket.getOutputStream();
            
            // Send the PING command
            out.write(pingCommand.getBytes());
            Thread.sleep(20);   

            // Send the first REPLCONF command (listening-port)
            out.write(replconfListeningPortCommand.getBytes());
            Thread.sleep(20);  // 

            // Send the second REPLCONF command (capa psync2)
            out.write(replconfCapaCommand.getBytes());

            // Ensure all commands are sent
            out.flush();
            Thread.sleep(20);
        } catch (InterruptedException e) {
            
            e.printStackTrace();
        }
        catch (IOException e) {
            System.out.println("Error during handshake: " + e.getMessage());
        } 
    }
}
