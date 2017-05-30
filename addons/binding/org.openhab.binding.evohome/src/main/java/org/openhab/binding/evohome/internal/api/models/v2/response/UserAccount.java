package org.openhab.binding.evohome.internal.api.models.v2.response;

import com.google.gson.annotations.SerializedName;

public class UserAccount {

    @SerializedName("userId")
    public int userId;

    @SerializedName("username")
    public String userName;

    @SerializedName("firstname")
    public String firstName;

    @SerializedName("lastname")
    public String lastName;

    @SerializedName("streetAddress")
    public String streetAddress;

    @SerializedName("city")
    public String city;

    @SerializedName("postcode")
    public String postCode;

    @SerializedName("country")
    public String country;

    @SerializedName("language")
    public String language;

}
