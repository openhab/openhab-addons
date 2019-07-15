package org.openhab.binding.hydrawise.internal.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SetControllerResponse extends Response {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("controller_id")
    @Expose
    private String controllerId;
    @SerializedName("message")
    @Expose
    private String message;

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return
     */
    public String getControllerId() {
        return controllerId;
    }

    /**
     * @param controllerId
     */
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

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

}