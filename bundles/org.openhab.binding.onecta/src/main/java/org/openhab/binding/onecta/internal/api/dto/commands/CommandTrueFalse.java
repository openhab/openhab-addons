package org.openhab.binding.onecta.internal.api.dto.commands;

import org.openhab.binding.onecta.internal.api.Enums;

import com.google.gson.annotations.SerializedName;

public class CommandTrueFalse {
    @SerializedName("value.enabled")
    public boolean value;

    public CommandTrueFalse(Enums.OnOff value) {
        this.value = value.getValue().equals(Enums.OnOff.ON);
    }
}
