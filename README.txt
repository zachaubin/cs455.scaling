### Usage ###

How to use:
1. Start a server
2. Start a bunch of clients.

Server:
java -cp ./build/libs/cs455.scaling-1.0-SNAPSHOT.jar cs455.scaling.server.Server [port] [thread-pool-size] [batch-size] [batch-time]
 ( e.g. java -cp ./build/libs/cs455.scaling-1.0-SNAPSHOT.jar cs455.scaling.server.Server 4568 8 8 1)

Client:
java -cp ./build/libs/cs455.scaling-1.0-SNAPSHOT.jar cs455.scaling.client.Client [server-host] [server-port] [message-rate-per-sec]
 ( e.g. java -cp ./build/libs/cs455.scaling-1.0-SNAPSHOT.jar cs455.scaling.client.Client localhost 4568 4)

Notes:
Deregistration may fail if the client is mid-transmission. Known issue, catch "Broken Pipe"?
Some testing classes are present, see below.


### Description of Files ###

cs455.scaling.bytes
 Hash and HashSingleton
  :: testing - these were used early on for churning out specific sets of messages
 RandomPacket
  :: this makes random packets and computes SHA-1 hash

cs455.scaling.client
 Client
  :: this contains the three client threads
    :: first thread is statistics tracker
    :: second thread is sender, loops sending random messages
    :: third thread is receiver, loops taking in hashes (sometimes parsing them)

 cs455.scaling.pool
  PrintBot
   :: testing - artifact
  ThreadPool
   :: contains batch manager and pool specifically set up to work with "keys >> readAndRespond"
     :: worker threads are Swimmers, uses .poll(timeout) to handle incomplete batches

 cs455.scaling.server
  Server
    :: makes and starts thread pool
    :: thread for tracker and thread for looping over selector managed by thread pool

 cs455.scaling.stats
  ClientTracker
    :: stats tracker for client with !BONUS! bad hash counter output
  Tracker
    :: stats tracker for server


### Point Breakdown ###

Server Node
3 points: Correct thread pool manager implementation
>works on my machine
1 point:Abstracting different types of tasks as work units for worker threads
>read and respond is abstracted out, deals with a key, this is a "task", batches are made and dealt with in pool
1 point:Receiving data from Clients
>bytes come in, can be printed, hashes are matching up
1 point: Sending hash codes back to Clients
>hashes are checked, just started removing after finding them from the LL because I think it was getting large
1 point: Using the thread pool to manage connections
>looks like it works for many connections as it should
2 points:Supporting 100 concurrent client connections
>this will be the stress test, should be fine
1 point:Creating work units of configurable size
>batch size can be changed, playing with this number can make things weird if you try to break it
1 point:Processing work units within configurable time
>this is handled with queue.poll(timeout) where the incomplete batch is processed in lieu of a batch from queue

Client Node
1 point: Generation of random data.
>class RandomPacket does this with a Random
2 points: Maintaining linked list data structure of pending hashes.
>yes, see sender and receiver threads
1 point: Sending data to the Server.
>see sender thread
1 point: Receiving and verifying responses (hashes) from the Server.
>using hashes.contains(hash) and then hashes.remove(hash)