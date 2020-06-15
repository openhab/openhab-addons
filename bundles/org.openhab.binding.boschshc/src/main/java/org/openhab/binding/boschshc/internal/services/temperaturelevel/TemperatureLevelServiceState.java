package org.openhab.binding.boschshc.internal.services.temperaturelevel;

import org.openhab.binding.boschshc.internal.services.BoschSHCServiceState;

public class TemperatureLevelServiceState extends BoschSHCServiceState {

    public TemperatureLevelServiceState() {
        super("temperatureLevelState");
    }

    /**
     * Current temperature (in degree celsius)
     */
    public double temperature;
}