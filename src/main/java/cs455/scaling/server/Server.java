package cs455.scaling.server;


import cs455.scaling.bytes.RandomPacket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public class Server {


    public Server() throws IOException {
    }

    public static void register(Selector selector, ServerSocketChannel serverSocket) throws IOException{
        //grab incoming socket from cs455.scaling.server
        SocketChannel client = serverSocket.accept();
        //configure it to be a new channel
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        System.out.println("\t\tNew cs455.scaling.client registered.");

    }


    private static void readAndRespond(SelectionKey key) throws IOException, NoSuchAlgorithmException {
        //create buffer to write into
        ByteBuffer buffer = ByteBuffer.allocate(8000);

        //grab the socket from the key
        SocketChannel client = (SocketChannel) key.channel();
        //Read from it
        int bytesRead = client.read(buffer);
        //handle a closed connection
        if(bytesRead == -1) {
            client.close();
            System.out.println("client disconnected.");
        } else {
            RandomPacket r = new RandomPacket();
            byte[] msg = buffer.array();
            String hash = r.hash(msg);
            System.out.println("\t\tReceived: " );
            r.printBytes(msg);
            System.out.println("apparent hash: " + hash);
            //flip the buffer to new write
            buffer.flip();
            buffer = ByteBuffer.allocate(256);
            buffer = ByteBuffer.wrap(hash.getBytes());
            client.write(buffer);
            //clear the buffer
            buffer.clear();
        }
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        //open selector
        Selector selector = Selector.open();
        //create input channel
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
        serverSocket.bind(new InetSocketAddress(hostname,port));
        serverSocket.configureBlocking(false);
        //register our channel to the selector
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        //loop on selector
        while(true) {
            System.out.println("listening for new connection or new messages on >>> host["+hostname+"]:port["+port+"]");
            //block here
            selector.select();
            System.out.println("\tActivity on selector!");

            //Keys are ready
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            //loop over ready keys
            Iterator<SelectionKey> iter = selectedKeys.iterator();
            while(iter.hasNext()) {
                //grab current key
                SelectionKey key = iter.next();

                //Optional
                if(key.isValid() == false){
//                    register(selector, serverSocket);
                    continue;
                }
                if(key.isAcceptable()){
                    register(selector,serverSocket);
                }

                //previous connection has data to read
                if(key.isReadable()) {
                    System.out.println("reading");
                    readAndRespond(key);
                }

                //remove it from our set
                iter.remove();
            }
        }
    }
}