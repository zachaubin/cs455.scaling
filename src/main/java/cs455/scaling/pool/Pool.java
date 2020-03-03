package cs455.scaling.pool;


/*
 This should allocate a given number of threads,
 maintain a queue of pending tasks, and
 assign tasks to be handled by the threads.
 */

import java.util.ArrayList;

public class Pool implements Runnable {

    ArrayList<Thread> pool;

    public Pool(int max){
        pool = new ArrayList<>(max);
        for(int i = 0; i < max; i++){
            Thread t = new Thread();
            pool.add(t);
        }
    }

    /////////////////////////http://tutorials.jenkov.com/java-concurrency/thread-pools.html

    @Override
    public void run() {
        for(Thread t : pool){
            t.start();
        }
        while(true){

        }
    }
}
