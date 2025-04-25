package org.smslib;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.smslib.callback.IDeviceInformationListener;

/**
 * Extracted from SMSLib
 */
@NonNullByDefault
public class DeviceInformation {

    @Nullable
    private IDeviceInformationListener deviceInformationListener;

    public enum Modes {
        PDU,
        TEXT
    }

    String manufacturer = "N/A";
    String model = "N/A";
    String swVersion = "N/A";
    String serialNo = "N/A";
    String imsi = "N/A";
    int rssi = 0;

    @Nullable
    Modes mode;

    int totalSent = 0;
    int totalFailed = 0;
    int totalReceived = 0;
    int totalFailures = 0;

    public void setDeviceInformationListener(@Nullable IDeviceInformationListener deviceInformationListener) {
        this.deviceInformationListener = deviceInformationListener;
    }

    public synchronized void increaseTotalSent() {
        this.totalSent++;
        IDeviceInformationListener dil = deviceInformationListener;
        if (dil != null) {
            dil.setTotalSent(Integer.toString(totalSent));
        }
    }

    public synchronized void increaseTotalFailed() {
        this.totalFailed++;
        IDeviceInformationListener dil = deviceInformationListener;
        if (dil != null) {
            dil.setTotalFailed(Integer.toString(totalFailed));
        }
    }

    public synchronized void increaseTotalReceived() {
        this.totalReceived++;
        IDeviceInformationListener dil = deviceInformationListener;
        if (dil != null) {
            dil.setTotalReceived(Integer.toString(totalReceived));
        }
    }

    public synchronized void increaseTotalFailures() {
        this.totalFailures++;
        IDeviceInformationListener dil = deviceInformationListener;
        if (dil != null) {
            dil.setTotalFailures(Integer.toString(totalFailures));
        }
    }

    public String getManufacturer() {
        return this.manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
        IDeviceInformationListener finalDeviceInformationListener = deviceInformationListener;
        if (finalDeviceInformationListener != null) {
            finalDeviceInformationListener.setManufacturer(manufacturer);
        }
    }

    public String getModel() {
        return this.model;
    }

    public void setModel(String model) {
        this.model = model;
        IDeviceInformationListener finalDeviceInformationListener = deviceInformationListener;
        if (finalDeviceInformationListener != null) {
            finalDeviceInformationListener.setModel(model);
        }
    }

    public String getSwVersion() {
        return this.swVersion;
    }

    public void setSwVersion(String swVersion) {
        this.swVersion = swVersion;
        IDeviceInformationListener finalDeviceInformationListener = deviceInformationListener;
        if (finalDeviceInformationListener != null) {
            finalDeviceInformationListener.setSwVersion(swVersion);
        }
    }

    public String getSerialNo() {
        return this.serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
        IDeviceInformationListener finalDeviceInformationListener = deviceInformationListener;
        if (finalDeviceInformationListener != null) {
            finalDeviceInformationListener.setSerialNo(serialNo);
        }
    }

    public String getImsi() {
        return this.imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
        IDeviceInformationListener finalDeviceInformationListener = deviceInformationListener;
        if (finalDeviceInformationListener != null) {
            finalDeviceInformationListener.setImsi(imsi);
        }
    }

    public int getRssi() {
        return this.rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
        IDeviceInformationListener finalDeviceInformationListener = deviceInformationListener;
        if (finalDeviceInformationListener != null) {
            finalDeviceInformationListener.setRssi(Integer.toString(rssi));
        }
    }

    public @Nullable Modes getMode() {
        return this.mode;
    }

    public void setMode(Modes mode) {
        this.mode = mode;
        IDeviceInformationListener finalDeviceInformationListener = deviceInformationListener;
        if (finalDeviceInformationListener != null) {
            finalDeviceInformationListener.setMode(mode.toString());
        }
    }

    @Override
    public String toString() {
        return String.format("MANUF:%s, MODEL:%s, SERNO:%s, IMSI:%s, SW:%s, RSSI:%ddBm, MODE:%s", getManufacturer(),
                getModel(), getSerialNo(), getImsi(), getSwVersion(), getRssi(), getMode());
    }
}
