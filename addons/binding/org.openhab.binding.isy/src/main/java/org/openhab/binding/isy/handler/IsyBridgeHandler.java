package org.openhab.binding.isy.handler;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.isy.config.IsyBridgeConfiguration;
import org.openhab.binding.isy.discovery.IsyDiscoveryService;
import org.openhab.binding.isy.internal.ISYModelChangeListener;
import org.openhab.binding.isy.internal.InsteonAddress;
import org.openhab.binding.isy.internal.InsteonClient;
import org.openhab.binding.isy.internal.InsteonClientProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.universaldevices.device.model.UDControl;
import com.universaldevices.device.model.UDNode;

public class IsyBridgeHandler extends BaseBridgeHandler implements InsteonClientProvider {
    private Logger logger = LoggerFactory.getLogger(IsyBridgeHandler.class);

    InsteonClient insteonClient;

    @Override
    public InsteonClient getInsteonClient() {
        return insteonClient;
    }

    private IsyDiscoveryService bridgeDiscoveryService;

    public IsyBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("isy bridge handler called");
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub
        super.dispose();
    }

    @Override
    public void initialize() {
        // super.initialize();
        logger.debug("initialize called for bridge handler");
        IsyBridgeConfiguration config = getThing().getConfiguration().as(IsyBridgeConfiguration.class);
        insteonClient = new InsteonClient(config.getUser(), config.getPassword(), new ISYModelChangeListener() {

            @Override
            public void onModelChanged(UDControl control, Object action, UDNode node) {
                // logger.debug("isy model changed event: control: {}, action: {}, node: {}", control.name, action,
                // node);
                IsyHandler handler = getThingHandler(node.address);
                if (handler != null) {
                    logger.debug("  we have handler");
                    handler.handleUpdate(control.name, action, node.address);
                }
            }

            @Override
            public void onDeviceOnLine() {
                updateStatus(ThingStatus.ONLINE);
            }

            @Override
            public void onDeviceOffLine() {
                updateStatus(ThingStatus.OFFLINE);

            }
        });
        logger.debug("starting insteon client");
        if (config.getUuid() != null) {
            try {
                this.insteonClient.start("uuid:" + config.getUuid(), config.getIpAddress());
            } catch (Exception e) {
                logger.error("error connecting", e);
                e.printStackTrace();
            }
        } else {
            this.insteonClient.start();
        }
    }

    public void registerDiscoveryService(IsyDiscoveryService isyBridgeDiscoveryService) {
        this.bridgeDiscoveryService = isyBridgeDiscoveryService;

    }

    public void unregisterDiscoveryService() {
        this.bridgeDiscoveryService = null;

    }

    private IsyHandler getThingHandler(String address) {
        String addressNoDeviceId = InsteonAddress.stripDeviceId(address);
        for (Thing thing : getThing().getThings()) {
            String thingsAddress = InsteonAddress.stripDeviceId((String) thing.getConfiguration().get("address"));
            if (addressNoDeviceId.equals(thingsAddress)) {
                logger.debug("address: " + thingsAddress);
                return (IsyHandler) thing.getHandler();
            }
        }
        return null;
    }
}
