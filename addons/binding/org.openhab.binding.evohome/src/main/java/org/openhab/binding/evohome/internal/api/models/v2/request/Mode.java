package org.openhab.binding.evohome.internal.api.models.v2.request;

import com.google.gson.annotations.SerializedName;

public class Mode {

    public Mode(String mode) {
        SystemMode = mode;
        TimeUntil = null;
        Permanent = true;
    }

    public Mode(String mode, String time) {
        SystemMode = mode;
        TimeUntil = null;
        Permanent = false;
        // TODO {"SystemMode":mode,"TimeUntil":"%sT00:00:00Z" % until.strftime('%Y-%m-%d'),"Permanent":False}
    }

    @SerializedName("SystemMode")
    public String SystemMode;

    @SerializedName("TimeUntil")
    public String TimeUntil;

    @SerializedName("Permanent")
    public boolean Permanent;

}
