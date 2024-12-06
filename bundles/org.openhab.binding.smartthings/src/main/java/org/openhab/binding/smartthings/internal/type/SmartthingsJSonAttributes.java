package org.openhab.binding.smartthings.internal.type;

public class SmartthingsJSonAttributes {
    protected String name;
    protected String setter;
    protected SmartthingsJSonSchema schema;

    public SmartthingsJSonAttributes() {
        // attributes = new ArrayList<Object>();
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        name = value;
    }

    public String getSetter() {
        return setter;
    }

    public void setSetter(String value) {
        setter = value;
    }

    public SmartthingsJSonSchema getSchema() {
        return schema;
    }

    public void setSchema(SmartthingsJSonSchema value) {
        schema = value;
    }
}
