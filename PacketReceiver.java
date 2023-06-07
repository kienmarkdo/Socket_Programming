import java.net.*;
import java.io.*;

public class PacketReceiver extends Thread {

    public static void main(String[] args) throws Exception {
        System.out.println("Packet Receiver Listening on port 8888");
        ServerSocket serverSocket = new ServerSocket(8888);
        // server timeout 60 minutes
        serverSocket.setSoTimeout(1000 * 60 * 60);

        // Below method waits until client socket tries to connect
        Socket server = serverSocket.accept();

        // Read from client using input stream
        DataInputStream in = new DataInputStream(server.getInputStream());
        System.out.println(in.readUTF());

        // Write to client using output stream
        DataOutputStream out = new DataOutputStream(server.getOutputStream());
        out.writeUTF("Welcome and Bye Client!");

        // close the connection
        server.close();
        serverSocket.close();
    }
}
