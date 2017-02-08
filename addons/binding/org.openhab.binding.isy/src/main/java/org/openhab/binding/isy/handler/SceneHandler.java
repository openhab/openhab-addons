package org.openhab.binding.isy.handler;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.isy.config.IsyInsteonDeviceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SceneHandler extends AbtractIsyThingHandler {
    private static final Logger logger = LoggerFactory.getLogger(SceneHandler.class);

    public SceneHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        IsyBridgeHandler bridgeHandler = getBridgeHandler();
        IsyInsteonDeviceConfiguration test = getThing().getConfiguration().as(IsyInsteonDeviceConfiguration.class);
        String myAddress = test.address;
        if (command.equals(RefreshType.REFRESH)) {
            logger.debug("Refresh not implemented for scenes");
        } else if (OnOffType.ON.equals(command)) {
            bridgeHandler.getInsteonClient().changeSceneState(myAddress, 255);

        } else if (OnOffType.OFF.equals(command)) {
            bridgeHandler.getInsteonClient().changeSceneState(myAddress, 0);

        } else {
            logger.warn("Unexpected command: " + command.toFullString());
        }
    }

    @Override
    public void handleUpdate(Object... parameters) {
        logger.warn("Not expecting to get updates for scenes");

    }

    @Override
    public void initialize() {
        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        updateStatus(ThingStatus.ONLINE);

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }
}
