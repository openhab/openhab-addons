package org.openhab.binding.evohome.internal.api.models.v2.response;

import com.google.gson.annotations.SerializedName;

public class TimeZone {
    @SerializedName("timeZoneId")
    public String TimeZoneId;

    @SerializedName("displayName")
    public String DisplayName;

    @SerializedName("offsetMinutes")
    public int OffsetMinutes;

    @SerializedName("currentOffsetMinutes")
    public int CurrentOffsetMinutes;

    @SerializedName("supportsDaylightSaving")
    public boolean SupportsDaylightSaving;

}
