import java.net.*;
import java.io.*;
import java.math.BigInteger;
import java.util.regex.*;

public class PacketSender {

    /**
     * Converts the data from string text to hexadecimal
     * 
     * @return the payload in hexadecimal
     */
    static String encodePayload(String text) throws UnsupportedEncodingException {
        return String.format("%040x", new BigInteger(1, text.getBytes()));
    }

    /**
     * Encapculates the data into an IP datagram (IP packet) and initialize the
     * modular sum (checksum) to 0000
     * 
     * @return encapsulated data
     */
    static String encapsulatePayload(String byteData) {
        // [FIX] All fields marked with FIX (fixed) is to be hard-coded
        // [VAR] All fields marked with VAR (variable) is to be variable data
        // NOTE: "Octet" means "byte"

        String[] octets = new String[7];
        octets[0] = "4500"; // [FIX] 4 == IPv4 + 5 == header length + 00 == service type
        octets[1] = ""; // [VAR] total header length (header length + payload length)
        octets[2] = "1c46"; // [FIX] identification
        octets[3] = "4000"; // [FIX] flags + fragment offset
        octets[4] = "4006"; // [FIX] 40 == TTL + 06 == TCP protocol
        octets[5] = "0000"; // [VAR] source header checksum (initialized to 0000)
        octets[6] = ""; // [VAR] IP address source + IP address destination

        String encapsulatedPayload = "";
        for (String octet : octets) {
            encapsulatedPayload += octet;
        }
        encapsulatedPayload += byteData;

        return encapsulatedPayload;
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
        // Socket client = new Socket(receiverIP, 8888);
        // if server is not listening - You will get Exception
        // java.net.ConnectException: Connection refused: connect

        // encode the payload
        System.out.println(receiverIP + " " + payload);
        System.out.println(encodePayload(payload));
        System.out.println(encapsulatePayload(encodePayload(payload)));

        // // write to server using output stream
        // DataOutputStream out = new DataOutputStream(client.getOutputStream());
        // out.writeUTF("Hello server - How are you sent by Client");

        // // read from the server
        // DataInputStream in = new DataInputStream(client.getInputStream());
        // System.out.println("Data received from the server is -> " + in.readUTF());

        // // close the connection
        // client.close();
    }
}