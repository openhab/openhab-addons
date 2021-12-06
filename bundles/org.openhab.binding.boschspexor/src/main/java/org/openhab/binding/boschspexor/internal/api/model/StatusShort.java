package org.openhab.binding.boschspexor.internal.api.model;

import java.util.ArrayList;
import java.util.List;

public class StatusShort {
    private String lastConnected;
    private boolean online;
    private String version;
    private int stateOfCharge;
    private boolean updateAvailable;
    private final List<ObservationStatus> observationStatus = new ArrayList<>();

    public String getLastConnected() {
        return lastConnected;
    }

    public void setLastConnected(String lastConnected) {
        this.lastConnected = lastConnected;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getStateOfCharge() {
        return stateOfCharge;
    }

    public void setStateOfCharge(int stateOfCharge) {
        this.stateOfCharge = stateOfCharge;
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public void setUpdateAvailable(boolean updateAvailable) {
        this.updateAvailable = updateAvailable;
    }

    public List<ObservationStatus> getObservationStatus() {
        return observationStatus;
    }
}
