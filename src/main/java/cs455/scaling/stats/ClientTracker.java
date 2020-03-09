package cs455.scaling.stats;

import cs455.scaling.client.Client;

import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Thread.sleep;

public class ClientTracker implements Runnable {

//    [timestamp] Total Sent Count: x, Total Received Count: y

    public AtomicLong sent;
    public AtomicLong received;
    private int timeout;

    public ClientTracker(int timeout){
        this.sent = new AtomicLong(0);
        this.received = new AtomicLong(0);
        this.timeout = timeout;
    }

    private void printStats(){
        long t = System.currentTimeMillis();
        System.out.println("--------Timestamp: "+ t);
        System.out.println("    Messages Sent: " + sent.get());
        System.out.println("Messages Received: " + received.get());


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
