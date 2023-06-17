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
     * Converts a string array of 2-byte packet fields into a singular packet string
     * 
     * @param fields (e.g. ["4500", "0028", "1c46"])
     * @return string of fields (e.g. 450000281c46)
     */
    static String convertFieldsToString(String[] fields) {

        String packet = "";

        for (String field : fields) {
            packet += field;
        }

        return packet;
    }

    /**
     * Calculate the total hexadecimal length of a string packet
     * 
     * @param packet (e.g.
     *               "450000281c46400040060000C0A80003C0A80001434f4c4f4d4249412032202d204d455353492030")
     * @return the length of the string packet formatted with 4 digits (e.g. 0028)
     */
    static String calculatePacketLength(String packet) {
        return String.format("%04X", packet.length() / 2);
    }

    /**
     * Calculates the header checksum field of a packet
     * 
     * @param packet (e.g.
     *               "450000281c46400040060000C0A80003C0A80001434f4c4f4d4249412032202d204d455353492030")
     * @return the header checksum (e.g. 9d35)
     */
    static String calculateHeaderChecksum(String packet) {

        // STEP 1: Split the long string into an array of substrings, each of length 4
        // For example, the string
        // "450000281c46400040060000C0A80003C0A80001434f4c4f4d4249412032202d204d455353492030"
        // should become an array of strings representing ONLY THE HEADER
        // ["4500","0028","1c46","4000","4006","0000","C0A8","0003","C0A8","0001","434f","4c4f","4d42","4941","2032","202d","204d","4553","5349","2030"]
        // ignore all data that comes after the first 40 digits of the header
        packet = packet.substring(0, 40);

        // create an array to store the substrings
        String[] hexNums = new String[packet.length() / 4];

        // iterate over the input string and extract substrings of length 4
        for (int i = 0; i < packet.length(); i += 4) {
            hexNums[i / 4] = packet.substring(i, i + 4);
        }

        // STEP 2: Calculate the header checksum

        int sumSimple = 0; // exact sum of 2-byte header fields (will include the carryout if needed)

        // calculate sum of 2-byte fields of the packet
        // convert each hexNum string to an equivalent decimal integer; add them all up
        for (String hexNum : hexNums) {
            sumSimple += Integer.parseInt(hexNum, 16);
        }
        String sumHexStr = Integer.toHexString(sumSimple); // simple sum as a hexadecimal string

        // determine whether the checksum has a carryout or not
        int carryout = 0;
        int checksum = 0;
        if (sumHexStr.length() == 5) { // has carryout; calculate new checksum value
            // split the carry out and the 2-byte checksum portion; add them together
            carryout = Integer.parseInt(sumHexStr.substring(0, 1), 16);
            checksum = Integer.parseInt(sumHexStr.substring(1), 16);
            checksum += carryout;
        } else { // no carryout
            checksum = sumSimple;
        }

        // calculate 1's complement
        checksum = 65535 - checksum; // 65535 is the decimal equivalent of FFFF in hexadecimal

        sumHexStr = Integer.toHexString(checksum);

        return sumHexStr;
    }

    /**
     * Converts the data from string text to hexadecimal
     * 
     * @param text
     * @return the payload in hexadecimal
     * @throws UnsupportedEncodingException
     */
    static String encodePayload(String text) throws UnsupportedEncodingException {
        return String.format("%020x", new BigInteger(1, text.getBytes()));
    }

    /**
     * Takes a packet packet string as an argument and add zeros to the end of the
     * packet such that the packet length is divisible by 8.
     * 
     * These zeros are called "padding".
     * 
     * @param packet string
     * @return paddedPacket whose length is divisible by 8
     */
    static String addPadding(String packet) {
        StringBuilder padding = new StringBuilder();
        StringBuilder packetStrBuilder = new StringBuilder(packet);

        while (packetStrBuilder.length() % 8 != 0) {
            packetStrBuilder.append("0");
            padding.append("0");
        }

        return padding.toString();
    }

    /**
     * Encapculates the data into an IP datagram (IP packet) and initialize the
     * modular sum (checksum) to 0000
     * 
     * @param byteData
     * @return packet (encapsulated data)
     */
    static String encapsulatePayload(String byteData) {
        // [FIX] All fields marked with FIX (fixed) is to be hard-coded
        // [VAR] All fields marked with VAR (variable) is to be variable data
        // NOTE: One field consists of 2 bytes; one byte consists of 2 hex numbers
        // e.g. the field 4500 consists of byte 45 and byte 00

        String[] fields = new String[9];
        fields[0] = "4500"; // [FIX] 4 == IPv4 and 5 == header length; 00 == service type
        fields[1] = "0000"; // [VAR] packet length (sum of header length + payload length) (init. to 0000)
        fields[2] = "1c46"; // [FIX] identification
        fields[3] = "4000"; // [FIX] 40 = flag; 00 == fragment offset
        fields[4] = "4006"; // [FIX] 40 == TTL; 06 == TCP protocol
        fields[5] = "0000"; // [VAR] source header checksum (init. to 0000)
        fields[6] = convertIPv4StringToHexadecimal("192.168.0.3") // [VAR] IP address source (4 bytes);
                + convertIPv4StringToHexadecimal("192.168.0.1"); // IP address destination (4 bytes)
        fields[7] = byteData; // [VAR] Payload (Has variable byte length too!!!)
        fields[8] = ""; // padding

        String packet = "";
        packet = convertFieldsToString(fields);
        fields[8] = addPadding(packet); // add padding such that the packet length is divisible by 8
        fields[1] = calculatePacketLength(packet); // calculate packet length field
        packet = convertFieldsToString(fields); // update packet with updated fields
        fields[5] = calculateHeaderChecksum(packet); // calculate header checksum field
        packet = convertFieldsToString(fields); // update packet with updated fields

        return packet;
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

        // ****************** VERIFY USER INPUT ****************** //

        // verify that command line arguments are inputted correctly
        if (!verifyArgs(args)) {
            throw new Exception(
                    "Command-line arguments are incorrect." +
                            "\nThe command-line arguments must be in the following format: java PacketSender.java <ip_address> <message>"
                            +
                            "\n\nExample of correct input:" +
                            "\n\n\tjava PacketSender.java 127.0.0.1 \"Columbia is the best\"\n");
        }

        // ******** ENCAPSULATE THE PAYLOAD AND CREATE THE PACKET ******** //

        String receiverIP = args[0];
        String payload = args[1];
        String encodedPayload = "";
        String packet = "";

        // encode the payload and create an IPv4 packet to be sent to the server
        System.out.println("Receiver IP: " + receiverIP + "\nPayload: " + payload);
        System.out.println("\n**** Creating packet by encapsulating the payload into an IPv4 packet... ****\n");

        System.out.println("Encoded the payload from plain-text to hexadecimal:");
        encodedPayload = encodePayload(payload);
        System.out.println("\t" + encodedPayload);

        packet = encapsulatePayload(encodedPayload); // begin payload encapsulation process
        System.out.println("Packet to be sent to PacketReceiver.java:");
        System.out.println("\t" + packet);

        // ******** SEND THE PACKET TO PACKET RECEIVER ******** //

        // try to connect to server / packet receiver
        Socket client = new Socket("127.0.0.1", 8888);
        // if server is not listening - You will get Exception
        // java.net.ConnectException: Connection refused: connect

        // write to server using output stream
        System.out.println("Sending data...");
        DataOutputStream out = new DataOutputStream(client.getOutputStream());
        out.writeUTF(packet);

        // read from the server
        DataInputStream in = new DataInputStream(client.getInputStream());
        System.out.println("Response received from the server ====> " + in.readUTF());

        // close the connection
        client.close();
    }
}