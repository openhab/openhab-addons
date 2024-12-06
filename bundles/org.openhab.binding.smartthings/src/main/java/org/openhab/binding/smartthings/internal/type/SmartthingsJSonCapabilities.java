package org.openhab.binding.smartthings.internal.type;

import java.util.List;

public class SmartthingsJSonCapabilities {
    protected String id;
    protected String version;
    protected String status;
    protected String name;
    protected boolean ephemeral;

    protected List<SmartthingsJSonAttributes> attributes;
    protected List<SmartthingsJSonCommands> commands;

    public SmartthingsJSonCapabilities() {
        // attributes = new ArrayList<Object>();
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        id = value;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String value) {
        version = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        status = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        name = value;
    }

    public boolean getEphemeral() {
        return ephemeral;
    }

    public void setEphemeral(boolean value) {
        ephemeral = value;
    }

    public List<SmartthingsJSonAttributes> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<SmartthingsJSonAttributes> value) {
        attributes = value;
    }

    public List<SmartthingsJSonCommands> getCommands() {
        return commands;
    }

    public void setCommands(List<SmartthingsJSonCommands> value) {
        commands = value;
    }
}
