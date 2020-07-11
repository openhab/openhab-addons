package org.openhab.binding.boschshc.internal.devices.climatecontrol;

import static org.openhab.binding.boschshc.internal.BoschSHCBindingConstants.CHANNEL_SETPOINT_TEMPERATURE;
import static org.openhab.binding.boschshc.internal.BoschSHCBindingConstants.CHANNEL_TEMPERATURE;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
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

/**
 * A virtual device which controls up to six Bosch Smart Home radiator thermostats in a room.
 */
@NonNullByDefault
public class ClimateControlHandler extends BoschSHCHandler {

    @NonNullByDefault({})
    private RoomClimateControlService roomClimateControlService;

    /**
     * Constructor.
     * 
     * @param thing The Bosch Smart Home device that should be handled.
     */
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
                    updateSetpointTemperature((QuantityType<?>) command);
                }
                break;
        }
    }

    private void updateChannels(TemperatureLevelServiceState state) {
        super.updateState(CHANNEL_TEMPERATURE, state.getTemperatureState());
    }

    private void updateChannels(RoomClimateControlServiceState state) {
        super.updateState(CHANNEL_SETPOINT_TEMPERATURE, state.getSetpointTemperatureState());
    }

    private void updateSetpointTemperature(QuantityType<?> quantityType) {
        QuantityType<?> celsiusType = quantityType.toUnit(Units.CELSIUS);
        if (celsiusType == null) {
            logger.debug("Could not convert quantity command to celsius");
            return;
        }

        if (this.roomClimateControlService != null) {
            logger.debug("RoomClimateControlService not initialized");
            return;
        }

        double setpointTemperature = celsiusType.doubleValue();
        this.roomClimateControlService.setState(new RoomClimateControlServiceState(setpointTemperature));
    }
}