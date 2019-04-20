package fps;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

class MessageHandler {

    // Simulates message processing (sleeps 1 second)
    void handle(Message message, int queueNumber) {
        Integer clientId = message.getClientId();
        try {
            LocalTime start = LocalTime.now();
            String startTime = start.format(DateTimeFormatter.ofPattern("hh:mm:ss"));
            System.out.println(startTime + " - thread #" + queueNumber + " - START - Client ID = " + clientId);

            TimeUnit.SECONDS.sleep(1);

        } catch (Exception e) {

            System.out.println("thread #" + queueNumber +
                    " - Exception occurred while handling message for client ID = " + clientId);

        } finally {
            LocalTime end = LocalTime.now();
            String endTime = end.format(DateTimeFormatter.ofPattern("hh:mm:ss"));
            System.out.println(endTime + " - thread #" + queueNumber + " - END - Client ID = " + clientId);
        }
    }
}
