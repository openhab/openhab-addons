package org.openhab.binding.robonect.model.cmd;

public class VersionCommand implements Command {
    @Override
    public String toCommandURL(String baseURL) {
        return baseURL + "?cmd=version";
    }
    
}
