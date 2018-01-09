
package org.openhab.binding.draytonwiser.internal.config;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Station {

    @SerializedName("Enabled")
    @Expose
    private Boolean enabled;
    @SerializedName("SSID")
    @Expose
    private String sSID;
    @SerializedName("SecurityMode")
    @Expose
    private String securityMode;
    @SerializedName("ScanSlotTime")
    @Expose
    private Integer scanSlotTime;
    @SerializedName("ScanSlots")
    @Expose
    private Integer scanSlots;
    @SerializedName("NetworkInterface")
    @Expose
    private NetworkInterface networkInterface;
    @SerializedName("ConnectionStatus")
    @Expose
    private String connectionStatus;
    @SerializedName("DhcpStatus")
    @Expose
    private DhcpStatus dhcpStatus;
    @SerializedName("Scanning")
    @Expose
    private Boolean scanning;
    @SerializedName("DetectedAccessPoints")
    @Expose
    private List<DetectedAccessPoint> detectedAccessPoints = null;
    @SerializedName("ConnectionFailures")
    @Expose
    private Integer connectionFailures;
    @SerializedName("MdnsHostname")
    @Expose
    private String mdnsHostname;
    @SerializedName("MacAddress")
    @Expose
    private String macAddress;
    @SerializedName("RSSI")
    @Expose
    private RSSI rSSI;
    @SerializedName("Channel")
    @Expose
    private Integer channel;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getSSID() {
        return sSID;
    }

    public void setSSID(String sSID) {
        this.sSID = sSID;
    }

    public String getSecurityMode() {
        return securityMode;
    }

    public void setSecurityMode(String securityMode) {
        this.securityMode = securityMode;
    }

    public Integer getScanSlotTime() {
        return scanSlotTime;
    }

    public void setScanSlotTime(Integer scanSlotTime) {
        this.scanSlotTime = scanSlotTime;
    }

    public Integer getScanSlots() {
        return scanSlots;
    }

    public void setScanSlots(Integer scanSlots) {
        this.scanSlots = scanSlots;
    }

    public NetworkInterface getNetworkInterface() {
        return networkInterface;
    }

    public void setNetworkInterface(NetworkInterface networkInterface) {
        this.networkInterface = networkInterface;
    }

    public String getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(String connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public DhcpStatus getDhcpStatus() {
        return dhcpStatus;
    }

    public void setDhcpStatus(DhcpStatus dhcpStatus) {
        this.dhcpStatus = dhcpStatus;
    }

    public Boolean getScanning() {
        return scanning;
    }

    public void setScanning(Boolean scanning) {
        this.scanning = scanning;
    }

    public List<DetectedAccessPoint> getDetectedAccessPoints() {
        return detectedAccessPoints;
    }

    public void setDetectedAccessPoints(List<DetectedAccessPoint> detectedAccessPoints) {
        this.detectedAccessPoints = detectedAccessPoints;
    }

    public Integer getConnectionFailures() {
        return connectionFailures;
    }

    public void setConnectionFailures(Integer connectionFailures) {
        this.connectionFailures = connectionFailures;
    }

    public String getMdnsHostname() {
        return mdnsHostname;
    }

    public void setMdnsHostname(String mdnsHostname) {
        this.mdnsHostname = mdnsHostname;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public RSSI getRSSI() {
        return rSSI;
    }

    public void setRSSI(RSSI rSSI) {
        this.rSSI = rSSI;
    }

    public Integer getChannel() {
        return channel;
    }

    public void setChannel(Integer channel) {
        this.channel = channel;
    }

}
