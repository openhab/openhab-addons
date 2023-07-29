/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.opengarage.internal;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.opengarage.internal.api.ControllerVariables;
import org.openhab.binding.opengarage.internal.api.Enums.OpenGarageCommand;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenGarageHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Paul Smedley - Initial contribution
 * @author Dan Cunningham - Minor improvements to vehicle state and invert option
 */
@NonNullByDefault
public class OpenGarageHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(OpenGarageHandler.class);

    private long refreshInterval;

    private @NonNullByDefault({}) OpenGarageWebTargets webTargets;
    private @Nullable ScheduledFuture<?> pollFuture;

    public OpenGarageHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            logger.debug("Received command {} for thing '{}' on channel {}", command, thing.getUID().getAsString(),
                    channelUID.getId());
            boolean invert = isChannelInverted(channelUID.getId());
            switch (channelUID.getId()) {
                case OpenGarageBindingConstants.CHANNEL_OG_STATUS:
                case OpenGarageBindingConstants.CHANNEL_OG_STATUS_SWITCH:
                case OpenGarageBindingConstants.CHANNEL_OG_STATUS_ROLLERSHUTTER:
                    if (command.equals(OnOffType.ON) || command.equals(UpDownType.UP)) {
                        changeStatus(invert ? OpenGarageCommand.CLOSE : OpenGarageCommand.OPEN);
                        return;
                    } else if (command.equals(OnOffType.OFF) || command.equals(UpDownType.DOWN)) {
                        changeStatus(invert ? OpenGarageCommand.OPEN : OpenGarageCommand.CLOSE);
                        return;
                    } else if (command.equals(StopMoveType.STOP) || command.equals(StopMoveType.MOVE)) {
                        changeStatus(OpenGarageCommand.CLICK);
                        return;
                    }
                    break;
                default:
            }
        } catch (OpenGarageCommunicationException ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
        }
    }

    @Override
    public void initialize() {
        OpenGarageConfiguration config = getConfigAs(OpenGarageConfiguration.class);
        logger.debug("config.hostname = {}, refresh = {}, port = {}", config.hostname, config.refresh, config.port);
        if (config.hostname == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Hostname/IP address must be set");
        } else {
            webTargets = new OpenGarageWebTargets(config.hostname, config.port, config.password);
            refreshInterval = config.refresh;

            schedulePoll();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        stopPoll();
    }

    private void schedulePoll() {
        if (pollFuture != null) {
            pollFuture.cancel(false);
        }
        logger.debug("Scheduling poll for 1 second out, then every {} s", refreshInterval);
        pollFuture = scheduler.scheduleWithFixedDelay(this::poll, 1, refreshInterval, TimeUnit.SECONDS);
    }

    private void poll() {
        try {
            logger.debug("Polling for state");
            pollStatus();
        } catch (IOException e) {
            logger.debug("Could not connect to OpenGarage controller", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (RuntimeException e) {
            logger.warn("Unexpected error connecting to OpenGarage controller", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void stopPoll() {
        final Future<?> future = pollFuture;
        if (future != null && !future.isCancelled()) {
            future.cancel(true);
            pollFuture = null;
        }
    }

    private void pollStatus() throws IOException {
        ControllerVariables controllerVariables = webTargets.getControllerVariables();
        updateStatus(ThingStatus.ONLINE);
        if (controllerVariables != null) {
            updateState(OpenGarageBindingConstants.CHANNEL_OG_DISTANCE,
                    new QuantityType<>(controllerVariables.dist, MetricPrefix.CENTI(SIUnits.METRE)));
            boolean invert = isChannelInverted(OpenGarageBindingConstants.CHANNEL_OG_STATUS_SWITCH);
            switch (controllerVariables.door) {
                case 0:
                    updateState(OpenGarageBindingConstants.CHANNEL_OG_STATUS, invert ? OnOffType.ON : OnOffType.OFF);
                    updateState(OpenGarageBindingConstants.CHANNEL_OG_STATUS_SWITCH,
                            invert ? OnOffType.ON : OnOffType.OFF);
                    updateState(OpenGarageBindingConstants.CHANNEL_OG_STATUS_ROLLERSHUTTER, UpDownType.DOWN);
                    updateState(OpenGarageBindingConstants.CHANNEL_OG_STATUS_CONTACT, OpenClosedType.CLOSED);
                    break;
                case 1:
                    updateState(OpenGarageBindingConstants.CHANNEL_OG_STATUS, invert ? OnOffType.OFF : OnOffType.ON);
                    updateState(OpenGarageBindingConstants.CHANNEL_OG_STATUS_SWITCH,
                            invert ? OnOffType.OFF : OnOffType.ON);
                    updateState(OpenGarageBindingConstants.CHANNEL_OG_STATUS_ROLLERSHUTTER, UpDownType.UP);
                    updateState(OpenGarageBindingConstants.CHANNEL_OG_STATUS_CONTACT, OpenClosedType.OPEN);
                    break;
                default:
                    logger.warn("Received unknown door value: {}", controllerVariables.door);
            }
            switch (controllerVariables.vehicle) {
                case 0:
                    updateState(OpenGarageBindingConstants.CHANNEL_OG_VEHICLE, new StringType("No vehicle detected"));
                    break;
                case 1:
                    updateState(OpenGarageBindingConstants.CHANNEL_OG_VEHICLE, new StringType("Vehicle detected"));
                    break;
                case 2:
                    updateState(OpenGarageBindingConstants.CHANNEL_OG_VEHICLE,
                            new StringType("Vehicle status unknown"));
                    break;
                default:
                    logger.warn("Received unknown vehicle value: {}", controllerVariables.vehicle);
            }
            updateState(OpenGarageBindingConstants.CHANNEL_OG_VEHICLE_STATUS,
                    new DecimalType(controllerVariables.vehicle));
        }
    }

    private void changeStatus(OpenGarageCommand status) throws OpenGarageCommunicationException {
        webTargets.setControllerVariables(status);
    }

    private boolean isChannelInverted(String channelUID) {
        Channel channel = getThing().getChannel(channelUID);
        return channel != null && channel.getConfiguration().as(OpenGarageChannelConfiguration.class).invert;
    }
}
