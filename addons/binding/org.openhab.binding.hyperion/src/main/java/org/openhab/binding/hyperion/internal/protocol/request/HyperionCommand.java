package org.openhab.binding.hyperion.internal.protocol.request;

import org.openhab.binding.hyperion.internal.protocol.response.Response;

public abstract class HyperionCommand {

    private String command;

    private transient Response response;

    public HyperionCommand(String command) {
        setCommand(command);
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }
}
