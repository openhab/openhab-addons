package org.openhab.binding.evcc.internal.handler;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

@NonNullByDefault
public class EvccSiteHandler extends EvccBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EvccSiteHandler.class);

    public EvccSiteHandler(Thing thing, ChannelTypeRegistry channelTypeRegistry) {
        super(thing, channelTypeRegistry);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        return;
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'handleCommand'");
    }

    @Override
    public void updateFromEvccState(JsonObject root) {
        if (root.has("gridConfigured")) {
            // If grid is configured, add gridPower to the root object
            // This is a workaround to avoid modifying the original JSON structure
            double gridPower = root.getAsJsonObject("grid").get("power").getAsDouble();
            root.addProperty("gridPower", gridPower);
        }
        super.updateFromEvccState(root);
    }

    @Override
    public void initialize() {
        Optional<JsonObject> stateOpt = bridgeHandler.getCachedEvccState();
        if (stateOpt.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            return;
        }

        JsonObject state = stateOpt.get();
        commonInitialize(state);
    }
}
