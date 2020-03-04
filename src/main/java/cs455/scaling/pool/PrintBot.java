package cs455.scaling.pool;

import static java.lang.Thread.sleep;

public class PrintBot implements Runnable {

    public int id;

    public PrintBot(int id){
        this.id = id;
    }
    @Override
    public void run() {
        System.out.println("Hello! I am PrintBot! <"+id+">");
    }
}
