package org.openhab.binding.evohome.internal.api.models.v2.response;

import com.google.gson.annotations.SerializedName;

public class LocationInfo {
    @SerializedName("locationId")
    public int LocationId;

    @SerializedName("name")
    public String Name;

    @SerializedName("streetAddress")
    public String StreetAddress;

    @SerializedName("city")
    public String City;

    @SerializedName("country")
    public String Country;

    @SerializedName("postcode")
    public String Postcode;

    @SerializedName("locationType")
    public String LocationType;

    @SerializedName("useDaylightSaveSwitching")
    public boolean UseDaylightSaveSwitching;

    @SerializedName("timeZone")
    public TimeZone TimeZone;

    @SerializedName("locationOwner")
    public LocationOwner LocationOwner;

}
