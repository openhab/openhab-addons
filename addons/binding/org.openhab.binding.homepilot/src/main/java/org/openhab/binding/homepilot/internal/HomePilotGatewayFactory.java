package org.openhab.binding.homepilot.internal;

import org.openhab.binding.homepilot.handler.HomePilotBridgeHandler;

public class HomePilotGatewayFactory {

    public static HomePilotGateway createGateway(String id, HomePilotConfig config,
            HomePilotBridgeHandler homePilotBridgeHandler) {
        return new DefaultHttpGateway(id, config);
    }
}
