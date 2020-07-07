package org.openhab.binding.boschshc.internal.services.roomclimatecontrol;

import javax.measure.quantity.Temperature;

import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.boschshc.internal.services.BoschSHCServiceState;

import tec.uom.se.unit.Units;

public class RoomClimateControlServiceState extends BoschSHCServiceState {

    private static final String Type = "climateControlState";

    public RoomClimateControlServiceState() {
        super(Type);
    }

    public RoomClimateControlServiceState(double setpointTemperature) {
        super(Type);
        this.setpointTemperature = setpointTemperature;
    }

    /**
     * Desired temperature (in degree celsius).
     * 
     * @apiNote Min: 5.0, Max: 30.0.
     * @apiNote Can be set in 0.5 steps.
     */
    public double setpointTemperature;

    /**
     * Desired temperature state to set for a thing.
     * 
     * @return Desired temperature state to set for a thing.
     */
    public State getSetpointTemperatureState() {
        return new QuantityType<Temperature>(this.setpointTemperature, Units.CELSIUS);
    }
}