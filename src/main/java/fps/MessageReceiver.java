package fps;

import java.util.concurrent.BlockingQueue;

class MessageReceiver implements Runnable {

    private final BlockingQueue<Message> dedicatedQueue;

    private final int queueNumber;

    private final BlockingQueue<Integer> availableThreads;

    private final MessageHandler messageHandler = new MessageHandler();

    MessageReceiver(BlockingQueue<Message> dedicatedQueue,
                    int queueNumber,
                    BlockingQueue<Integer> availableThreads) {
        this.dedicatedQueue = dedicatedQueue;
        this.queueNumber = queueNumber;
        this.availableThreads = availableThreads;
    }

    @Override
    public void run() {

        // Runs forever, except if interrupted
        while (!Thread.currentThread().isInterrupted()) {

            Message message;
            try {
                // Block wait until a new message is available at this thread's dedicated queue
                message = dedicatedQueue.take();

            } catch (InterruptedException e) {
                System.out.println("Interrupted while waiting for message at dedicated queue");
                Thread.currentThread().interrupt();
                // Honor interruption: break
                break;
            }

            // Now handle the message
            messageHandler.handle(message, queueNumber);

            // Notify message dispatcher if the dedicated queue is available
            if (dedicatedQueue.isEmpty()) {
                availableThreads.offer(queueNumber);
            }

        }
    }
}
