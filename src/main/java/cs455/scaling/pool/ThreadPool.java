package cs455.scaling.pool;

import java.util.concurrent.LinkedBlockingQueue;

public class ThreadPool {
    private int maxThreads;
    private LinkedBlockingQueue queue;
    private Swimmer[] swimmers;

    public ThreadPool(int maxThreads) {
        this.maxThreads = maxThreads;
        queue = new LinkedBlockingQueue();
        swimmers = new Swimmer[maxThreads];

        //start thread containers
        for (int i = 0; i < maxThreads; i++) {
            swimmers[i] = new Swimmer();
            swimmers[i].start();
        }
    }

    public void execute(Runnable task) {
        synchronized (queue) {
            queue.add(task);
            queue.notify();
        }
    }

    private class Swimmer extends Thread {
        public void run() {
            Runnable task;

            while (true) {
                synchronized (queue) {
                    while (queue.isEmpty()) {
                        try {
                            queue.wait();
                        } catch (InterruptedException e) {
                            System.out.println("::pool:ThreadPool:queue.wait():: ||empty queue error|| " + e.getMessage());
                            Thread.currentThread().getStackTrace();
                        }
                    }
                    task = (Runnable) queue.poll();
                }
                try {
                    task.run();
//                    sleep(1000);// to observe the chunks
                } catch (RuntimeException e) {
                    System.out.println("::pool:ThreadPool:task.run():: " + e.getMessage());
                    Thread.currentThread().getStackTrace();
                }
            }
        }
    }
}
