package org.openhab.binding.boschshc.internal.thermostat;

import static org.openhab.binding.boschshc.internal.BoschSHCBindingConstants.*;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.boschshc.internal.BoschSHCBridgeHandler;
import org.openhab.binding.boschshc.internal.BoschSHCHandler;
import org.openhab.binding.boschshc.internal.services.temperaturelevel.TemperatureLevelService;
import org.openhab.binding.boschshc.internal.services.temperaturelevel.TemperatureLevelServiceState;

public class ThermostatHandler extends BoschSHCHandler {

    private TemperatureLevelService temperatureLevelService;

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
        this.temperatureLevelService = new TemperatureLevelService(bridgeHandler);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            TemperatureLevelServiceState temperaturelevelState = this.temperatureLevelService
                    .getState(this.getBoschID());
            if (temperaturelevelState != null) {
                this.updateChannels(temperaturelevelState);
            }
        }
    }

    @Override
    public void processUpdate(String id, JsonElement state) {
        try {
            Gson gson = new Gson();
            updateChannels(gson.fromJson(state, TemperatureLevelServiceState.class));
        } catch (JsonSyntaxException e) {
            logger.warn("Received unknown update in Thermostat: {}", state);
        }
    }

    private void updateChannels(TemperatureLevelServiceState state) {
        super.updateState(CHANNEL_TEMPERATURE, new DecimalType(state.temperature));
    }
}