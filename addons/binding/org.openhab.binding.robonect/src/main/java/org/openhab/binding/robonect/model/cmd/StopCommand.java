package org.openhab.binding.robonect.model.cmd;

public class StopCommand implements Command {
    @Override
    public String toCommandURL(String baseURL) {
        return baseURL + "?cmd=stop";
    }
}