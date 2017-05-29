package org.openhab.binding.evohome.internal.api.models;

import org.openhab.binding.evohome.internal.api.models.v2.response.TemperatureControlSystem;

public interface ControlSystem {
    public int getId();
    public String getName();
    public String[] getModes();
    public String getCurrentMode();
    public void setMode(String mode);
    TemperatureControlSystem getHeatingZones();
}
