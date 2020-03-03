package cs455.scaling.pool;

public class Test {

    public static void main(String[] args){
        ThreadPool pool = new ThreadPool(10,5);
        for(int i = 0; i < 50; i++){
            PrintBot p = new PrintBot(i);
            Thread thread = new Thread(p);
            pool.execute(thread);
        }
        System.out.println("Test: queue.size():"+pool.queue.size());
        pool.run();
    }




}
