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
        this.temperatureLevelService = new TemperatureLevelService(bridgeHandler);
        this.valveTappetService = new ValveTappetService(bridgeHandler);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            TemperatureLevelServiceState temperatureLevelState = this.temperatureLevelService
                    .getState(this.getBoschID());
            if (temperatureLevelState != null) {
                this.updateChannels(temperatureLevelState);
            }
            ValveTappetServiceState valveTappetServiceState = this.valveTappetService.getState(this.getBoschID());
            if (valveTappetServiceState != null) {
                this.updateChannels(valveTappetServiceState);
            }
        }
    }

    @Override
    public void processUpdate(String id, JsonElement state) {
        try {
            Gson gson = new Gson();
            // TODO: Make sure that provided state is really of specific service state
            // before doing cast (type should be checked)
            updateChannels(gson.fromJson(state, TemperatureLevelServiceState.class));
        } catch (JsonSyntaxException e) {
            logger.warn("Received unknown update in Thermostat: {}", state);
        }
    }

    private void updateChannels(TemperatureLevelServiceState state) {
        super.updateState(CHANNEL_TEMPERATURE, new DecimalType(state.temperature));
    }

    private void updateChannels(ValveTappetServiceState state) {
        super.updateState(CHANNEL_VALVE_TAPPET_POSITION, new DecimalType(state.position));
    }
}