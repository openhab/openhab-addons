package org.openhab.binding.robonect.model.cmd;

public class StatusCommand implements Command {
    @Override
    public String toCommandURL(String baseURL) {
        return baseURL + "?cmd=status";
    }
}
