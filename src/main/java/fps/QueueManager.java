package fps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class QueueManager {

    // Client IDs of incoming messages
    private static final List<Integer> MESSAGE_CLIENT_IDS =
            List.of(1, 2, 3, 1, 5, 4, 3, 7, 8, 1, 3, 5);

    // Thread pool size
    private static final int N = 3;

    // Thread pool
    private ExecutorService threadPool = Executors.newFixedThreadPool(N);

    // No need to be concurrent here, but it should be in the real world
    private final BlockingQueue<Message> incomingMessagesQueue = new LinkedBlockingQueue<>();

    // Dedicated queues that sit on top of each thread of the thread pool
    private final List<BlockingQueue<Message>> dedicatedQueues = new ArrayList<>(N);

    // Queue used to notify about available threads
    private final BlockingQueue<Integer> availableThreads = new ArrayBlockingQueue<>(N);

    QueueManager() {
        // Initialize the incoming messages queue with sample messages
        incomingMessagesQueue.addAll(
                MESSAGE_CLIENT_IDS.stream()
                        .map(Message::new)
                        .collect(Collectors.toList()));

        // Create up to N dedicated queues and add them to a list,
        // so that they can be accessed by index
        // Also mark all dedicated queues as available to process messages
        // Finally, create up to N tasks that will handle dispatched messages,
        // binding them to each dedicated queue
        List<MessageReceiver> messageReceivers = new ArrayList<>(N);
        for (int i = 0; i < N; i++) {
            LinkedBlockingQueue<Message> dedicatedQueue = new LinkedBlockingQueue<>();
            dedicatedQueues.add(dedicatedQueue);
            MessageReceiver messageReceiver = new MessageReceiver(dedicatedQueue, i, availableThreads);
            messageReceivers.add(messageReceiver);
            availableThreads.offer(i);
        }

        // Now we only need to submit each message receiver task to the thread pool
        messageReceivers.forEach(threadPool::execute);
    }

    // Manage the queue (this is where all the action happens) :P
    private void manageQueue() {

        /*
         * GENERAL IDEA OF THE ALGORITHM:
         *
         * This method runs forever in an infinite loop. Elements are polled from the
         * **incoming messages queue** and dispatched whenever there's an available
         * thread in the thread pool. Polling from the incoming messages queue is
         * blocking, i.e. if there are no incoming messages, we wait.
         *
         * To guarantee that messages from a given client are processed sequentially,
         * there are up to N dedicated queues that sit on top of each thread of the thread
         * pool. Messages are dispatched from the incoming messages queue to either one of
         * these dedicated queues, i.e. the incoming messages queue is *multiplexed* into
         * N dedicated queues that are bound to each thread of the thread pool. We call
         * this the **message dispatcher**.
         *
         * It never happens that different dedicated queues contain messages from the same
         * client simultaneously. At any given time, messages from the same client are
         * sent to the same dedicated queue, although it might happen that later, messages
         * from this client are sent to another dedicated queue (i.e. when this dedicated
         * queue is drained or if it contains messages from another client).
         *
         * Finally, there exists an *availableThreads* queue that is used to notify the
         * message dispatcher that one dedicated queue has finished processing messages
         * from a client, i.e. that it has been drained.
         */

        // Map used to match message client IDs to dedicated queue numbers
        Map<Integer, Integer> dispatchMap = new ConcurrentHashMap<>(N);

        // Yep, run forever (unless we're interrupted)
        while (true) {

            Message message;
            try {
                // Block wait until an incoming message is available
                message = incomingMessagesQueue.take();

            } catch (InterruptedException e) {
                System.out.println("Interrupted while waiting for incoming message");
                Thread.currentThread().interrupt();
                // Honor interruption: break
                break;
            }

            Integer clientId = message.getClientId();

            // The dispatchToDedicatedQueue method executes atomically
            // (because of ConcurrentHashMap.compute method)
            Integer dispatchedQueueNumber = dispatchMap.compute(
                    clientId,
                    (unused, queueNumber) -> dispatchToDedicatedQueue(message, queueNumber));

            // If message has not been dispatched (due to interruption)
            if (dispatchedQueueNumber == null) {
                // Honor interruption: break
                break;
            }
        }

        // Shutdown gracefully, release resources, etc
        shutdown();
    }

    // Attempt to find a queue with messages from the same client ID
    // If found, put the message directly in the queue
    // If not, block wait until a dedicated queue is available
    // and put the message there
    private Integer dispatchToDedicatedQueue(Message message, Integer queueNumber) {

        try {
            // Get dedicated queue number
            Integer newQueueNumber = queueNumber == null ?
                    availableThreads.take() :
                    queueNumber;

            // Get dedicated queue and dispatch message
            dedicatedQueues.get(newQueueNumber).offer(message);

            // Return the new dedicated queue number,
            // (which will also be put in the dispatchMap)
            return newQueueNumber;

        } catch (InterruptedException e) {
            System.out.println("Interrupted while waiting for dedicated queue available");
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private void shutdown() {
        // Shutdown executor cleanly, waiting up to 10 seconds
        try {
            threadPool.shutdown();
            threadPool.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.out.println("Interrupted while awaiting for thread pool shutdown");
            Thread.currentThread().interrupt();
        } finally {
            threadPool.shutdownNow();
        }
        System.out.println("Incoming messages queue management stopped.");
    }

    public static void main(String[] args) {
        // Run the whole simulation
        // Create sample messages and manage the queue forever
        new QueueManager().manageQueue();
    }
}
