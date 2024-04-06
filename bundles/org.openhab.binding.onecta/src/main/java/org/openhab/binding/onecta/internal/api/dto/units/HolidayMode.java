package org.openhab.binding.onecta.internal.api.dto.units;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.annotations.SerializedName;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HolidayMode {
    @SerializedName("ref")
    private String ref;
    @SerializedName("settable")
    private boolean settable;
    @SerializedName("value")
    private HolidayModeValue value;

    public String getRef() {
        return ref;
    }

    public boolean isSettable() {
        return settable;
    }

    public String getValue() {
        return value.isEnabled() ? "ON" : "OFF";
    }
}
