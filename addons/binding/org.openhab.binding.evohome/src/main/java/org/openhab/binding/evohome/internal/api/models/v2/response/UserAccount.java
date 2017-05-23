package org.openhab.binding.evohome.internal.api.models.v2.response;

import com.google.gson.annotations.SerializedName;

public class UserAccount {

    @SerializedName("userId")
    public int UserId;

    @SerializedName("username")
    public String UserName;

    @SerializedName("firstname")
    public String FirstName;

    @SerializedName("lastname")
    public String LastName;

    @SerializedName("streetAddress")
    public String StreetAddress;

    @SerializedName("city")
    public String City;

    @SerializedName("postcode")
    public String PostCode;

    @SerializedName("country")
    public String Country;

    @SerializedName("language")
    public String Language;

}
