package org.openhab.binding.antiferencematrix.internal.model;

import com.google.gson.annotations.SerializedName;

public abstract class PortDetail extends Response {

    private String statusMessage;
    private int bay;
    private String mode;
    private String type;
    private int status;
    private String name;

    @SerializedName("DPS")
    private int dps;

    @SerializedName("HPD")
    private int hpd;
    private boolean hasSignal;

    public String getStatusMessage() {
        return statusMessage;
    }

    public int getBay() {
        return bay;
    }

    public String getMode() {
        return mode;
    }

    public String getType() {
        return type;
    }

    public int getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public int getDps() {
        return dps;
    }

    public int getHpd() {
        return hpd;
    }

    public boolean getHasSignal() {
        return hasSignal;
    }

}
