package org.openhab.binding.onecta.internal.api.dto.commands;

import org.openhab.binding.onecta.internal.api.Enums;

import com.google.gson.annotations.SerializedName;

public class CommandOnOf {
    @SerializedName("value")
    public String value;

    @SerializedName("path")
    public String path;

    public CommandOnOf(Enums.OnOff value) {
        this.value = value.getValue();
    }

    public CommandOnOf(String value, String path) {
        this.value = value.toLowerCase();
        this.path = path;
    }
}
