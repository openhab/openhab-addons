package org.openhab.binding.evcc.internal.handler;

import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.Command;

import com.google.gson.JsonObject;

@NonNullByDefault
public class EvccHeatingHandler extends EvccBaseThingHandler {

    private final int index;

    public EvccHeatingHandler(Thing thing, ChannelTypeRegistry channelTypeRegistry) {
        super(thing, channelTypeRegistry);
        Map<String, String> props = thing.getProperties();
        String indexString = props.getOrDefault("index", "0");
        index = Integer.parseInt(indexString);
    }

    @Override
    public void initialize() {
        Bridge bridge = getBridge();
        if (bridge == null)
            return;

        bridgeHandler = bridge.getHandler() instanceof EvccBridgeHandler ? (EvccBridgeHandler) bridge.getHandler()
                : null;
        if (bridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            return;
        }

        Optional<JsonObject> stateOpt = bridgeHandler.getCachedEvccState();
        if (stateOpt.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            return;
        }

        JsonObject state = stateOpt.get().getAsJsonArray("loadpoints").get(index).getAsJsonObject();
        commonInitialize(state);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleCommand'");
    }

    @Override
    public void updateFromEvccState(JsonObject root) {
        root = root.getAsJsonArray("loadpoints").get(index).getAsJsonObject();
        super.updateFromEvccState(root);
    }
}
