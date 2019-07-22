package org.openhab.binding.hydrawise.internal.api.model;

public class SetControllerResponse extends Response {

    private String name;

    private String controllerId;

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