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

The solution implemented here consists of:
 
- The `QueueManager` class, which performs a simulation of all the system
- An unbounded `incomingMessagesQueue` queue that represents... well, the incoming messages queue
- A `threadPool` of size `N` that executes tasks that handle incoming messages in parallel
- A `MessageDispatcher` class that runs in the main thread and encapsulates the mechanics to dispatch messages to available threads, as per the problem constraints and requirements
- The `MessageDispatcher` class contains a `messageQueues` list containing up to `N` queues, with each queue sitting on top of each thread of the threadpool. These queues are used to dispatch messages that belong to one given client sequentially, as per the problem statement
- A `MessageRecipient` class that receives messages dispatched by the `MessageDispatcher` class and performs synchronization stuff before delegating the received message to its corresponding handler. This class executes within the threadpool
- A `MessageHandler` class that actually handles each message. Message handling is simulated here by sleeping 1 second and printing the client ID both at the start and end of the sleeping period. This class executes within the threadpool

