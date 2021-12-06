package org.openhab.binding.boschspexor.internal.api.model;

public class Firmware {

    public enum FirmwareState {
        UpToDate,
        UpdateAvailable,
        Scheduled,
        Installing,
        InstallationFailed,
    }

    private String currentVersion;
    private FirmwareState state;
    private String availableVersion;

    public String getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    public FirmwareState getState() {
        return state;
    }

    public void setState(FirmwareState state) {
        this.state = state;
    }

    public String getAvailableVersion() {
        return availableVersion;
    }

    public void setAvailableVersion(String availableVersion) {
        this.availableVersion = availableVersion;
    }
}
