package org.openhab.binding.robonect.model.cmd;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NameCommand implements Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(NameCommand.class);

    private String newName;

    public NameCommand withNewName(String newName) {
        this.newName = newName != null ? newName : "";
        return this;
    }

    @Override
    public String toCommandURL(String baseURL) {
        if (newName == null) {
            return baseURL + "?cmd=name";
        } else {
            try {
                return baseURL + "?cmd=name&name=" + URLEncoder.encode(newName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                LOGGER.error("Could not encode name " + newName, e);
                return baseURL + "?cmd=name";
            }
        }
    }
}