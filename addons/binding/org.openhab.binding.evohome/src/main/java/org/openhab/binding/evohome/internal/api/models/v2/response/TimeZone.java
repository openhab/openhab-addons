package org.openhab.binding.evohome.internal.api.models.v2.response;

import com.google.gson.annotations.SerializedName;

public class TimeZone {

    @SerializedName("timeZoneId")
    public String timeZoneId;

    @SerializedName("displayName")
    public String displayName;

    @SerializedName("offsetMinutes")
    public int offsetMinutes;

    @SerializedName("currentOffsetMinutes")
    public int currentOffsetMinutes;

    @SerializedName("supportsDaylightSaving")
    public boolean supportsDaylightSaving;

}
