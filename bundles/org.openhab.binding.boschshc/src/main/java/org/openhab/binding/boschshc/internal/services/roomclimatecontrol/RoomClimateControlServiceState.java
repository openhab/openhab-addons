package org.openhab.binding.boschshc.internal.services.roomclimatecontrol;

import org.openhab.binding.boschshc.internal.services.BoschSHCServiceState;

public class RoomClimateControlServiceState extends BoschSHCServiceState {

    public RoomClimateControlServiceState() {
        super("climateControlState");
    }

    /**
     * Desired temperature (in degree celsius).
     * 
     * @apiNote Min: 5.0, Max: 30.0.
     * @apiNote Can be set in 0.5 steps.
     */
    public Double setpointTemperature;
}