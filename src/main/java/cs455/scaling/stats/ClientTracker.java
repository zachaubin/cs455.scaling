package cs455.scaling.stats;

import cs455.scaling.client.Client;

import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Thread.sleep;

public class ClientTracker implements Runnable {

//    [timestamp] Total Sent Count: x, Total Received Count: y

    public AtomicLong sent;
    public AtomicLong received;
    private int timeout;
    public AtomicLong badHashes;

    public ClientTracker(int timeout){
        this.sent = new AtomicLong(0);
        this.received = new AtomicLong(0);
        this.timeout = timeout;
        this.badHashes = new AtomicLong(0);
    }

    private void printStats(){
        long t = System.currentTimeMillis();
        System.out.println("--------Timestamp: "+ t);
        System.out.println("    Messages Sent: " + sent.get());
        System.out.println("Messages Received: " + received.get());
//        if(badHashes.get() > 0){
//            System.out.println("       Bad Hashes: " + badHashes.get());
//        }
    }

    @Override
    public void run() {
        while(true){
            try {
                sleep(timeout * 1000);
                printStats();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
