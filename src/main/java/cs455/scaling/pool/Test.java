package cs455.scaling.pool;

public class Test {

    public static void main(String[] args) {
        int maxThreads;
        if(!args[0].isEmpty()){
            maxThreads = Integer.parseInt(args[0]);
        } else {
            maxThreads = 4;
        }
        ThreadPool pool = new ThreadPool(maxThreads);
        for (int i = 0; i < 50; i++) {
            PrintBot task = new PrintBot(i);
            pool.execute(task);
        }
    }
}
