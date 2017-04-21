package org.openhab.binding.antiferencematrix.internal.model;

import com.google.gson.annotations.SerializedName;

public class OutputPortDetails extends PortDetail {

    private int receiveFrom;
    private int sinkPowerStatus;
    private String physicalAddress;
    private String vendor;
    private String model;
    private String monitorName;
    private String serial;
    private String manufactured;
    @SerializedName("EDIDRead")
    private boolean edidRead;
    private String logicalAddresses;
    private String linkStatus;
    private String firmwareVersion;
    private boolean firmwareVersionAvailable;
    private int estimatedCableLength;
    @SerializedName("TMDS")
    private int tmds;

    private LinkQuality linkQuality;

    public int getReceiveFrom() {
        return receiveFrom;
    }

    public int getSinkPowerStatus() {
        return sinkPowerStatus;
    }

    public String getPhysicalAddress() {
        return physicalAddress;
    }

    public String getVendor() {
        return vendor;
    }

    public String getModel() {
        return model;
    }

    public String getMonitorName() {
        return monitorName;
    }

    public String getSerial() {
        return serial;
    }

    public String getManufactured() {
        return manufactured;
    }

    public boolean getEdidRead() {
        return edidRead;
    }

    public String getLogicalAddresses() {
        return logicalAddresses;
    }

    public String getLinkStatus() {
        return linkStatus;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public boolean getFirmwareVersionAvailable() {
        return firmwareVersionAvailable;
    }

    public int getEstimatedCableLength() {
        return estimatedCableLength;
    }

    public int getTmds() {
        return tmds;
    }

    public LinkQuality getLinkQuality() {
        return linkQuality;
    }

}
