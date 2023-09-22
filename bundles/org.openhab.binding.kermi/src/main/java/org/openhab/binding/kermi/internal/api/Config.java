package org.openhab.binding.kermi.internal.api;

import java.util.Map;

import com.google.gson.annotations.SerializedName;

public class Config {

    @SerializedName("DatapointConfigId")
    private String datapointConfigId;

    @SerializedName("DisplayName")
    private String displayName;

    @SerializedName("Description")
    private String description;

    @SerializedName("WellKnownName")
    private String wellKnownName;

    @SerializedName("Unit")
    private String unit;

    @SerializedName("PossibleValues")
    private Map<String, String> possibleValues;

    public String getDatapointConfigId() {
        return datapointConfigId;
    }

    public void setDatapointConfigId(String datapointConfigId) {
        this.datapointConfigId = datapointConfigId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWellKnownName() {
        return wellKnownName;
    }

    public void setWellKnownName(String wellKnownName) {
        this.wellKnownName = wellKnownName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Map<String, String> getPossibleValues() {
        return possibleValues;
    }

    public void setPossibleValues(Map<String, String> possibleValues) {
        this.possibleValues = possibleValues;
    }

}
