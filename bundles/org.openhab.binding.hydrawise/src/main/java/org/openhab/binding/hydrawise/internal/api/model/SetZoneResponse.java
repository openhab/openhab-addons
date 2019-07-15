package org.openhab.binding.hydrawise.internal.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SetZoneResponse extends Response {
    @SerializedName("message")
    @Expose
    private String message;

    @SerializedName("message_type")
    @Expose
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
