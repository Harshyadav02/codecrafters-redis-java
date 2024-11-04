package Replication;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class HandShake {

    // Method to handle the replica handshake
    public void replicaHandshake(String host, int port, int replicaPort) {
        try (Socket masterSocket = new Socket(host, port)) {

            // Send the PING command
            sendPingCommand(masterSocket.getOutputStream());

            // Send the REPLCONF listening-port command
            sendReplconfListeningPortCommand(masterSocket.getOutputStream(), replicaPort);

            // Send the REPLCONF capa psync2 command
            sendReplconfCapaCommand(masterSocket.getOutputStream());

            // Send the PSYNC command
            sendPsyncCommand(masterSocket.getOutputStream());

        } catch (IOException | InterruptedException e) {
            System.out.println("Error during handshake: " + e.getMessage());
        }
    }

   

    private void sendPingCommand(OutputStream outputStream) throws IOException, InterruptedException {
        String pingCommand = "*1\r\n$4\r\nPING\r\n";
        outputStream.write(pingCommand.getBytes());
        outputStream.flush();
        Thread.sleep(20);
    }

    private void sendReplconfListeningPortCommand(OutputStream outputStream, int replicaPort)
            throws IOException, InterruptedException {
        String replconfListeningPortCommand = String.format(
                "*3\r\n$8\r\nREPLCONF\r\n$14\r\nlistening-port\r\n$%d\r\n%d\r\n",
                String.valueOf(replicaPort).length(), replicaPort);
        outputStream.write(replconfListeningPortCommand.getBytes());
        outputStream.flush();
        Thread.sleep(20);
    }

    private void sendReplconfCapaCommand(OutputStream outputStream) throws IOException, InterruptedException {
        String replconfCapaCommand = "*3\r\n$8\r\nREPLCONF\r\n$4\r\ncapa\r\n$6\r\npsync2\r\n";
        outputStream.write(replconfCapaCommand.getBytes());
        outputStream.flush();
        Thread.sleep(20);
    }

    private void sendPsyncCommand(OutputStream outputStream) throws IOException, InterruptedException {
        String psyncCommand = "*3\r\n$5\r\nPSYNC\r\n$1\r\n?\r\n$2\r\n-1\r\n";
        outputStream.write(psyncCommand.getBytes());
        outputStream.flush();
        Thread.sleep(20);
    }
}
