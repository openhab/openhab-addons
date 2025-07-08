package org.openhab.binding.evcc.internal.handler;

import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.Command;

import com.google.gson.JsonObject;

public class EvccLoadpointHandler extends BaseThingHandler implements EvccJsonAwareHandler {

    private final ChannelTypeRegistry channelTypeRegistry;
    private EvccBridgeHandler bridgeHandler;

    public EvccLoadpointHandler(Thing thing, ChannelTypeRegistry channelTypeRegistry) {
        super(thing);
        this.channelTypeRegistry = channelTypeRegistry;
    }

    @Override
    public void initialize() {
        if (getBridge() != null && getBridge().getHandler() instanceof EvccBridgeHandler bridge) {
            this.bridgeHandler = bridge;
            this.bridgeHandler.register(this);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        }
    }

    @Override
    public void dispose() {
        if (bridgeHandler != null) {
            bridgeHandler.unregister(this);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleCommand'");
    }

    @Override
    public void updateFromEvccState(JsonObject root) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateFromEvccState'");
    }
}
