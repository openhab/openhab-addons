package org.openhab.binding.boschshc.internal.services.temperaturelevel;

import javax.measure.quantity.Temperature;

import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.boschshc.internal.services.BoschSHCServiceState;

import tec.uom.se.unit.Units;

public class TemperatureLevelServiceState extends BoschSHCServiceState {

    public TemperatureLevelServiceState() {
        super("temperatureLevelState");
    }

    /**
     * Current temperature (in degree celsius)
     */
    public double temperature;

    /**
     * Current temperature state to set for a thing.
     * 
     * @return Current temperature state to use for a thing.
     */
    public State getTemperatureState() {
        return new QuantityType<Temperature>(this.temperature, Units.CELSIUS);
    }
}
