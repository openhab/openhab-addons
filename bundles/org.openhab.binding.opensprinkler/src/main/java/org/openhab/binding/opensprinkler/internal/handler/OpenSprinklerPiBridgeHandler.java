package org.openhab.binding.opensprinkler.internal.handler;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.opensprinkler.internal.api.OpenSprinklerApiFactory;
import org.openhab.binding.opensprinkler.internal.config.OpenSprinklerPiConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenSprinklerPiBridgeHandler extends OpenSprinklerBaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(OpenSprinklerPiBridgeHandler.class);

    private OpenSprinklerPiConfig openSprinklerConfig = null;

    public OpenSprinklerPiBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        openSprinklerConfig = getConfig().as(OpenSprinklerPiConfig.class);

        if (openSprinklerConfig == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Could not parse the config for the OpenSprinkler.");

            return;
        }

        logger.debug("Initializing OpenSprinkler with config (Refresh: {}).", openSprinklerConfig.refresh);

        try {
            openSprinklerDevice = OpenSprinklerApiFactory.getGpioApi(openSprinklerConfig.stations);
        } catch (Exception exp) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Could not create a connection to the OpenSprinkler.");
            logger.debug("Could not create API connection to the OpenSprinkler device. Exception received: {}",
                    exp.toString());

            return;
        }

        logger.debug("Successfully created API connection to the OpenSprinkler device.");

        try {
            openSprinklerDevice.openConnection();
        } catch (Exception exp) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Could not open the connection to the OpenSprinkler.");
            logger.debug("Could not open API connection to the OpenSprinkler device. Exception received: {}",
                    exp.toString());
        }

        if (openSprinklerDevice.isConnected()) {
            updateStatus(ThingStatus.ONLINE);
            logger.debug("OpenSprinkler connected.");
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Could not initialize the connection to the OpenSprinkler.");

            return;
        }

        super.initialize();
    }

    @Override
    protected long getRefreshInterval() {
        return openSprinklerConfig.refresh;
    }

}
