package org.openhab.binding.onecta.internal.api.dto.units;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Setpoints {
    private IconID roomTemperature;
    private IconID leavingWaterTemperature;
    private IconID leavingWaterOffset;

    private IconID domesticHotWaterTemperature;

    public IconID getRoomTemperature() {
        return roomTemperature;
    }

    public IconID getLeavingWaterTemperature() {
        return leavingWaterTemperature;
    }

    public IconID getLeavingWaterOffset() {
        return leavingWaterOffset;
    }

    public IconID getdomesticHotWaterTemperature() {
        return domesticHotWaterTemperature;
    }
}
