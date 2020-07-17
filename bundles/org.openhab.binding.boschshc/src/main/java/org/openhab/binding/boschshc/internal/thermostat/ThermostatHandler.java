package org.openhab.binding.boschshc.internal.thermostat;

import static org.openhab.binding.boschshc.internal.BoschSHCBindingConstants.CHANNEL_TEMPERATURE;
import static org.openhab.binding.boschshc.internal.BoschSHCBindingConstants.CHANNEL_VALVE_TAPPET_POSITION;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.boschshc.internal.BoschSHCHandler;
import org.openhab.binding.boschshc.internal.services.temperaturelevel.TemperatureLevelService;
import org.openhab.binding.boschshc.internal.services.temperaturelevel.TemperatureLevelServiceState;
import org.openhab.binding.boschshc.internal.services.valvetappet.ValveTappetService;
import org.openhab.binding.boschshc.internal.services.valvetappet.ValveTappetServiceState;

@NonNullByDefault
public final class ThermostatHandler extends BoschSHCHandler {

    public ThermostatHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();

        // Initialize services
        this.createService(TemperatureLevelService.class, this::updateChannels, Arrays.asList(CHANNEL_TEMPERATURE));
        this.createService(ValveTappetService.class, this::updateChannels,
                Arrays.asList(CHANNEL_VALVE_TAPPET_POSITION));
    }

    /**
     * Updates the channels which are linked to the {@link TemperatureLevelService} of the device.
     * 
     * @param state Current state of {@link TemperatureLevelService}.
     */
    private void updateChannels(TemperatureLevelServiceState state) {
        super.updateState(CHANNEL_TEMPERATURE, state.getTemperatureState());
    }

    /**
     * Updates the channels which are linked to the {@link ValveTappetService} of the device.
     * 
     * @param state Current state of {@link ValveTappetService}.
     */
    private void updateChannels(ValveTappetServiceState state) {
        super.updateState(CHANNEL_VALVE_TAPPET_POSITION, state.getPositionState());
    }
}