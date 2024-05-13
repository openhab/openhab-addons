package org.openhab.binding.onecta.internal.api.dto.units;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.annotations.SerializedName;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HolidayModeValue {
    @SerializedName("enabled")
    private boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }
}
