package cs455.scaling.server;

/*
The thread pool manager also maintains a list of the work that it needs to perform. It maintains these work units in
a FIFO queue implemented using the linked list data structure. Each unit of work is a list of data packets with a
maximum length of batch-size. Work units are added to the tail of the work queue and when the work unit at the top
of the queue has either:
  (1) reached a length of batch-size or
  (2) batch-time has expired since the previous unit was
removed, an available worker is assigned to the work unit.
 */

public class ThreadWorkQueue {
}
