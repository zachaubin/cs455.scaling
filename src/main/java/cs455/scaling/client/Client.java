package cs455.scaling.client;

import cs455.scaling.bytes.RandomPacket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/*
 (1)Connect and maintain an active connection to the server.
 (2)Regularly send data packets to the server. The payloads for these data packets are 8 KB and the  values  for
  these  bytes  are  randomly  generated. The  rate  at  which  each  connection  will generate packets is R per-second;
  include a Thread.sleep(1000/R) in the client which ensures that you achieve the targeted production rate. The typical
  value of R is between 2-4.
 (3)The client should track hash codes of the data packets that it has sent to the server. A server will acknowledge
 every packet that it has received by sending the computed hash code back to the client.
 */

public class Client {
    private static SocketChannel client;
    private static ByteBuffer buffer;
    private byte[] msg;
    private ArrayList<String> hashedMessages;//to verify with server's response



//    //registering server socket channels
//
//    SocketChannel socketChannel = serverSocket.accept();
//    socketChannel.configureBlocking(false);
//    socketChannel.register( selector, SelectionKey.OP_READ);

    public static void register() throws IOException {

        String hostname = "localhost";
        int port = 6548;

        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect( new InetSocketAddress( hostname, port ));
    }




    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        String hostname = "localhost";
        int port = 6548;
        ArrayList<String> hashes = new ArrayList<>();
        try {
            //connect to cs455.scaling.server
            client = SocketChannel.open(new InetSocketAddress(hostname,port));
            //create buffer
            buffer = ByteBuffer.allocate(8000);
        } catch (IOException e){
            System.err.println("::Client: error connecting to cs455.scaling.server, stacktrace:...");
            e.printStackTrace();
        }

//        buffer = ByteBuffer.wrap("Please send this back to me.".getBytes());

        RandomPacket randomPacket = new RandomPacket();
        byte[] msg = randomPacket.generate();
        String hash = randomPacket.hash(msg);
        hashes.add(hash);
        System.out.println("hash out: " + hash);
        buffer = ByteBuffer.wrap(msg);



        String response = null;
        try {
            client.write(buffer);
            buffer.clear();
            buffer = ByteBuffer.allocate(256);
            client.read(buffer);
            response = new String(buffer.array()).trim();
            System.out.println("Server responded with: " + response);
            buffer.clear();
        } catch (IOException e){
            System.err.println("error receiving from cs455.scaling.server, stacktrace:...");
            e.printStackTrace();
        }
    }
}