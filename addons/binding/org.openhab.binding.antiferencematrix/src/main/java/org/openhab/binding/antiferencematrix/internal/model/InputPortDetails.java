package org.openhab.binding.antiferencematrix.internal.model;

public class InputPortDetails extends PortDetail {

    private int edidProfile;
    private String logicalAddress;
    private int[] transmissionNodes;

    public int getEdidProfile() {
        return edidProfile;
    }

    public String getLogicalAddress() {
        return logicalAddress;
    }

    public int[] getTransmissionNodes() {
        return transmissionNodes;
    }

}
