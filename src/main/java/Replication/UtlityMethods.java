package Replication;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
public class UtlityMethods {

    private Socket clientSocket ;
    public UtlityMethods(Socket clientSocket){
        this.clientSocket = clientSocket;
    }
    public void handleMasterReplconfCommand( BufferedReader input) throws IOException {
        clientSocket.getOutputStream().write(
                "+OK\r\n".getBytes()
        );
        clientSocket.getOutputStream().flush();

    }
}
