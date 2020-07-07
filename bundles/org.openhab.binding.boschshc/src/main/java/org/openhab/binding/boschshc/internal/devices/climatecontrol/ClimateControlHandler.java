package org.openhab.binding.boschshc.internal.devices.climatecontrol;

import static org.openhab.binding.boschshc.internal.BoschSHCBindingConstants.CHANNEL_SETPOINT_TEMPERATURE;
import static org.openhab.binding.boschshc.internal.BoschSHCBindingConstants.CHANNEL_TEMPERATURE;

import java.util.Arrays;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.boschshc.internal.BoschSHCHandler;
import org.openhab.binding.boschshc.internal.services.roomclimatecontrol.RoomClimateControlService;
import org.openhab.binding.boschshc.internal.services.roomclimatecontrol.RoomClimateControlServiceState;
import org.openhab.binding.boschshc.internal.services.temperaturelevel.TemperatureLevelService;
import org.openhab.binding.boschshc.internal.services.temperaturelevel.TemperatureLevelServiceState;

public class ClimateControlHandler extends BoschSHCHandler {
    public ClimateControlHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();

        super.createService(TemperatureLevelService.class, this::updateChannels, Arrays.asList(CHANNEL_TEMPERATURE));
        super.createService(RoomClimateControlService.class, this::updateChannels,
                Arrays.asList(CHANNEL_SETPOINT_TEMPERATURE));
    }

    protected void updateChannels(TemperatureLevelServiceState state) {
        super.updateState(CHANNEL_TEMPERATURE, new DecimalType(state.temperature));
    }

    protected void updateChannels(RoomClimateControlServiceState state) {
        super.updateState(CHANNEL_SETPOINT_TEMPERATURE, new DecimalType(state.setpointTemperature));
    }
}