package org.openhab.binding.evohome.internal.api.models.v2.request;

import com.google.gson.annotations.SerializedName;

public class Mode {

    public Mode(String mode) {
        systemMode = mode;
        timeUntil  = null;
        permanent  = true;
    }

    public Mode(String mode, String time) {
        systemMode = mode;
        timeUntil  = null;
        permanent  = false;
        // TODO {"SystemMode":mode,"TimeUntil":"%sT00:00:00Z" % until.strftime('%Y-%m-%d'),"Permanent":False}
    }

    @SerializedName("SystemMode")
    public String systemMode;

    @SerializedName("TimeUntil")
    public String timeUntil;

    @SerializedName("Permanent")
    public boolean permanent;

}
