package fps;

import java.util.concurrent.TimeUnit;

class MessageHandler {

    // Simulates message processing (sleeps 1 second)
    void handle(Message message) {
        Integer clientId = message.getClientId();
        try {
            System.out.println("START - Client ID = " + clientId);
            TimeUnit.SECONDS.sleep(1);
        } catch (Exception e) {
            System.out.println("Exception occurred while handling message for client ID = " + clientId);
        } finally {
            System.out.println("END - Client ID = " + clientId);
        }
    }
}
