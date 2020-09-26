package org.openhab.binding.dbquery.internal;

import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;

public class JDBCBridgeHandler extends BaseBridgeHandler {
    public JDBCBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }
}
