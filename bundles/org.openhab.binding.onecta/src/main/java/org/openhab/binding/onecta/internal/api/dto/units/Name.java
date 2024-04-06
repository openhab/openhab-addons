package org.openhab.binding.onecta.internal.api.dto.units;

import com.google.gson.annotations.SerializedName;

public class Name {
    @SerializedName("settable")
    private boolean settable;
    @SerializedName("maxLength")
    private Integer maxLength;
    @SerializedName("value")
    private String value;

    public boolean isSettable() {
        return settable;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public String getValue() {
        return value;
    }
}
