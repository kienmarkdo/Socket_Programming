import java.net.*;
import java.io.*;
import java.util.regex.*;

public class PacketSender {

    static String encodePayload() {

    }

    /**
     * Validate the command-line arguments
     * 
     * @param args
     * @return true if command-line arguments are correct; otherwise, false
     */
    static boolean verifyArgs(String[] args) {

        // validate if args[1] is IPv4 address or not
        String regex = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$"; // This regex matches IPv4 addresses
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(args[0]);
        boolean isIPv4 = matcher.matches();

        System.out.println(args[0] + " --- " + args[1]);

        return args.length == 2 && isIPv4 && args[1] != null;
    }

    public static void main(String[] args) throws Exception {

        // verify that command line arguments are inputted correctly
        if (!verifyArgs(args)) {
            throw new Exception(
                    "Command-line arguments are incorrect." +
                            "\nThe command-line arguments must be in the following format: java PacketSender.java <ip_address> <message>"
                            +
                            "\n\nExample of correct input:" +
                            "\n\n\tjava PacketSender.java 127.0.0.1 \"Columbia is the best\"\n");
        }

        String receiverIP = args[0];
        String payload = args[1];

        // try to connect to server / packet receiver
        Socket client = new Socket(receiverIP, 8888);
        // if server is not listening - You will get Exception
        // java.net.ConnectException: Connection refused: connect

        // encode the payload

        // write to server using output stream
        DataOutputStream out = new DataOutputStream(client.getOutputStream());
        out.writeUTF("Hello server - How are you sent by Client");

        // read from the server
        DataInputStream in = new DataInputStream(client.getInputStream());
        System.out.println("Data received from the server is -> " + in.readUTF());

        // close the connection
        client.close();
    }
}