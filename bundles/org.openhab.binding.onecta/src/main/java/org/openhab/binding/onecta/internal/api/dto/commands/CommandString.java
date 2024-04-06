package org.openhab.binding.onecta.internal.api.dto.commands;

import com.google.gson.annotations.SerializedName;

public class CommandString {
    @SerializedName("value")
    public String value;

    @SerializedName("path")
    public String path;

    public CommandString(String value) {
        this.value = value;
    }

    public CommandString(String value, String path) {
        this.value = value;
        this.path = path;
    }
}
