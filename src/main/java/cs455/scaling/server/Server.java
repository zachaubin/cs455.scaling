package cs455.scaling.server;


import cs455.scaling.bytes.RandomPacket;
import cs455.scaling.pool.PrintBot;
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
import java.util.Random;
import java.util.Set;

import static java.lang.Thread.sleep;

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

    static class RNR implements Runnable {

        SelectionKey key;

        RNR(SelectionKey k ){
            key = k;
        }

        @Override
        public void run() {
            try {
                readAndRespond(key);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
    }


    public static void readAndRespond(SelectionKey key) throws IOException, NoSuchAlgorithmException {
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
            // ##stats## add +1 to msgs processed

//            System.out.println("\t\tReceived: " );
//            r.printBytes(msg);
//            System.out.println("apparent hash: " + hash);
            //flip the buffer to new write
            buffer.flip();
            buffer = ByteBuffer.allocate(256);
            buffer = ByteBuffer.wrap(hash.getBytes());
            client.write(buffer);
            //clear the buffer
            buffer.clear();
        }
    }
    private static class ReadAndRespond implements Runnable {

        SelectionKey key = null;

        ReadAndRespond(SelectionKey key){
            this.key = key;
        }

        @Override
        public void run() {
//            synchronized (this) {
//                System.out.println("##############################staring task.run()");
//                //create buffer to write into
//                ByteBuffer buffer = ByteBuffer.allocate(8000);
//
//                //grab the socket from the key
//                SocketChannel client = (SocketChannel) key.channel();
//                //Read from it
//                int bytesRead = 0;
//                try {
//                    bytesRead = client.read(buffer);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                //handle a closed connection
//                if (bytesRead == -1) {
//                    try {
//                        client.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    System.out.println("client disconnected.");
//                } else {
//                    RandomPacket r = new RandomPacket();
//
//                    byte[] msg = buffer.array();
//                    String hash = null;
//                    try {
//                        hash = r.hash(msg);
//                    } catch (NoSuchAlgorithmException e) {
//                        e.printStackTrace();
//                    }
//                    // ##stats## add +1 to msgs processed
//
////            System.out.println("\t\tReceived: " );
////            r.printBytes(msg);
////                    System.out.println("apparent hash: " + hash);
//                    //flip the buffer to new write
//                    buffer.flip();
//                    buffer = ByteBuffer.allocate(256);
//                    buffer = ByteBuffer.wrap(hash.getBytes());
//                    try {
//                        client.write(buffer);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    //clear the buffer
//                    buffer.clear();
//                }
//            }
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

        ThreadPool threadPool = new ThreadPool(poolSize,batchSize);
        threadPool.poolOn();
        Tracker tracker = new Tracker(5);
        Thread trackerThread = new Thread(tracker);
        trackerThread.start();

        //loop on selector
        while(true) {
//            System.out.println("listening for new connection or new messages on >>> host["+hostname+"]:port["+port+"]");
            //block here
            boolean flag = false;
            int test = selector.select();
//            System.out.println("\tActivity on selector!: " + test);

            //Keys are ready
            Set<SelectionKey> selectedKeys = selector.selectedKeys();

//            System.out.println("selectedKeys.size()="+selectedKeys.size());
            //loop over ready keys
            Iterator<SelectionKey> iter = selectedKeys.iterator();
            while(iter.hasNext()) {
                //grab current key
                SelectionKey key = iter.next();
                System.out.println("key: "+key);

                //Optional
                if(key.isValid() == false){
//                    register(selector, serverSocket);
                    sleep(100);
                    iter.remove();
                    System.out.println("not valid key?");

                    continue;
                }
                if(key.isAcceptable()){
                    register(selector,serverSocket);
                    tracker.connections.incrementAndGet();
//                    System.out.println("??registering");
                }
                if(key.isWritable()){
                    break;
                }

                //previous connection has data to read
                if(key.isReadable()) {
                    tracker.msgs.incrementAndGet();

//                    System.out.println("reading, key="+key);

//                    RNR rnr = new RNR(key);

                    threadPool.execute(key);
                    sleep(69);
//                    threadPool.execute(new PrintBot(0));
//                    readAndRespond(key);
//                    break;

                    key.interestOps(SelectionKey.OP_WRITE);
                    System.out.println("set key to op write");
                }



                //remove it from our set
                iter.remove();
            }
        }
    }
}