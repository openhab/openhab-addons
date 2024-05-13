package org.openhab.binding.onecta.internal.api.dto.commands;

import com.google.gson.annotations.SerializedName;

public class CommandInteger {
    @SerializedName("value")
    public Integer value;

    @SerializedName("path")
    public String path;

    public CommandInteger(Integer value) {
        this.value = value;
    }

    public CommandInteger(Integer value, String path) {
        this.value = value;
        this.path = path;
    }
}
