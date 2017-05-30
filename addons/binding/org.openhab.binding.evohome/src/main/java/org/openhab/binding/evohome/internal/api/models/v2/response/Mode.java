package org.openhab.binding.evohome.internal.api.models.v2.response;

import com.google.gson.annotations.SerializedName;

public class Mode {

    @SerializedName("systemMode")
    public String systemMode;

    @SerializedName("canBePermanent")
    public boolean canBePermanent;

    @SerializedName("canBeTemporary")
    public boolean canBeTemporary;

    @SerializedName("timingMode")
    public String timingMode;

    //TODO Should be of time time, format: 1.00:00:00
    @SerializedName("maxDuration")
    public String maxDuration;

    //TODO Should be of time time, format: 00:10:00
    @SerializedName("timingResolution")
    public String timingResolution;

}
