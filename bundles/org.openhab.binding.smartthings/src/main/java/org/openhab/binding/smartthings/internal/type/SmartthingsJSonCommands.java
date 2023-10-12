package org.openhab.binding.smartthings.internal.type;

import java.util.List;

public class SmartthingsJSonCommands {
    protected String name;

    protected List<SmartthingsJSonArguments> arguments;

    public SmartthingsJSonCommands() {
        // attributes = new ArrayList<Object>();
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        name = value;
    }

    public List<SmartthingsJSonArguments> getCommands() {
        return arguments;
    }

    public void setCommands(List<SmartthingsJSonArguments> value) {
        arguments = value;
    }
}
