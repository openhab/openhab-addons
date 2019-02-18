package org.openhab.binding.sensibosky.model;

import com.google.gson.annotations.SerializedName;

public class AcStateRead {
    @SerializedName("status")
    public String status;
    @SerializedName("reason")
    public String reason;
    @SerializedName("acState")
    public AcState acState;
}