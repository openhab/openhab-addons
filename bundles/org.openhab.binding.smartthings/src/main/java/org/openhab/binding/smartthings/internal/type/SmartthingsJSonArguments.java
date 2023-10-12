package org.openhab.binding.smartthings.internal.type;

public class SmartthingsJSonArguments {
    protected String name;
    protected boolean optional;
    protected SmartthingsJSonSchema schema;

    public SmartthingsJSonArguments() {
        // attributes = new ArrayList<Object>();
    }

    public void setName(String value) {
        name = value;
    }

    public String getName() {
        return name;
    }

    public void setOptional(boolean value) {
        optional = value;
    }

    public boolean getOptional() {
        return optional;
    }

    public void setSchema(SmartthingsJSonSchema value) {
        schema = value;
    }

    public SmartthingsJSonSchema getSchema() {
        return schema;
    }
}
