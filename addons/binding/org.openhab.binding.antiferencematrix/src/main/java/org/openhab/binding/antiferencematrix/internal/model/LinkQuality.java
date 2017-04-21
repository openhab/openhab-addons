package org.openhab.binding.antiferencematrix.internal.model;

import com.google.gson.annotations.SerializedName;

public class LinkQuality {

    private int channelA;
    private int channelB;
    private int channelC;
    private int channelD;
    private int errorsChannelA;
    private int errorsChannelB;
    private int errorsChannelC;
    private int errorsChannelD;
    @SerializedName("BER")
    private int ber;
    private int status;

    public int getChannelA() {
        return channelA;
    }

    public int getChannelB() {
        return channelB;
    }

    public int getChannelC() {
        return channelC;
    }

    public int getChannelD() {
        return channelD;
    }

    public int getErrorsChannelA() {
        return errorsChannelA;
    }

    public int getErrorsChannelB() {
        return errorsChannelB;
    }

    public int getErrorsChannelC() {
        return errorsChannelC;
    }

    public int getErrorsChannelD() {
        return errorsChannelD;
    }

    public int getBer() {
        return ber;
    }

    public int getStatus() {
        return status;
    }

}
