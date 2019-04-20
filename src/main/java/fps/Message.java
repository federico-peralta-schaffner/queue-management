package fps;

// Class that represents messages (messages contain just the client ID)
class Message {

    private final Integer clientId;

    Message(Integer clientId) {
        this.clientId = clientId;
    }

    Integer getClientId() {
        return clientId;
    }
}
