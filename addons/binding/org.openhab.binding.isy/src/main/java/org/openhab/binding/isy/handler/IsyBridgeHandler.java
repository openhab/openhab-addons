package org.openhab.binding.isy.handler;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.isy.IsyBindingConstants;
import org.openhab.binding.isy.config.IsyBridgeConfiguration;
import org.openhab.binding.isy.internal.ISYModelChangeListener;
import org.openhab.binding.isy.internal.InsteonAddress;
import org.openhab.binding.isy.internal.InsteonClientProvider;
import org.openhab.binding.isy.internal.IsyRestClient;
import org.openhab.binding.isy.internal.OHIsyClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IsyBridgeHandler extends BaseBridgeHandler implements InsteonClientProvider {
    private Logger logger = LoggerFactory.getLogger(IsyBridgeHandler.class);

    private DiscoveryService bridgeDiscoveryService;

    private OHIsyClient isyClient;

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

    private IsyVariableHandler getVariableHandler(String id) {
        logger.debug("find thing handler for address: " + id);
        String[] idParts = id.split(" ");
        for (Thing thing : getThing().getThings()) {
            if (IsyBindingConstants.VARIABLE_THING_TYPE.equals(thing.getThingTypeUID())) {
                String theId = (String) thing.getConfiguration().get("id");
                String theType = (String) thing.getConfiguration().get("type");
                if (theType.equals(idParts[0]) && theId.equals(idParts[1])) {
                    return (IsyVariableHandler) thing.getHandler();
                }
            }
        }
        return null;
    }

    @Override
    public void initialize() {
        // super.initialize();
        logger.debug("initialize called for bridge handler");
        IsyBridgeConfiguration config = getThing().getConfiguration().as(IsyBridgeConfiguration.class);

        isyClient = new IsyRestClient(config.getIpAddress(), config.getUser(), config.getPassword(),
                new ISYModelChangeListener() {

                    @Override
                    public void onModelChanged(String control, String action, String node) {
                        IsyThingHandler handler;
                        if ("_1".equals(control) && "6".equals(action)) {
                            handler = getVariableHandler(node);
                            logger.debug("handler");
                        } else {
                            handler = getThingHandler(node);
                        }
                        if (handler != null) {
                            handler.handleUpdate(control, action, node);
                        }

                    }

                    @Override
                    public void onDeviceOnLine() {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onDeviceOffLine() {
                        // TODO Auto-generated method stub

                    }
                });
        updateStatus(ThingStatus.ONLINE);

    }

    public void registerDiscoveryService(DiscoveryService isyBridgeDiscoveryService) {
        this.bridgeDiscoveryService = isyBridgeDiscoveryService;

    }

    public void unregisterDiscoveryService() {
        this.bridgeDiscoveryService = null;

    }

    private IsyThingHandler getThingHandler(String address) {
        logger.debug("find thing handler for address: " + address);
        String addressNoDeviceId = InsteonAddress.stripDeviceId(address);
        for (Thing thing : getThing().getThings()) {
            if (!(IsyBindingConstants.PROGRAM_THING_TYPE.equals(thing.getThingTypeUID())
                    || IsyBindingConstants.VARIABLE_THING_TYPE.equals(thing.getThingTypeUID()))) {

                String theAddress = (String) thing.getConfiguration().get("address");
                if (theAddress == null) {
                    logger.debug("no address");
                }
                String thingsAddress = InsteonAddress.stripDeviceId(theAddress);
                if (addressNoDeviceId.equals(thingsAddress)) {
                    logger.debug("address: " + thingsAddress);
                    return (IsyInsteonDeviceHandler) thing.getHandler();
                }
            }
        }
        return null;
    }

    @Override
    public OHIsyClient getInsteonClient() {
        return isyClient;
    }
}
