package org.openhab.binding.antiferencematrix.internal.model;

import com.google.gson.annotations.SerializedName;

public class SystemDetails extends Response {

    private String model;
    private String version;
    private String serial;

    @SerializedName("MAC")
    private String mac;

    private int boardRev;
    private String statusMessage;
    private int status;

    public String getModel() {
        return model;
    }

    public String getVersion() {
        return version;
    }

    public String getSerial() {
        return serial;
    }

    public String getMac() {
        return mac;
    }

    public int getBoardRev() {
        return boardRev;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public int getStatus() {
        return status;
    }

}
