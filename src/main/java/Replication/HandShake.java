package Replication;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class HandShake {

    public void threeWayHandshake(String host, int port) {
        try (Socket masterSocket = new Socket(host, port)) {
            
            // Sending the PING command as a RESP Array: *1\r\n$4\r\nPING\r\n
            String pingCommand = "*1\r\n$4\r\nPING\r\n";
            OutputStream out = masterSocket.getOutputStream();
            out.write(pingCommand.getBytes());
            out.flush();

            

        } catch (IOException e) {
            System.out.println("Error during handshake: " + e.getMessage());
        }
    }
}
