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

                        // Refresh the temperature from the Bosch Twinguard device.
                        // Might not be necessary, can just wait until we get one
                        logger.warn("Refreshing the temperature is not yet supported.");
                    }
                    // Otherwise: not action supported here.
                }
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Bridge or config is NUL");
        }
    }

    @Override
    public void processUpdate(String id, @NonNull JsonElement state) {
        logger.warn("Motion detector: received update: {} {}", id, state);
        Gson gson = new Gson();

        try {
            LatestMotionState parsed = gson.fromJson(state, LatestMotionState.class);

            DateTimeType date = new DateTimeType(parsed.latestMotionDetected);
            logger.warn("Parsed date of latest motion to {}: {} as date {}", this.getBoschID(), parsed, date);
            updateState(CHANNEL_LATEST_MOTION, date);

        } catch (JsonSyntaxException e) {
            logger.warn("Received unknown update in in-wall switch: {}", state);
        }
    }
}
