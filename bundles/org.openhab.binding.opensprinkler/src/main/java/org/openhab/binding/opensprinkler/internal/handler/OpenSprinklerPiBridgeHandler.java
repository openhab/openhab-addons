package org.openhab.binding.opensprinkler.internal.handler;

import static org.openhab.binding.opensprinkler.internal.OpenSprinklerBindingConstants.DEFAULT_REFRESH_RATE;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.opensprinkler.internal.api.OpenSprinklerApi;
import org.openhab.binding.opensprinkler.internal.api.OpenSprinklerApiFactory;
import org.openhab.binding.opensprinkler.internal.config.OpenSprinklerPiConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Florian Schmidt - Refactoring
 */
@NonNullByDefault
public class OpenSprinklerPiBridgeHandler extends OpenSprinklerBaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(OpenSprinklerPiBridgeHandler.class);

    @Nullable
    private OpenSprinklerPiConfig openSprinklerConfig;

    public OpenSprinklerPiBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        OpenSprinklerPiConfig openSprinklerConfig = getConfig().as(OpenSprinklerPiConfig.class);
        this.openSprinklerConfig = openSprinklerConfig;

        logger.debug("Initializing OpenSprinkler with config (Refresh: {}).", openSprinklerConfig.refresh);

        OpenSprinklerApi openSprinklerDevice;
        try {
            openSprinklerDevice = OpenSprinklerApiFactory.getGpioApi(openSprinklerConfig.stations);
            this.openSprinklerDevice = openSprinklerDevice;
        } catch (Exception exp) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Could not create API connection to the OpenSprinkler device. Error received: " + exp);

            return;
        }

        logger.debug("Successfully created API connection to the OpenSprinkler device.");

        try {
            openSprinklerDevice.enterManualMode();
        } catch (Exception exp) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Could not open API connection to the OpenSprinkler device. Error received: " + exp);
        }

        if (openSprinklerDevice.isManualModeEnabled()) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Could not initialize the connection to the OpenSprinkler.");

            return;
        }

        super.initialize();
    }

    @Override
    protected long getRefreshInterval() {
        OpenSprinklerPiConfig openSprinklerConfig = this.openSprinklerConfig;
        if (openSprinklerConfig == null) {
            return DEFAULT_REFRESH_RATE;
        }
        return openSprinklerConfig.refresh;
    }

}
