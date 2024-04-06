package org.openhab.binding.onecta.internal.api.dto.units;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.annotations.SerializedName;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FanMovement {
    @SerializedName("currentMode")
    private FanCurrentMode currentMode;

    public FanCurrentMode getCurrentMode() {
        return currentMode;
    }
}
