package cs455.scaling.pool;


/*
 This should allocate a given number of threads,
 maintain a queue of pending tasks, and
 assign tasks to be handled by the threads.
 */

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static java.lang.System.exit;

public class ThreadPool implements Runnable {

    ArrayList<Swimmer> pool;
    ArrayBlockingQueue queue = null;

    boolean poolOpen;


    public ThreadPool(int max, int init){
        if( init > max ){
            System.err.println("Pool:pool:: error cannot init [" + init + "] threads with a max of [" + max + "].");
            Thread.currentThread().getStackTrace();
            exit(1);
        }
        queue = new ArrayBlockingQueue(max);
        pool = new ArrayList<>(max);

        for(int i = 0; i < init; i++){
            Swimmer s = new Swimmer(queue);
            pool.add(s);
        }
    }

    public synchronized void execute(Runnable runnable) {
        this.queue.offer(runnable);
    }

    public synchronized void kill(){
        poolOpen = false;
        for(Swimmer thread : pool){
            thread.kill();
        }
    }

    /////////////////////////http://tutorials.jenkov.com/java-concurrency/thread-pools.html

    @Override
    public void run() {
        poolOpen = true;
        for(Thread t : pool){
            t.start();
        }
        while(true){

        }
    }
}
