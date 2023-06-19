import java.net.*;
import java.io.*;

public class PacketReceiver extends Thread {

    /**
     * Converts a hexadecimal string IPv4 address to a regular string IPv4 address
     * 
     * @param hexIpAddress (e.g. 0A064156)
     * @return IPv4 address string (e.g.: 10.6.65.86)
     */
    static String convertHexadecimalToIPv4String(String hexIpAddress) {

        String ip = "";

        // splits the hex IP address into groups of 2 and then convert them to integers
        // 0A = 10 -- 06 = 06 -- 41 = 65 -- 56 = 86
        for (int i = 0; i < hexIpAddress.length(); i = i + 2) {
            ip = ip + Integer.valueOf(hexIpAddress.substring(i, i + 2), 16);

            // do not add a period if this is the last octet in the IPv4 address
            if (i != hexIpAddress.length() - 2) {
                ip += ".";
            }
        }

        return ip;
    }

    /**
     * Converts a singular packet string into a string array of 2-byte packet fields
     * 
     * @param packet (e.g. 450000281c46)
     * @return string array of fields (e.g. ["4500", "0028", "1c46"])
     */
    static String[] convertStringToFields(String packet) {

        // create an array to store the substrings
        String[] fields = new String[packet.length() / 4];

        // iterate over the input string and extract substrings of length 4
        for (int i = 0; i < packet.length(); i += 4) {
            fields[i / 4] = packet.substring(i, i + 4);
        }

        return fields;

    }

    /**
     * s
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
        String[] hexNums = convertStringToFields(packet); // iterate and extract substrings of length 4 from packet

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

    static String convertHexadecimalToString(String hexString) {
        StringBuilder output = new StringBuilder("");

        for (int i = 0; i < hexString.length(); i += 2) {
            String str = hexString.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }

        return output.toString();
    }

    /**
     * Verifies if the received encoded packet has errors or not
     * 
     * @param encodedPacket
     * @return Returns true if the received encoded packet has no errors; false
     *         otherwise
     */
    static boolean verifyEncodedPacket(String encodedPacket) {
        String modularSumHex = calculateHeaderChecksum(encodedPacket);
        int modularSumDecimal = Integer.parseInt(modularSumHex, 16);

        if (modularSumDecimal == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Extracts the decoded payload from an encoded IPv4 packet
     * 
     * @param encodedPacket "450000281c46400040069d35C0A80003C0A80001434f4c4f4d4249412032202d204d455353492030"
     * @return payload in plain-text e.g. "COLOMBIA 2 - MESSI 0"
     */
    static String decodePayload(String encodedPacket) {
        String message = "";
        if (!verifyEncodedPacket(encodedPacket)) {
            message = "ERROR: This packet has been corrupted.";
        } else {
            String encodedPayload = encodedPacket.substring(40); // extract the payload portion from the encoded packet
            message = convertHexadecimalToString(encodedPayload);
        }

        return message;
    }

    /**
     * Extracts the decoded source IP from an encoded IPv4 packet
     * 
     * @param encodedPacket "450000281c46400040069d35C0A80003C0A80001434f4c4f4d4249412032202d204d455353492030"
     * @return source IP in plain-text e.g. 192.168.0.3
     */
    static String decodeSourceIP(String encodedPacket) {
        String message = "";
        if (!verifyEncodedPacket(encodedPacket)) {
            message = "ERROR: This packet has been corrupted.";
        } else {
            String encodedSourceIP = encodedPacket.substring(24, 32); // extract the source IP from the encoded packet
            message = convertHexadecimalToIPv4String(encodedSourceIP);
        }

        return message;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Packet Receiver Listening on port 8888\n");
        ServerSocket serverSocket = new ServerSocket(8888);
        // server timeout 60 minutes
        serverSocket.setSoTimeout(1000 * 60 * 60);

        // Below method waits until client socket tries to connect
        Socket server = serverSocket.accept();

        // Read from client using input stream
        DataInputStream in = new DataInputStream(server.getInputStream());
        String receivedPacket = in.readUTF();
        String sourceIP = decodeSourceIP(receivedPacket); // gets the decoded source IP from the raw encoded packet

        System.out.println("Successfully received packet from: " + sourceIP);
        System.out.println("Raw packet data: " + receivedPacket + "\n");

        // Decode packet (de-encapsulate) and print the payload/message to terminal
        String message = decodePayload(receivedPacket);
        System.out.println("Message: " + message + "\n");

        // display other logistical information regarding the packet
        int headerLength = 40;
        int payloadLength = receivedPacket.length() - headerLength;

        int numOfBits = payloadLength * 4;
        int numOfBytes = payloadLength / 2;
        int packetLength = receivedPacket.length() / 2;

        System.out.println("The data has " + numOfBits + " bits or " + numOfBytes + " bytes.");
        System.out.println("The total length of the packet is " + packetLength + " bytes.");

        // Write to client using output stream
        DataOutputStream out = new DataOutputStream(server.getOutputStream());
        if (verifyEncodedPacket(receivedPacket)) {
            System.out.println("Checksum verification confirms that the received packet is authentic.");
            out.writeUTF("I received your message succesfully! Goodbye!");
        } else {
            System.out.println("Checksum verification shows that the received packet is corrupted. Packet discarded!");
            out.writeUTF(
                    "Oh no, the packet I received was corrupted. Send me your message again when we talk next time!");
        }

        // close the connection
        server.close();
        serverSocket.close();
    }
}
