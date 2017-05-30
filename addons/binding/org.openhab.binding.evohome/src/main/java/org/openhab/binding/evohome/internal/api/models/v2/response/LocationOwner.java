package org.openhab.binding.evohome.internal.api.models.v2.response;

import com.google.gson.annotations.SerializedName;

public class LocationOwner {

    @SerializedName("userId")
    public int userId;

    @SerializedName("username")
    public String username;

    @SerializedName("firstname")
    public String firstName;

    @SerializedName("lastname")
    public String lastName;

}
