package org.openhab.binding.evohome.internal.api.models.v2;

import com.google.gson.annotations.SerializedName;

public class LocationOwner {
    @SerializedName("userId")
    public int UserId;

    @SerializedName("username")
    public String Username;

    @SerializedName("firstname")
    public String FirstName;

    @SerializedName("lastname")
    public String LastName;

}
