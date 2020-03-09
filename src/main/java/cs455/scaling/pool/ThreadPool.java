package cs455.scaling.pool;

import cs455.scaling.server.Server;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.channels.SelectionKey;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import static java.lang.Thread.sleep;

public class ThreadPool {
    private int maxNumberActiveThreads;
    private volatile LinkedBlockingQueue<ArrayList<SelectionKey>> queue;
    private Swimmer[] swimmers;

    private int batchSize;
    private Runnable[] cumber_batch;

    private ArrayList<SelectionKey> keys;//batch

    private Thread[] pool;
    private Server server;

    public ThreadPool(int maxNumberActiveThreads, int batchSize) {
        this.maxNumberActiveThreads = maxNumberActiveThreads;
        queue = new LinkedBlockingQueue();
        //each swimmer is a thread that will process a cumber_batch
        swimmers = new Swimmer[maxNumberActiveThreads];

        //start thread containers
        pool = new Thread[maxNumberActiveThreads];
        for (int i = 0; i < maxNumberActiveThreads; i++) {
            swimmers[i] = new Swimmer(i);
            pool[i] = new Thread(swimmers[i]);

//            swimmers[i].start();// potential bad?
        }
        this.batchSize = batchSize;
        cumber_batch = new Runnable[batchSize];
        for(Runnable c : cumber_batch){
            c = null;
        }
        keys = new ArrayList<>();
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
            swimmers[i] = new Swimmer(i);
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

    public void execute(Runnable task) {
//        synchronized (cumber_batch){
//            for(Runnable c : cumber_batch){
//                if(c == null) {
//                    c = task;
//                    break;
//                }
//            }
//            if(cumber_batch.length == batchSize){
//                synchronized (queue) {
//                    //add a full batch to the queue
//                    queue.add(cumber_batch);
//                    queue.notify();
//                    System.out.println("queue updated");
//                    //reset batch pointers
//                    for(Runnable c : cumber_batch){
//                        c = null;
//                    }
//                }
//            }
//        }
    }
    public void execute(SelectionKey key) {
        synchronized (keys){
            keys.add(key);
            System.out.println("key added:"+key);

            if(keys.size() == batchSize){
//                synchronized (queue) {

                    //add a full batch to the queue
                    queue.offer(keys);
//                    queue.notify();
                    System.out.println("queue updated");
                    //reset batch pointers
                    keys.clear();
//                }
            }
        }
        System.out.println("end of key add");
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
        Swimmer(int id){
            this.id = id;
        }
        @Override
        public void run() {
            ArrayList<SelectionKey> keys = new ArrayList<>();
            try {
                Server server = new Server();
            } catch (IOException e) {
                e.printStackTrace();
            }
//            System.out.println(">>>>swimmer started");

            while (true) {
//                synchronized (queue) {
                    try {
                        keys = queue.take();
                        System.out.println("::pool:ThreadPool:Swimmer[" + id + "]:took keys");
                        for (SelectionKey key : keys) {
                            server.readAndRespond(key);
                            System.out.println("running task");
                            sleep(69);
                            // concurrency issue? synchronize the run method of task object
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//                }

//                    try {
//                        for (SelectionKey key : keys) {
//                            server.readAndRespond(key);
//                        System.out.println("running task");
//                            sleep(69);
//                            // concurrency issue? synchronize the run method of task object
//                        }
//                        sleep(100);// to observe the chunks
//                    } catch (RuntimeException e) {
//                        System.out.println("::pool:ThreadPool:Swimmer[" + id + "]:task.run():: " + e.getMessage());
//                        Thread.currentThread().getStackTrace();
//                    } catch (NoSuchAlgorithmException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                }
            }



        }


}
