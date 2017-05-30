package org.openhab.binding.evohome.internal.api.models.v1;

import com.google.gson.annotations.SerializedName;

public class UserInfo {

    @SerializedName("userID")
    private String userId;

    private String username;

    private String firstname;

    private String lastname;

    @Override
    public String toString() {
        return "userId[" + userId + "] username[" + username + "] firstname[" + firstname + "] lastname[" + lastname
                + "]";
    }

    public String getUserId() {
        return userId;
    }

}
