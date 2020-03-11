package cs455.scaling.pool;

import com.sun.org.apache.bcel.internal.generic.Select;
import cs455.scaling.server.Server;
import cs455.scaling.stats.Tracker;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.channels.SelectionKey;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ThreadPool {
    private int maxNumberActiveThreads;
    private volatile LinkedBlockingQueue<ArrayList<SelectionKey>> queue;
    private Swimmer[] swimmers;

    private int batchSize;
    private Runnable[] cumber_batch;

    private volatile ArrayList<SelectionKey> keys;//batch

    private Thread[] pool;
    private Server server;

    private int batchTime;

    private Tracker tracker;

    public ThreadPool(int maxNumberActiveThreads, int batchSize,int batchTime) {
        this.maxNumberActiveThreads = maxNumberActiveThreads;
        queue = new LinkedBlockingQueue();
        //each swimmer is a thread that will process a cumber_batch
        swimmers = new Swimmer[maxNumberActiveThreads];

        keys = new ArrayList<>();


        tracker = new Tracker(0);
        //start thread containers
        pool = new Thread[maxNumberActiveThreads];
        for (int i = 0; i < maxNumberActiveThreads; i++) {
            swimmers[i] = new Swimmer(i,keys,tracker);
            pool[i] = new Thread(swimmers[i]);

//            swimmers[i].start();// potential bad?
        }
        this.batchSize = batchSize;
        cumber_batch = new Runnable[batchSize];
        for(Runnable c : cumber_batch){
            c = null;
        }
        this.batchTime = batchTime;



    }

    public void poolOn(){
        for( Thread t : pool){
            t.start();
        }
    }
    public ThreadPool(int maxNumberActiveThreads, int batchSize, Server server) {
        this.maxNumberActiveThreads = maxNumberActiveThreads;
        queue = new LinkedBlockingQueue();
        //each swimmer is a thread that will process a cumber_batch
        swimmers = new Swimmer[maxNumberActiveThreads];

        //start thread containers
        for (int i = 0; i < maxNumberActiveThreads; i++) {
            swimmers[i] = new Swimmer(i, keys,tracker);
//            swimmers[i].start();// potential bad?
        }
        this.batchSize = batchSize;
        cumber_batch = new Runnable[batchSize];
        for(Runnable c : cumber_batch){
            c = null;
        }
        pool = new Thread[maxNumberActiveThreads];
        this.server = server;
    }

    public void execute(SelectionKey key) {
        synchronized (keys){
            key.attach(1);
            keys.add(key);
//            System.out.println("xx keys.size()="+keys.size());


//            System.out.println("key added:"+key);
//            System.out.println("keys.size() = "+keys.size());
            if(keys.size() == batchSize){
//                synchronized (queue) {

                    //add a full batch to the queue
                ArrayList<SelectionKey> batch = new ArrayList<>();
                for(SelectionKey k : keys){
                    batch.add(k);
                }

                    queue.offer(batch);
//                    queue.notify();
//                    System.out.println("queue updated");
                    //reset batch pointers
                    keys.clear();
//                }
            }
        }
//        key.interestOps(key.interestOps() | SelectionKey.OP_READ);
//        System.out.println("end of key add");
    }

//
//    public Thread next() throws InterruptedException {
//        for(Thread t : swimmers){
//            if(t.getState() != Thread.State.RUNNABLE){
//                return t;
//            }
//        }
////        swimmers[0].wait();
//        return next();
//    }

    public String hash(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA1");
        byte[] hash  = digest.digest(data);
        BigInteger hashInt = new BigInteger(1, hash);

//        pad with leading 0's so size == 40,
//         it may have been stripped of leading 0's
        String hashString = hashInt.toString(16);
        while(hashString.length() < 40){
            hashString = "0" + hashString;
        }
        return hashString;
    }


    //processes a *full* batch of cumber_tasks_t
    private class Swimmer implements Runnable {
        int id;
        long timeoutStart = System.currentTimeMillis();
        Tracker tracker;

        ArrayList<SelectionKey> keys = new ArrayList<>();

        Swimmer(int id, ArrayList<SelectionKey> keys, Tracker tracker){
            this.keys = keys;
            this.id = id;
            this.tracker = tracker;
        }
        @Override
        public void run() {
            ArrayList<SelectionKey> batch = new ArrayList<>();
            boolean timeout = false;
            timeoutStart = System.currentTimeMillis();
            try {
                Server server = new Server();
            } catch (IOException e) {
                e.printStackTrace();
            }
//            System.out.println(">>>>swimmer started");

            while (true) {
//                synchronized (queue) {
                    try {
                            batch = queue.poll((long)batchTime * 1000,MILLISECONDS);
//                            System.out.println("batch.size()="+batch.size());
                        synchronized (keys) {

                            if(batch == null){
                            batch = new ArrayList<>();
//                                    System.out.println("timed out, keys.size()="+keys.size());
                                if(keys == null) continue;
                                if(keys.size() == 0) continue;
                                for( SelectionKey key : keys){
                                    System.out.println("key from unfinished batch:");//+key);
                                    batch.add(key);
                                }
                                keys.clear();
                            }
                        }

//                        System.out.println("::pool:ThreadPool:Swimmer[" + id + "]:took keys");
//                        System.out.println("keys.size()="+batch.size());
//                        if(batch == null) continue;
                        for (SelectionKey key : batch) {
                            synchronized(key){ //prevents hash mismatch and double send
                                server.readAndRespond(key,tracker);
                                key.attach(0);
                            }
                            System.out.println("running task");
//                            key.interestOps(key.interestOps() | SelectionKey.OP_READ);
//                            sleep(69);
                            // concurrency issue? synchronize the run method of task object
                        }
                        batch = null;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }



        }


}
