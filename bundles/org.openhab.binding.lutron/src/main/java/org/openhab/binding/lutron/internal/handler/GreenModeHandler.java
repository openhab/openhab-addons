/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.lutron.internal.handler;

import static org.openhab.binding.lutron.internal.LutronBindingConstants.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lutron.internal.protocol.ModeCommand;
import org.openhab.binding.lutron.internal.protocol.lip.LutronCommandType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler responsible for communicating with RadioRA 2 Green Mode subsystem
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class GreenModeHandler extends LutronHandler {
    private static final int GREENSTEP_MIN = 1;

    // poll interval parameters are in minutes
    private static final int POLL_INTERVAL_DEFAULT = 15;
    private static final int POLL_INTERVAL_MAX = 240;
    private static final int POLL_INTERVAL_MIN = 0;

    private final Logger logger = LoggerFactory.getLogger(GreenModeHandler.class);

    private int integrationId;
    private int pollInterval;
    private @Nullable ScheduledFuture<?> pollJob;

    public GreenModeHandler(Thing thing) {
        super(thing);
    }

    @Override
    public int getIntegrationId() {
        return integrationId;
    }

    @Override
    public void initialize() {
        Number id = (Number) getThing().getConfiguration().get(INTEGRATION_ID);
        if (id == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No integrationId");
            return;
        }
        integrationId = id.intValue();

        Number pollInterval = (Number) getThing().getConfiguration().get(POLL_INTERVAL);
        if (pollInterval == null) {
            this.pollInterval = POLL_INTERVAL_DEFAULT;
        } else {
            this.pollInterval = pollInterval.intValue();
            this.pollInterval = Math.min(this.pollInterval, POLL_INTERVAL_MAX);
            this.pollInterval = Math.max(this.pollInterval, POLL_INTERVAL_MIN);
        }
        logger.debug("Initializing Green Mode handler for integration ID {} with poll interval {}", integrationId,
                this.pollInterval);

        initDeviceState();
    }

    @Override
    protected void initDeviceState() {
        logger.debug("Initializing device state for Green Mode subsystem {}", getIntegrationId());
        stopPolling();
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge configured");
        } else if (bridge.getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Awaiting initial response");
            queryGreenMode(ModeCommand.ACTION_STEP);
            // handleUpdate() will set thing status to online when response arrives
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    protected void thingOfflineNotify() {
        stopPolling();
    }

    private void startPolling() {
        if (pollInterval > 0 && pollJob == null) {
            logger.debug("Scheduling green mode polling job for integration ID {}", integrationId);
            pollJob = scheduler.scheduleWithFixedDelay(this::pollState, pollInterval, pollInterval, TimeUnit.MINUTES);
        }
    }

    private void stopPolling() {
        ScheduledFuture<?> pollJob = this.pollJob;
        if (pollJob != null) {
            this.pollJob = null;
            logger.debug("Canceling green mode polling job for integration ID {}", integrationId);
            pollJob.cancel(true);
        }
    }

    private synchronized void pollState() {
        logger.trace("Executing green mode polling job for integration ID {}", integrationId);
        queryGreenMode(ModeCommand.ACTION_STEP);
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (channelUID.getId().equals(CHANNEL_STEP)) {
            queryGreenMode(ModeCommand.ACTION_STEP);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_STEP)) {
            if (command == OnOffType.ON) {
                greenMode(ModeCommand.ACTION_STEP, 2);
            } else if (command == OnOffType.OFF) {
                greenMode(ModeCommand.ACTION_STEP, 1);
            } else if (command instanceof Number) {
                Integer step = ((Number) command).intValue();
                if (step.intValue() >= GREENSTEP_MIN) {
                    greenMode(ModeCommand.ACTION_STEP, step);
                }
            } else if (command instanceof RefreshType) {
                queryGreenMode(ModeCommand.ACTION_STEP);
            } else {
                logger.debug("Ignoring invalid command {} for id {}", command, integrationId);
            }
        } else {
            logger.debug("Ignoring command to invalid channel {} for id {}", channelUID.getId(), integrationId);
        }
    }

    @Override
    public void handleUpdate(LutronCommandType type, String... parameters) {
        try {
            if (type == LutronCommandType.MODE && parameters.length > 1
                    && ModeCommand.ACTION_STEP.toString().equals(parameters[0])) {
                Long step = Long.valueOf(parameters[1]);
                if (getThing().getStatus() == ThingStatus.UNKNOWN) {
                    updateStatus(ThingStatus.ONLINE);
                    startPolling();
                }
                updateState(CHANNEL_STEP, new DecimalType(step.longValue()));
            } else {
                logger.debug("Ignoring unexpected update for id {}", integrationId);
            }
        } catch (NumberFormatException e) {
            logger.debug("Encountered number format exception while handling update for greenmode {}", integrationId);
        }
    }

    @Override
    public void dispose() {
        stopPolling();
    }
}
