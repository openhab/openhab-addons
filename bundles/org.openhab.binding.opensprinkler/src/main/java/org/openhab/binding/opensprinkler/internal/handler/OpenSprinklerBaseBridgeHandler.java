/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.opensprinkler.internal.handler;

import static org.openhab.binding.opensprinkler.internal.OpenSprinklerBindingConstants.DEFAULT_WAIT_BEFORE_INITIAL_REFRESH;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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

/**
 * @author Florian Schmidt - Refactoring
 */
@NonNullByDefault
public abstract class OpenSprinklerBaseBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(OpenSprinklerBaseBridgeHandler.class);

    @Nullable
    private ScheduledFuture<?> pollingJob;
    @Nullable
    protected OpenSprinklerApi openSprinklerDevice;

    public OpenSprinklerBaseBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    public OpenSprinklerApi getApi() {
        OpenSprinklerApi api = openSprinklerDevice;
        if (api == null) {
            throw new IllegalStateException();
        }
        return api;
    }

    @Override
    public void initialize() {
        pollingJob = scheduler.scheduleWithFixedDelay(this::refreshStations, DEFAULT_WAIT_BEFORE_INITIAL_REFRESH,
                getRefreshInterval(), TimeUnit.SECONDS);
    }

    protected abstract long getRefreshInterval();

    private void refreshStations() {
        if (openSprinklerDevice != null) {
            if (openSprinklerDevice.isManualModeEnabled()) {
                updateStatus(ThingStatus.ONLINE);

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
    }

    @Override
    public void dispose() {
        super.dispose();
        if (openSprinklerDevice != null) {
            try {
                openSprinklerDevice.leaveManualMode();
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
