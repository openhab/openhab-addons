package org.openhab.binding.boschshc.internal.devices.climatecontrol;

import static org.openhab.binding.boschshc.internal.BoschSHCBindingConstants.CHANNEL_SETPOINT_TEMPERATURE;
import static org.openhab.binding.boschshc.internal.BoschSHCBindingConstants.CHANNEL_TEMPERATURE;

import java.util.Arrays;

import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.boschshc.internal.BoschSHCHandler;
import org.openhab.binding.boschshc.internal.services.roomclimatecontrol.RoomClimateControlService;
import org.openhab.binding.boschshc.internal.services.roomclimatecontrol.RoomClimateControlServiceState;
import org.openhab.binding.boschshc.internal.services.temperaturelevel.TemperatureLevelService;
import org.openhab.binding.boschshc.internal.services.temperaturelevel.TemperatureLevelServiceState;

import tec.uom.se.unit.Units;

public class ClimateControlHandler extends BoschSHCHandler {

    private RoomClimateControlService roomClimateControlService;

    public ClimateControlHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();

        super.createService(TemperatureLevelService.class, this::updateChannels, Arrays.asList(CHANNEL_TEMPERATURE));
        this.roomClimateControlService = super.createService(RoomClimateControlService.class, this::updateChannels,
                Arrays.asList(CHANNEL_SETPOINT_TEMPERATURE));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        switch (channelUID.getId()) {
            case CHANNEL_SETPOINT_TEMPERATURE:
                if (command instanceof QuantityType<?>) {
                    // Set specific temperature
                    QuantityType<?> quantityType = (QuantityType<?>) command;
                    double setpointTemperature = quantityType.toUnit(Units.CELSIUS).doubleValue();
                    this.roomClimateControlService.setState(new RoomClimateControlServiceState(setpointTemperature));
                }
                break;
        }
    }

    protected void updateChannels(TemperatureLevelServiceState state) {
        super.updateState(CHANNEL_TEMPERATURE, state.getTemperatureState());
    }

    protected void updateChannels(RoomClimateControlServiceState state) {
        super.updateState(CHANNEL_SETPOINT_TEMPERATURE, state.getSetpointTemperatureState());
    }
}