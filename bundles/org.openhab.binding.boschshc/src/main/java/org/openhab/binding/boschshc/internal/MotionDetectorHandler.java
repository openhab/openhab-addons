package org.openhab.binding.boschshc.internal;

import static org.openhab.binding.boschshc.internal.BoschSHCBindingConstants.CHANNEL_LATEST_MOTION;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

public class MotionDetectorHandler extends BoschSHCHandler {
    private final Logger logger = LoggerFactory.getLogger(BoschSHCHandler.class);

    public MotionDetectorHandler(Thing thing) {
        super(thing);
        logger.warn("Creating motion detector thing: {}", thing.getLabel());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        BoschSHCConfiguration config = super.getBoschConfig();
        Bridge bridge = this.getBridge();

        if (bridge != null && config != null) {

            logger.info("Handle command for: {} - {}", config.id, command);
            BoschSHCBridgeHandler bridgeHandler = (BoschSHCBridgeHandler) bridge.getHandler();

            if (bridgeHandler != null) {

                if (CHANNEL_LATEST_MOTION.equals(channelUID.getId())) {
                    if (command instanceof RefreshType) {

                        updateLatestMotionState(
                                bridgeHandler.refreshState(getThing(), "LatestMotion", LatestMotionState.class));
                    }
                    // Otherwise: not action supported here.
                }
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Bridge or config is NUL");
        }
    }

    void updateLatestMotionState(LatestMotionState state) {
        DateTimeType date = new DateTimeType(state.latestMotionDetected);
        logger.debug("Parsed date of latest motion to {}: {} as date {}", this.getBoschID(), state, date);
        updateState(CHANNEL_LATEST_MOTION, date);
    }

    @Override
    public void processUpdate(String id, @NonNull JsonElement state) {
        logger.debug("Motion detector: received update: {} {}", id, state);

        try {
            Gson gson = new Gson();
            updateLatestMotionState(gson.fromJson(state, LatestMotionState.class));
        } catch (JsonSyntaxException e) {
            logger.warn("Received unknown update in in-wall switch: {}", state);
        }
    }
}
