package org.openhab.binding.smartthings.internal.type;

import java.util.List;

public class SmartthingsJSonSchema {
    protected String type;
    protected List<SmartthingsJSonProperties> properties;
    protected boolean additionalProperties;
    protected String title;

    public SmartthingsJSonSchema() {
        // attributes = new ArrayList<Object>();
    }

    public String getType() {
        return type;
    }

    public void setType(String value) {
        type = value;
    }

    public List<SmartthingsJSonProperties> getProperties() {
        return properties;
    }

    public void setAttributes(List<SmartthingsJSonProperties> value) {
        properties = value;
    }

    public boolean getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(boolean value) {
        additionalProperties = value;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String value) {
        title = value;
    }
}
