package org.openhab.binding.evohome.internal.api.models.v2.response;

import com.google.gson.annotations.SerializedName;

public class SystemModeStatus {

    @SerializedName("mode")
    public String mode;

    @SerializedName("isPermanent")
    public boolean isPermanent;

}
