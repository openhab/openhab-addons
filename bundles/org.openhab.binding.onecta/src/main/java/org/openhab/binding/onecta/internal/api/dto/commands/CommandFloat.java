package org.openhab.binding.onecta.internal.api.dto.commands;

import com.google.gson.annotations.SerializedName;

public class CommandFloat {
    @SerializedName("value")
    public float value;

    @SerializedName("path")
    public String path;

    public CommandFloat(float value) {
        this.value = value;
    }

    public CommandFloat(float value, String path) {
        this.value = value;
        this.path = path;
    }
}
