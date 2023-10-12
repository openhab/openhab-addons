package org.openhab.binding.smartthings.internal.type;

public class SmartthingsJSonProperties {
    protected String name;
    protected String title;
    protected String type;

    public SmartthingsJSonProperties() {
        // attributes = new ArrayList<Object>();
    }

    public void setType(String value) {
        type = value;
    }

    public String getType() {
        return type;
    }

    public void setName(String value) {
        name = value;
    }

    public String getName() {
        return name;
    }

    public void setTitle(String value) {
        title = value;
    }

    public String getTitle() {
        return title;
    }

}
