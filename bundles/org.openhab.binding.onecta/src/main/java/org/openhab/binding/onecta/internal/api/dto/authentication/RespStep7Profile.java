package org.openhab.binding.onecta.internal.api.dto.authentication;

import com.google.gson.annotations.SerializedName;

public class RespStep7Profile {
    @SerializedName("firstName")
    public String firstName;
    @SerializedName("lastName")
    public String lastName;
    @SerializedName("country")
    public String country;
    @SerializedName("email")
    public String email;

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getCountry() {
        return country;
    }

    public String getEmail() {
        return email;
    }
}
