package org.openhab.binding.fronius.api;

import com.google.gson.annotations.SerializedName;

public class Head {
    @SerializedName("RequestArguments")
    private HeadRequestArguments requestArguments;
    @SerializedName("Status")
    private HeadStatus status;
    @SerializedName("Timestamp")
    private String timestamp;

    public HeadRequestArguments getRequestArguments() {
        return requestArguments;
    }

    public void setRequestArguments(HeadRequestArguments requestArguments) {
        this.requestArguments = requestArguments;
    }

    public HeadStatus getStatus() {
        return status;
    }

    public void setStatus(HeadStatus status) {
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

}
