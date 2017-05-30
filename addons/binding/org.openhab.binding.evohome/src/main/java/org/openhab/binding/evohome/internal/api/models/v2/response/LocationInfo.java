package org.openhab.binding.evohome.internal.api.models.v2.response;

import com.google.gson.annotations.SerializedName;

public class LocationInfo {

    @SerializedName("locationId")
    public int locationId;

    @SerializedName("name")
    public String name;

    @SerializedName("streetAddress")
    public String streetAddress;

    @SerializedName("city")
    public String city;

    @SerializedName("country")
    public String country;

    @SerializedName("postcode")
    public String postcode;

    @SerializedName("locationType")
    public String locationType;

    @SerializedName("useDaylightSaveSwitching")
    public boolean useDaylightSaveSwitching;

    @SerializedName("timeZone")
    public TimeZone timeZone;

    @SerializedName("locationOwner")
    public LocationOwner locationOwner;

}
