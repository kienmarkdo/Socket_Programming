import java.net.*;
import java.io.*;
import java.math.BigInteger;
import java.util.regex.*;

public class PacketSender {

    /**
     * Converts a regular string IPv4 address to a hexadecimal string IPv4 address
     * 
     * @param ipAddress
     * @return string of hexadecimal IPv4 address
     */
    static String convertIPv4StringToHexadecimal(String ipAddress) {
        String hexadecimalIpv4Address = "";
        String[] ipOctects = ipAddress.split("\\.");

        // traverse through each of the 4 octets and convert them to hexadecimal
        for (int i = 0; i < 4; i++) {
            int currOctet = Integer.parseInt(ipOctects[i]); // current octect (byte) in int

            // convert decimal to hexadecimal; add leading zeros to the octet if needed
            hexadecimalIpv4Address += String.format("%02X", currOctet);
            // https://stackoverflow.com/questions/8689526/integer-to-two-digits-hex-in-java
        }

        return hexadecimalIpv4Address;
    }

    /**
     * Converts the data from string text to hexadecimal
     * 
     * @param text
     * @return the payload in hexadecimal
     * @throws UnsupportedEncodingException
     */
    static String encodePayload(String text) throws UnsupportedEncodingException {
        return String.format("%040x", new BigInteger(1, text.getBytes()));
    }

    /**
     * Encapculates the data into an IP datagram (IP packet) and initialize the
     * modular sum (checksum) to 0000
     * 
     * @param byteData
     * @return encapsulated data
     */
    static String encapsulatePayload(String byteData) {
        // [FIX] All fields marked with FIX (fixed) is to be hard-coded
        // [VAR] All fields marked with VAR (variable) is to be variable data
        // NOTE: "Octet" means "byte"

        String[] octets = new String[8];
        octets[0] = "4500"; // [FIX] 4 == IPv4 + 5 == header length + 00 == service type
        octets[1] = "0000"; // [VAR] header length (header length + payload length) (initialized to 0000)
        octets[2] = "1c46"; // [FIX] identification
        octets[3] = "4000"; // [FIX] flags + fragment offset
        octets[4] = "4006"; // [FIX] 40 == TTL + 06 == TCP protocol
        octets[5] = "0000"; // [VAR] source header checksum (initialized to 0000)
        octets[6] = convertIPv4StringToHexadecimal("192.168.0.3")
                + convertIPv4StringToHexadecimal("192.168.0.1"); // [VAR] IP address source + IP address destination
        octets[7] = byteData;

        String encapsulatedPayload = "";

        for (String octet : octets) {
            encapsulatedPayload += octet;
        }

        // calculate header length then place the value in the header length field
        // TODO: This logic may be wrong. Review how to calculate header length
        octets[1] = String.format("%04X", encapsulatedPayload.length() / 2);

        encapsulatedPayload = "";

        for (String octet : octets) {
            encapsulatedPayload += octet;
        }

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

        return args.length == 2 && isIPv4 && args[1] != null;
    }

    /**
     * Sends a user-inputted message to a specified IP address
     * Takes two arguments as input: <IPv4_ADDRESS> and <MESSAGE_STRING>
     * 
     * Example:
     * java PacketSender.java 127.0.0.1 "Colombia 1 - Messi 0"
     * 
     * NOTE: Due to the complicated nature of getting the local host's IP address
     * and the various network interface cards, we will assume that both the
     * PacketSender's and PacketReceiver's IP addresses will be 127.0.0.1
     * https://stackoverflow.com/questions/9481865/getting-the-ip-address-of-the-current-machine-using-java
     * 
     * @param args
     * @throws Exception
     */
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
        // System.out.println(encodePayload(payload));
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