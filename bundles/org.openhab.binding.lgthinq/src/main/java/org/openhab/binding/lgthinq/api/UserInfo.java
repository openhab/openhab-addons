package org.openhab.binding.lgthinq.api;

import java.io.Serializable;

public class UserInfo  implements Serializable {

    private String userNumber;
    private String userID;
    private String userIDType;
    private String displayUserID;

    public UserInfo() {}

    public UserInfo(String userNumber, String userID, String userIDType, String displayUserId) {
        this.userNumber = userNumber;
        this.userID = userID;
        this.userIDType = userIDType;
        this.displayUserID = displayUserId;
    }

    public String getUserNumber() {
        return userNumber;
    }

    public void setUserNumber(String userNumber) {
        this.userNumber = userNumber;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserIDType() {
        return userIDType;
    }

    public void setUserIDType(String userIDType) {
        this.userIDType = userIDType;
    }

    public String getDisplayUserID() {
        return displayUserID;
    }

    public void setDisplayUserID(String displayUserID) {
        this.displayUserID = displayUserID;
    }
}
