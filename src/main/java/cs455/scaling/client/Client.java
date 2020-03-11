package cs455.scaling.client;

import cs455.scaling.bytes.RandomPacket;
import cs455.scaling.stats.ClientTracker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;

import static java.lang.Thread.sleep;

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
    private volatile LinkedList<String> hashes;
    RandomPacket randomPacket;


    public Client(){
        this.hashes = new LinkedList<>();
        this.randomPacket = new RandomPacket();
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InterruptedException {

        String hostname = "localhost";
        int port = 6548;
        int messageRate = 1;
        try {
            hostname = args[0];
            port = Integer.parseInt(args[1]);
            messageRate = Integer.parseInt(args[2]);
        } catch(Exception e){
            System.out.println("Usage: ");
            System.out.println("java cs455.scaling.client.Client [server-host] [server-port] [message-rate-per-sec]");
            return;
        }


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
        Client node = new Client();
        ClientTracker ct = new ClientTracker(20);
        Thread ctThread = new Thread(ct);
        ctThread.start();

        //sender
        int finalMessageRate = messageRate;
        Thread senderThread = new Thread(){
            public void run(){
                ByteBuffer senderBuffer = null;
                senderBuffer.allocate(80000);
                RandomPacket packet = new RandomPacket();


                while(true) {

                    try {
                        sleep(1000 / finalMessageRate);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    byte[] msg = packet.generate();
                    String hash = null;
                    try {
                        hash = packet.hash(msg);
//                        System.out.println("             hash out: "+hash);

                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    synchronized (node.hashes) {
                        node.hashes.add(hash);
                    }
//                    System.out.println("msg.size()="+msg.length);
//                    System.out.println("hash.size()="+hash.length());

                    senderBuffer = ByteBuffer.wrap(msg);
                    try {
                        client.write(senderBuffer);
                        ct.sent.incrementAndGet();
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
            }
        };
        senderThread.start();

        //receiver
        Thread receiverThread = new Thread(){
            public void run() {
                ByteBuffer receiverBuffer = null;
//                receiverBuffer.allocate(20);

                while (true) {

                    receiverBuffer = ByteBuffer.allocate(40);
                    int check = 0;
                    try {
                        check = client.read(receiverBuffer);
//                        System.out.println("read returned: "+check);
                        if(check < 0) continue;
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                    String response = new String(receiverBuffer.array()).trim();
//                        System.out.println("response:" + response);
                    int start = 0;
                    int count = 0;

                    while(response.length() > 39){
                            String token = response.substring(start,start+40);
                        synchronized (node.hashes) {
                            if (!node.hashes.contains(token)) {
//                                System.out.println("Bad! Unexpected hash received from Server.");
//                                System.out.println("     hash: " + token);
//                                ct.badHashes.incrementAndGet();
                            } else {
                                node.hashes.remove(token);
                                count++;
                            }
                        }
                        try {
                            response = response.substring(41, response.length());
                        } catch (java.lang.StringIndexOutOfBoundsException e){
                            break;
                        }
                    }
                    for(int i = 0; i < count; i++) {
                        ct.received.incrementAndGet();
                    }

                }
            }
        };
        receiverThread.start();
    }
}