package org.openhab.binding.kermi.internal.api;

import com.google.gson.annotations.SerializedName;

public class DeviceOption {

    @SerializedName("OptionId")
    private String optionId;

    @SerializedName("Name")
    private String name;

    @SerializedName("IsActivated")
    private boolean isActivated;

    public String getOptionId() {
        return optionId;
    }

    public void setOptionId(String optionId) {
        this.optionId = optionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActivated() {
        return isActivated;
    }

    public void setActivated(boolean isActivated) {
        this.isActivated = isActivated;
    }

}
