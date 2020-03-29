package org.openhab.binding.boschshc.internal;

import static org.openhab.binding.boschshc.internal.BoschSHCBindingConstants.CHANNEL_POWER_SWITCH;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

/**
 * Represents Bosch in-wall switches.
 *
 * @author Stefan Kästle
 */
public class BoschInWallSwitchHandler extends BoschSHCHandler {

    private final Logger logger = LoggerFactory.getLogger(BoschSHCHandler.class);

    public BoschInWallSwitchHandler(Thing thing) {
        super(thing);
        logger.warn("Creating in-wall: {}", thing.getLabel());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        BoschSHCConfiguration config = super.getBoschConfig();
        Bridge bridge = this.getBridge();

        if (bridge != null && config != null) {

            logger.info("Handle command for: {} - {}", config.id, command);
            BoschSHCBridgeHandler bridgeHandler = (BoschSHCBridgeHandler) bridge.getHandler();

            if (bridgeHandler != null) {

                if (CHANNEL_POWER_SWITCH.equals(channelUID.getId())) {
                    if (command instanceof RefreshType) {
                        PowerSwitchState state = bridgeHandler.refreshSwitchState(getThing());

                        if (state != null) {

                            State powerState = OnOffType.from(state.switchState);
                            updateState(channelUID, powerState);
                        }
                    }

                    else {
                        bridgeHandler.updateSwitchState(getThing(), command.toFullString());
                    }
                }
            }
        } else {

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Bridge or config is NUL");
        }

    }

    @Override
    public void processUpdate(JsonElement state) {
        logger.warn("in-wall switch: received update: {}", state);

        Gson gson = new Gson();

        try {
            PowerSwitchState parsed = gson.fromJson(state, PowerSwitchState.class);

            // Update power switch
            logger.warn("Parsed switch state of {}: {}", this.getBoschID(), parsed.switchState);
            State powerState = OnOffType.from(parsed.switchState);
            updateState(CHANNEL_POWER_SWITCH, powerState);

        } catch (JsonSyntaxException e) {
            logger.warn("Received unknown update in in-wall switch: {}", state);
        }
    }

}
