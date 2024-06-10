/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.teslascope.internal;

import static org.openhab.binding.teslascope.internal.TeslascopeBindingConstants.*;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.teslascope.internal.api.DetailedInformation;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TeslascopeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Paul Smedley - Initial contribution
 */
@NonNullByDefault
public class TeslascopeHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TeslascopeHandler.class);

    private long refreshInterval;
    private String apiKey = "";
    private String publicID = "";

    private @Nullable TeslascopeConfiguration config;
    private @NonNullByDefault({}) TeslascopeWebTargets webTargets;
    private @Nullable ScheduledFuture<?> pollFuture;

    public TeslascopeHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.warn("This binding is read only (for now)");
    }

    @Override
    public void initialize() {
        config = getConfigAs(TeslascopeConfiguration.class);
        if (config.apiKey.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.no-api-key");
            return;
        }

        webTargets = new TeslascopeWebTargets();
        updateStatus(ThingStatus.UNKNOWN);
        refreshInterval = config.refreshInterval;
        publicID = config.publicID;
        apiKey = config.apiKey;

        schedulePoll();
    }

    @Override
    public void dispose() {
        super.dispose();
        stopPoll();
    }

    private void schedulePoll() {
        logger.debug("Scheduling poll every {} s", refreshInterval);
        this.pollFuture = scheduler.scheduleWithFixedDelay(this::poll, 0, refreshInterval, TimeUnit.SECONDS);
    }

    private void poll() {
        try {
            logger.debug("Polling for state");
            pollStatus();
        } catch (IOException e) {
            logger.debug("Could not connect to Teslascope API", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (RuntimeException e) {
            logger.warn("Unexpected error connecting to Teslascope API", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void stopPoll() {
        final Future<?> future = pollFuture;
        if (future != null) {
            future.cancel(true);
            pollFuture = null;
        }
    }

    private void pollStatus() throws IOException {

        try {
            DetailedInformation detailedInformation = webTargets.getDetailedInformation(publicID, apiKey);

            updateStatus(ThingStatus.ONLINE);
            updateState(TeslascopeBindingConstants.CHANNEL_VIN, new StringType(detailedInformation.vin));
            updateState(TeslascopeBindingConstants.CHANNEL_VEHICLENAME,
                    new StringType(detailedInformation.vehiclename));
            updateState(TeslascopeBindingConstants.CHANNEL_VEHICLESTATE,
                    new StringType(detailedInformation.vehiclestate));
            updateState(TeslascopeBindingConstants.CHANNEL_ODOMETER, new DecimalType(detailedInformation.odometer));
            updateState(TeslascopeBindingConstants.CHANNEL_BATTERYLEVEL,
                    new DecimalType(detailedInformation.batterylevel));
            updateState(TeslascopeBindingConstants.CHANNEL_CHARGINGSTATE,
                    new StringType(detailedInformation.chargingstate));
        } catch (TeslascopeCommunicationException e) {
            logger.debug("Unexpected error connecting to Teslascope API", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }
}
