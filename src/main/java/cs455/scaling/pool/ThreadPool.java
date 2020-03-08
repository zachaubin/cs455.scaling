package cs455.scaling.pool;

import java.util.concurrent.LinkedBlockingQueue;

public class ThreadPool {
    private int maxNumberActiveThreads;
    private LinkedBlockingQueue queue;
    private Swimmer[] swimmers;

    private int batchSize;
    private Runnable[] cumber_batch;

    public ThreadPool(int maxNumberActiveThreads, int batchSize) {
        this.maxNumberActiveThreads = maxNumberActiveThreads;
        queue = new LinkedBlockingQueue();
        //each swimmer is a thread that will process a cumber_batch
        swimmers = new Swimmer[maxNumberActiveThreads];

        //start thread containers
        for (int i = 0; i < maxNumberActiveThreads; i++) {
            swimmers[i] = new Swimmer();
            swimmers[i].start();// potential bad?
        }
        this.batchSize = batchSize;
        cumber_batch = new Runnable[batchSize];
        for(Runnable c : cumber_batch){
            c = null;
        }
    }

    public void poolOn(){
        for( Swimmer s : swimmers){
            s.start();
        }
    }

    public void execute(Runnable task) {
        synchronized (cumber_batch){
            for(Runnable c : cumber_batch){
                if(c == null) {
                    c = task;
                }
            }
            if(cumber_batch.length == batchSize){
                synchronized (queue) {
                    //add a full batch to the queue
                    queue.add(cumber_batch);
                    queue.notify();
                    //reset batch pointers
                    for(Runnable c : cumber_batch){
                        c = null;
                    }
                }
            }
        }

        synchronized (queue) {
            queue.add(task);
            queue.notify();
        }
    }

    public Thread next() throws InterruptedException {
        for(Thread t : swimmers){
            if(t.getState() != Thread.State.RUNNABLE){
                return t;
            }
        }
        swimmers[0].wait();
        return next();
    }


    //process a *full* batch of cumber_tasks_t
    private class Swimmer extends Thread {
        public void run() {
            Runnable[] cumber_batch;

            while (true) {
                synchronized (queue) {
                    while (queue.isEmpty()) {// change to continue to avoid lockups? add set=100 for a sleep at end, default prior to set=0
                        try {
                            queue.wait();//no batch to pop
                        } catch (InterruptedException e) {
                            System.out.println("::pool:ThreadPool:queue.wait():: ||empty queue error|| " + e.getMessage());
                            Thread.currentThread().getStackTrace();
                        }
                    }
                    cumber_batch = (Runnable[]) queue.poll();
                }
                try {
                    for(Runnable cumber_task : cumber_batch) {
                        cumber_task.run();
                        // concurrency issue? synchronize the run method of task object
                    }
//                    sleep(1000);// to observe the chunks
                } catch (RuntimeException e) {
                    System.out.println("::pool:ThreadPool:task.run():: " + e.getMessage());
                    Thread.currentThread().getStackTrace();
                }
            }
        }
    }
}
