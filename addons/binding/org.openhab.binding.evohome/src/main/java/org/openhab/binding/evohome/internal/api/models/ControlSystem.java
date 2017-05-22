package org.openhab.binding.evohome.internal.api.models;

public interface ControlSystem {
    public int getId();
    public String getName();
    public String[] getModes();
    public String getCurrentMode();
    public void setMode(String mode);
}
