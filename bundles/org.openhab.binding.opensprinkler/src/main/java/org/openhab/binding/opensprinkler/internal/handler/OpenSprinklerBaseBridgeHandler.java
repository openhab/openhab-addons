package org.openhab.binding.opensprinkler.internal.handler;

import static org.openhab.binding.opensprinkler.internal.OpenSprinklerBindingConstants.DEFAULT_WAIT_BEFORE_INITIAL_REFRESH;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.opensprinkler.internal.api.OpenSprinklerApi;
import org.openhab.binding.opensprinkler.internal.api.exception.CommunicationApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class OpenSprinklerBaseBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(OpenSprinklerBaseBridgeHandler.class);

    private ScheduledFuture<?> pollingJob;
    protected OpenSprinklerApi openSprinklerDevice = null;

    public OpenSprinklerBaseBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    public OpenSprinklerApi getApi() {
        return this.openSprinklerDevice;
    }

    @Override
    public void initialize() {
        pollingJob = scheduler.scheduleWithFixedDelay(this::refresh, DEFAULT_WAIT_BEFORE_INITIAL_REFRESH,
                getRefreshInterval(), TimeUnit.SECONDS);
    }

    protected abstract long getRefreshInterval();

    /**
     * Threaded scheduled job that periodically syncs the state of the OpenSprinkler device.
     */
    private void refresh() {
        if (openSprinklerDevice != null) {
            if (openSprinklerDevice.isConnected()) {
                logger.debug("Refreshing state with the OpenSprinkler device.");

                this.getThing().getThings().forEach(thing -> {
                    OpenSprinklerBaseHandler handler = (OpenSprinklerBaseHandler) thing.getHandler();
                    if (handler != null) {
                        handler.updateChannels();
                    }
                });
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        "Could not sync status with the OpenSprinkler.");
            }
        }
    };

    @Override
    public void dispose() {
        super.dispose();
        if (openSprinklerDevice != null) {
            try {
                openSprinklerDevice.closeConnection();
            } catch (CommunicationApiException e) {
                logger.error("Could not close connection on teardown.", e);
            }
            openSprinklerDevice = null;
        }

        if (pollingJob != null) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Nothing to do for the bridge handler
    }

}
