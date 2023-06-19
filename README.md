# Network Socket Programming

This program simulates two machines communicating with each other (client-server communication) by sending IPv4 packets via a socket. On the client, the user starts by entering the IP address of the receiver and the message they wish to send to the receiver (the payload). 

The sender (client) then takes that information and performs the following operations:
- Encodes the payload from plain-text into hexadecimal
- Creates an IPv4 packet containing all of the necessary headers (version, header length, service type, total length, identification, flags, fragment offset, TTL, protocol, header checksum, source IP address, destination IP address, data, padding)
- Establishes a TCP/IP socket connection with the receiver
- Sends the packet to the receiver

The receiver (server), while waiting for a packet from the sender:
- Receives the packet
- Verifies that the packet has not been corrupted by calculating the header checksum
- Decodes the payload and;
    - Prints the message to the terminal and signals to the sender that the message has been received successfully
    - Prints an error message to the terminal and signals to the sender that the message received has been corrupted

#### Demo

![image](https://github.com/kienmarkdo/Socket_Programming/assets/67518620/b4c53252-f208-47a2-8c92-7d22158a01a1)

_NOTE: All demo images contain dummy IP addresses._

## Instructions
- Open two terminals in root of project (VSCode is preferred)
- In terminal 1, compile and start the server
    - `javac PacketReceiver.java ; java PacketReceiver`
- In terminal 2, compile and run the program with two command-line arguments in the following format:
    - `javac PacketSender.java ; java PacketSender <IP_ADDRESS_OF_RECEIVER> "<MESSAGE>"`
    - Example: `javac PacketSender.java ; java PacketSender 192.168.0.1 "Hello! How are you?"`

### Testing corrupt packet transmission
How to test the program by simulating a corrupt packet scenario? Since it is highly unlikely that a packet will be corrupt during transmission in this simple program, you need to send a corrupt packet manually.

Consider the following correct scenario:
 - Input: `java PacketSender 192.168.0.1 "Welcome to Kien's GitHub!"`
 - Packet: `450000301c46400040069d2dC0A80003C0A8000157656c636f6d6520746f204b69656e27732047697448756221000000`
```java
// ==> In PacketSender.java
// write to server using output stream
System.out.println("Sending data...");
DataOutputStream out = new DataOutputStream(client.getOutputStream());
out.writeUTF(packet);
```
To simulate sending corrupt data, rather than sending the variable `packet` in `out.writeUTF(packet);`, you can manually send a corrupt string instead, 
```java
// ==> In PacketSender.java
// write to server using output stream
System.out.println("Sending data...");
DataOutputStream out = new DataOutputStream(client.getOutputStream());
out.writeUTF("050000301c46400040069d2dC0A80003C0A8000157656c636f6d6520746f204b69656e27732047697448756221000000"); // notice that the first digit is changed from "4" to "0"
```
#### Demo

![image](https://github.com/kienmarkdo/Socket_Programming/assets/67518620/96dd3cd9-0c3e-4fd2-96c2-eab96ba7057a)

_NOTE: All demo images contain dummy IP addresses._



