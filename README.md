# Queue management mini-system

## Problem statement:
 
>There is a message queue that needs to be managed by a service (messages arrive to this queue asynchronously). Each message belongs to a client and the queue can contain more than one message belonging to the same client.
>
>The service has a thread pool of size `N` to attend incoming messages, with `N < #clients` (there are tons of clients, while `N` is a small number). Two threads cannot attend the same client at the same time and messages from one client must be dispatched sequentially (i.e. in the same order they have arrived).
>
>It cannot happen that, if messages are arriving from different clients and there are available threads, some message stays in the queue waiting for a busy thread.
>
>Your task is to implement a solution that manages the incoming messages queue and fulfils all the requirements, constraints and conditions specified above.

## Solution

This is a sketch of the solution, please read the comments in the code for further details...
 
The `QueueManager` class is the entry point of the simulation. Its `manageQueue` method is where most of the action happens.

### General idea of the algorithm

The `QueueManager.manageQueue` method runs forever in an infinite loop. Elements are polled from the **incoming messages queue** and dispatched whenever there's an available thread in the thread pool. Polling from the incoming messages queue is blocking, i.e. if there are no incoming messages, we wait.

To guarantee that messages from a given client are processed sequentially, there are up to N dedicated queues that sit on top of each thread of the thread pool. Messages are dispatched from the incoming messages queue to either one of these dedicated queues, i.e. the incoming messages queue is *multiplexed* into N dedicated queues that are bound to each thread of the thread pool. We call this the **message dispatcher**.

It never happens that different dedicated queues contain messages from the same client simultaneously. At any given time, messages from the same client are sent to the same dedicated queue, although it might happen that later, messages from this client are sent to another dedicated queue (i.e. when this dedicated queue is drained or if it contains messages from another client).

Finally, there exists an *availableThreads* queue that is used to notify the message dispatcher that one dedicated queue has finished processing messages from a client, i.e. that it has been drained.
