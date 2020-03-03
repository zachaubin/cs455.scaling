package cs455.scaling.pool;

import java.util.concurrent.ArrayBlockingQueue;

public class Swimmer extends Thread {

    ArrayBlockingQueue queue = null;
    boolean swimming = false;

    public Swimmer(ArrayBlockingQueue queue){
        this.queue = queue;
    }

    public boolean isSwimming(){
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
                runnable.run();
            } catch(Exception e){
                System.err.println("pool:Swimmer:: error running a swimmer.");
                Thread.currentThread().getStackTrace();
                // name swimmers?
            }
        }
    }

}
