package cs455.scaling.server;


import cs455.scaling.bytes.RandomPacket;
import cs455.scaling.pool.ThreadPool;
import cs455.scaling.stats.Tracker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Set;

import static java.lang.Thread.sleep;

public class Server {
    public RandomPacket randomPacket = new RandomPacket();


    public Server() throws IOException {
    }

    public static void register(Selector selector, ServerSocketChannel serverSocket) throws IOException{
        //grab incoming socket from cs455.scaling.server
        SocketChannel client = serverSocket.accept();
        //configure it to be a new channel
        if(client.isRegistered()){
            return;
        }
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
//        System.out.println("\t\tNew cs455.scaling.client registered.");
    }

    public static void readAndRespond(SelectionKey key, Tracker tracker) throws IOException, NoSuchAlgorithmException {
        //create buffer to write into

        ByteBuffer buffer = ByteBuffer.allocate(8000);

            //grab the socket from the key
            SocketChannel client = (SocketChannel) key.channel();
                //Read from it
                int bytesRead = client.read(buffer);
                //handle a closed connection
                if (bytesRead == -1) {
                    client.close();
                    System.out.println("client disconnected.");
                    tracker.connections.decrementAndGet();
                } else {
                    RandomPacket r = new RandomPacket();

                    byte[] msg = buffer.array();
                    String hash = r.hash(msg);

                    //flip the buffer to new write
                    buffer.flip();
                    buffer = ByteBuffer.allocate(40);
//                    System.out.println("hash:"+hash);
//                    System.out.println("hash.size():"+hash.length());
                    buffer = ByteBuffer.wrap(hash.getBytes(),0,40);
                    client.write(buffer);
                    //clear the buffer
                    buffer.clear();
                }
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InterruptedException {
        //open selector
        Selector selector = Selector.open();
        //create input channel
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        String hostname = "localhost";
        int port = 6548;
        int poolSize = 1;//num threads to be used in pool
        int batchSize = 1;//num msgs to be processed at once
        int batchTime = 1;// countdown (from first add) til just do work in queue even if !=batchSize

        try{
            port = Integer.parseInt(args[0]);
            poolSize  = Integer.parseInt(args[1]);
            batchSize = Integer.parseInt(args[2]);
            batchTime = Integer.parseInt(args[3]);
        } catch (Exception e) {
            System.out.println("Usage: ");
            System.out.println("java cs455.scaling.server.Server [port] [thread-pool-size] [batch-size] [batch-time]");
            return;
        }

        serverSocket.bind(new InetSocketAddress(hostname,port));
        serverSocket.configureBlocking(false);
        //register our channel to the selector
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        //thread pool and tracker thread
        Tracker tracker = new Tracker(20);
        ThreadPool threadPool = new ThreadPool(poolSize,batchSize,batchTime,tracker);
        threadPool.poolOn();
        Thread trackerThread = new Thread(tracker);
        trackerThread.start();

        //loop on selector
        while(true) {
            boolean flag = false;
            int test = selector.select();

            //Keys are ready
            Set<SelectionKey> selectedKeys = selector.selectedKeys();

            //loop over ready keys
            Iterator<SelectionKey> iter = selectedKeys.iterator();
            while(iter.hasNext()) {
                //grab current key
                SelectionKey key = iter.next();

                if(key.isValid() == false){
                    iter.remove();
//                    System.out.println("not valid key?");
                    continue;
                }
                if(key.isAcceptable()){
                    register(selector,serverSocket);
                    tracker.connections.incrementAndGet();
                }
                if(key.isWritable() ){
                    break;
                }

                //previous connection has data to read
                if(key.isReadable() && (key.attachment() == (Object)0 || key.attachment() == null) ) {
                    tracker.msgs.incrementAndGet();
                    threadPool.execute(key);
//                    System.out.println("::Server: execute(key)");
                }
                iter.remove();
            }
        }
    }
}