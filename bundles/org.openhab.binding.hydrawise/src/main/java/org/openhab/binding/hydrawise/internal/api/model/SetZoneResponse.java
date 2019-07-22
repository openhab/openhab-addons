package org.openhab.binding.hydrawise.internal.api.model;

public class SetZoneResponse extends Response {

    private String message;

    private String messageType;

    /**
     * @return
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return
     */
    public String getMessageType() {
        return messageType;
    }

    /**
     * @param messageType
     */
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

}
