package cs455.scaling.pool;


/*
 This should allocate a given number of threads,
 maintain a queue of pending tasks, and
 assign tasks to be handled by the threads.
 */

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static java.lang.System.exit;
import static java.lang.Thread.sleep;

public class ThreadPool implements Runnable {

    ArrayList<Swimmer> pool;
    LinkedBlockingQueue<Thread> queue = null;

    boolean poolOpen;


    public ThreadPool(int max, int init){
        if( init > max ){
            System.err.println("Pool:pool:: error cannot init [" + init + "] threads with a max of [" + max + "].");
            Thread.currentThread().getStackTrace();
            exit(1);
        }
        queue = new LinkedBlockingQueue();
        pool = new ArrayList<>(max);

        for(int i = 0; i < init; i++){
            Swimmer s = new Swimmer(queue);
            pool.add(s);
        }
    }

    public synchronized void execute(Thread thread) {
//        if(!this.swimming()) {
//            System.err.println("");
//        }
        this.queue.offer(thread);
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
        while(true){
            Thread thread = null;
            try {
                thread = queue.take();
                System.out.println("taking thread, queue.size():"+queue.size());
            } catch (InterruptedException e) {
                System.err.println("pool:ThreadPool:: error taking from queue ");
                e.printStackTrace();
            }
            thread.run();



        }
    }
}
