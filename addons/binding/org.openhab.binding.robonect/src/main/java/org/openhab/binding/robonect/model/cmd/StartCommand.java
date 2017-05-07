package org.openhab.binding.robonect.model.cmd;

public class StartCommand implements Command {
    @Override
    public String toCommandURL(String baseURL) {
        return baseURL + "?cmd=start";
    }
}