package cs455.scaling.stats;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Thread.sleep;

public class Tracker implements Runnable {
    /*
    [timestamp]
    Server Throughput: x messages/s,
    Active Client Connections: y,
    Mean Per-client Throughput: p messages/s,
    Std. Dev. Of
    Per-client Throughput: q messages/s
     */

    public AtomicLong msgs;
    public volatile AtomicLong connections;
    public AtomicLong meanThroughput;
    private ArrayList<Long> throughputs;
    public AtomicLong iterations;
    long timeout;
    long t0;

    public Tracker(long timeout){
        this.msgs = new AtomicLong(0);
        this.connections = new AtomicLong(0);
        this.meanThroughput = new AtomicLong(0);
        this.iterations = new AtomicLong(0);
        this.timeout = timeout;
        this.throughputs = new ArrayList<>();
    }

    private void printStats(){
        long t = System.currentTimeMillis();
        System.out.println("-----------------Timestamp: "+ t);
        long throughput = msgs.get() / timeout;
        throughputs.add(throughput);
        long mean = 0;
        for(long one : throughputs){
            mean += one;
        }
        mean /= throughputs.size();
        long stddev = 0;
        for(long two : throughputs){
            stddev += (mean - two) * (mean - two);
        }
        stddev /= throughputs.size();
        float stddevFinal = (float)Math.sqrt((double)stddev);
        long cn = connections.get();
        if(cn == 0){
            System.out.println("Tracker: no connections");
            return;
        }
        iterations.incrementAndGet();
        meanThroughput.lazySet((meanThroughput.get() + throughput) / iterations.get() );
        System.out.println("         Server Throughput: " + throughput);
        System.out.println(" Active Client Connections: " + cn);
        System.out.println("Mean Per-client Throughput: " + mean);
        System.out.println("        Standard Deviation: " + stddevFinal);
        System.out.println("     Per-client Throughput: " + throughput/cn);


        msgs.lazySet(0);
    }

    @Override
    public void run() {
        while(true){
            t0 = System.currentTimeMillis();
            try {
                sleep(timeout * 1000);

                printStats();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
