package org.openhab.binding.robonect.model.cmd;

public interface Command {
    String toCommandURL(String baseURL);
}
