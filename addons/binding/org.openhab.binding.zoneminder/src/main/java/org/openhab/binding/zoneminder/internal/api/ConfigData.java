package org.openhab.binding.zoneminder.internal.api;

import com.google.gson.JsonElement;

public class ConfigData extends ZoneMinderApiData {

    private String id;
    private String name;
    private String value;
    private String type;
    private String defaultValue;
    private String readonly;

    public ConfigData(JsonElement id, JsonElement name, JsonElement value, JsonElement type, JsonElement defaultValue,
            JsonElement readonly) {
        this.id = id.getAsString();
        this.name = name.getAsString();
        this.value = value.getAsString();
        this.type = type.getAsString();
        this.defaultValue = defaultValue.getAsString();
        this.readonly = readonly.getAsString();
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
