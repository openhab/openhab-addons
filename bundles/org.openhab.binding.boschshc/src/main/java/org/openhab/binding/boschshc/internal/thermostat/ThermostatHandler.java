package org.openhab.binding.boschshc.internal.thermostat;

import static org.openhab.binding.boschshc.internal.BoschSHCBindingConstants.*;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.boschshc.internal.BoschSHCBridgeHandler;
import org.openhab.binding.boschshc.internal.BoschSHCHandler;
import org.openhab.binding.boschshc.internal.services.temperaturelevel.TemperatureLevelService;
import org.openhab.binding.boschshc.internal.services.temperaturelevel.TemperatureLevelServiceState;
import org.openhab.binding.boschshc.internal.services.valvetappet.ValveTappetService;
import org.openhab.binding.boschshc.internal.services.valvetappet.ValveTappetServiceState;

public class ThermostatHandler extends BoschSHCHandler {

    private TemperatureLevelService temperatureLevelService;

    private ValveTappetService valveTappetService;

    public ThermostatHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();

        BoschSHCBridgeHandler bridgeHandler = this.getBridgeHandler();
        if (bridgeHandler == null) {
            throw new Error(String.format("Could not initialize {}, no valid bridge set", this.getThing()));
        }

        // Initialize services
        String deviceId = this.getBoschID();
        this.temperatureLevelService = new TemperatureLevelService();
        this.temperatureLevelService.initialize(bridgeHandler, deviceId, this::updateChannels);
        this.registerService(this.temperatureLevelService);
        this.valveTappetService = new ValveTappetService();
        this.valveTappetService.initialize(bridgeHandler, deviceId, this::updateChannels);
        this.registerService(this.valveTappetService);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            this.temperatureLevelService.refreshState();
            this.valveTappetService.refreshState();
        }
    }

    protected void updateChannels(TemperatureLevelServiceState state) {
        super.updateState(CHANNEL_TEMPERATURE, new DecimalType(state.temperature));
    }

    private void updateChannels(ValveTappetServiceState state) {
        super.updateState(CHANNEL_VALVE_TAPPET_POSITION, new DecimalType(state.position));
    }
}