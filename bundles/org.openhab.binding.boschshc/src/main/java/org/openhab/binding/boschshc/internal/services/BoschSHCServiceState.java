package org.openhab.binding.boschshc.internal.services;

import com.google.gson.annotations.SerializedName;

public class BoschSHCServiceState {
    @SerializedName("@type")
    private final String type;

    protected BoschSHCServiceState(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
