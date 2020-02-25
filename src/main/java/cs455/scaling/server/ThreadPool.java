package cs455.scaling.server;

/*
The server relies on the thread pool to perform all tasks. The threads within the thread pool should be created just
once. Care must be taken to ensure that you are not inadvertently creating a new thread every time a task needs to be
performed. There is a steep deduction (see Section 4) if you are doing so. The thread pool needs methods that allow:
  (1) a spare worker thread to be retrieved and
  (2) a worker thread to return itself to the pool after it has finished it task.
*/

public class ThreadPool {

    // no new thread per thingy, make em and keep em clean

}
