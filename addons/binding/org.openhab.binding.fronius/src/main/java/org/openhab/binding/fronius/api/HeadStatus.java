package org.openhab.binding.fronius.api;

import com.google.gson.annotations.SerializedName;

public class HeadStatus {
    @SerializedName("Code")
    private int code;
    @SerializedName("Reason")
    private String reason;
    @SerializedName("UserMessage")
    private String userMessage;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

}
