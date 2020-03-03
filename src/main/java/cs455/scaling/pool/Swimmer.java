package cs455.scaling.pool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Swimmer extends Thread {

    LinkedBlockingQueue queue = null;
    boolean swimming = false;

    public Swimmer(LinkedBlockingQueue queue){
        this.queue = queue;
    }

    public boolean swimming(){
        return swimming;
    }

    public void kill(){
        swimming = false;
        this.interrupt();
    }

    public void run(){
        while(!swimming){
            try{
                Runnable runnable = (Runnable) queue.take();
                System.out.println("swimmer: took, queue size:" + queue.size());
                runnable.run();
            } catch(Exception e){
                System.err.println("pool:Swimmer:: error running a swimmer.");
                Thread.currentThread().getStackTrace();
                // name swimmers?
            }
        }
    }

}