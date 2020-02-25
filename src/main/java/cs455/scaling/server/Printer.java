package cs455.scaling.server;

/*
Every 20 seconds, the server should print its current throughput (number of messages processed per second
during  last  20  seconds),  the  number  of  active  client  connections,  and  mean  and  standard deviation
of per-client throughput to the console. In order to calculate the per-client throughput statistics
(mean and standard deviation), you need to maintain the through puts for individual clients for last 20 seconds
(number of messages processed per second sent by a particular client during last 20 seconds) and calculate the
mean and the standard deviation of those throughput values. This message should look like the following:

[timestamp] Server Throughput: x messages/s,
Active Client Connections: y,
Mean Per-client Throughput: p messages/s,
Std. Dev. Of Per-client Throughput: q messages/s
 */


public class Printer {
}
