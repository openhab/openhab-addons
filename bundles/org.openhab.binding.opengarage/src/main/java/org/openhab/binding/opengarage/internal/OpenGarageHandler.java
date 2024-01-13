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
package org.openhab.binding.opengarage.internal;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
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

    private @NonNullByDefault({}) OpenGarageWebTargets webTargets;

    // reference to periodically scheduled poll task
    private Future<?> pollScheduledFuture = CompletableFuture.completedFuture(null);

    // reference to one-shot poll task which gets scheduled after a garage state change command
    private Future<?> pollScheduledFutureTransition = CompletableFuture.completedFuture(null);
    private Instant lastTransition;
    private String lastTransitionText;

    private OpenGarageConfiguration config = new OpenGarageConfiguration();

    public OpenGarageHandler(Thing thing) {
        super(thing);
        this.lastTransition = Instant.MIN;
        this.lastTransitionText = "";
    }

    @Override
    public synchronized void handleCommand(ChannelUID channelUID, Command command) {
        try {
            logger.debug("Received command {} for thing '{}' on channel {}", command, thing.getUID().getAsString(),
                    channelUID.getId());
            Function<Boolean, Boolean> maybeInvert = getInverter(channelUID.getId());
            switch (channelUID.getId()) {
                case OpenGarageBindingConstants.CHANNEL_OG_STATUS:
                case OpenGarageBindingConstants.CHANNEL_OG_STATUS_SWITCH:
                case OpenGarageBindingConstants.CHANNEL_OG_STATUS_ROLLERSHUTTER:
                    if (command.equals(StopMoveType.STOP) || command.equals(StopMoveType.MOVE)) {
                        changeStatus(OpenGarageCommand.CLICK);
                    } else {
                        boolean doorOpen = command.equals(OnOffType.ON) || command.equals(UpDownType.UP);
                        changeStatus(maybeInvert.apply(doorOpen) ? OpenGarageCommand.OPEN : OpenGarageCommand.CLOSE);
                        this.lastTransition = Instant.now();
                        this.lastTransitionText = doorOpen ? this.config.doorOpeningState
                                : this.config.doorClosingState;

                        this.poll(); // invoke poll directly to communicate the door transition state
                        this.pollScheduledFutureTransition.cancel(false);
                        this.pollScheduledFutureTransition = this.scheduler.schedule(this::poll,
                                this.config.doorTransitionTimeSeconds, TimeUnit.SECONDS);
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
        this.config = getConfigAs(OpenGarageConfiguration.class);
        logger.debug("config.hostname = {}, refresh = {}, port = {}", config.hostname, config.refresh, config.port);
        if (config.hostname.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Hostname/IP address must be set");
        } else {
            updateStatus(ThingStatus.UNKNOWN);
            int requestTimeout = Math.max(OpenGarageWebTargets.DEFAULT_TIMEOUT_MS, config.refresh * 1000);
            webTargets = new OpenGarageWebTargets(config.hostname, config.port, config.password, requestTimeout);
            this.pollScheduledFuture = this.scheduler.scheduleWithFixedDelay(this::poll, 1, config.refresh,
                    TimeUnit.SECONDS);
        }
    }

    @Override
    public void dispose() {
        this.pollScheduledFuture.cancel(true);
        this.pollScheduledFutureTransition.cancel(true);
        super.dispose();
    }

    /**
     * Update the state of the controller.
     *
     *
     */
    private synchronized void poll() {
        try {
            logger.debug("Polling for state");
            ControllerVariables controllerVariables = webTargets.getControllerVariables();
            long lastTransitionAgoSecs = Duration.between(lastTransition, Instant.now()).getSeconds();
            boolean inTransition = lastTransitionAgoSecs < this.config.doorTransitionTimeSeconds;
            if (controllerVariables != null) {
                updateStatus(ThingStatus.ONLINE);
                updateState(OpenGarageBindingConstants.CHANNEL_OG_DISTANCE,
                        new QuantityType<>(controllerVariables.dist, MetricPrefix.CENTI(SIUnits.METRE)));
                Function<Boolean, Boolean> maybeInvert = getInverter(
                        OpenGarageBindingConstants.CHANNEL_OG_STATUS_SWITCH);

                if ((controllerVariables.door != 0) && (controllerVariables.door != 1)) {
                    logger.debug("Received unknown door value: {}", controllerVariables.door);
                } else {
                    boolean doorOpen = controllerVariables.door == 1;
                    OnOffType onOff = OnOffType.from(maybeInvert.apply(doorOpen));
                    UpDownType upDown = doorOpen ? UpDownType.UP : UpDownType.DOWN;
                    OpenClosedType contact = doorOpen ? OpenClosedType.OPEN : OpenClosedType.CLOSED;

                    String transitionText;
                    if (inTransition) {
                        transitionText = this.lastTransitionText;
                    } else {
                        transitionText = doorOpen ? this.config.doorOpenState : this.config.doorClosedState;
                    }
                    if (!inTransition) {
                        updateState(OpenGarageBindingConstants.CHANNEL_OG_STATUS, onOff); // deprecated channel
                        updateState(OpenGarageBindingConstants.CHANNEL_OG_STATUS_SWITCH, onOff);
                    }
                    updateState(OpenGarageBindingConstants.CHANNEL_OG_STATUS_ROLLERSHUTTER, upDown);
                    updateState(OpenGarageBindingConstants.CHANNEL_OG_STATUS_CONTACT, contact);
                    updateState(OpenGarageBindingConstants.CHANNEL_OG_STATUS_TEXT, new StringType(transitionText));
                }

                switch (controllerVariables.vehicle) {
                    case 0:
                        updateState(OpenGarageBindingConstants.CHANNEL_OG_VEHICLE,
                                new StringType("No vehicle detected"));
                        break;
                    case 1:
                        updateState(OpenGarageBindingConstants.CHANNEL_OG_VEHICLE, new StringType("Vehicle detected"));
                        break;
                    case 2:
                        updateState(OpenGarageBindingConstants.CHANNEL_OG_VEHICLE,
                                new StringType("Vehicle status unknown"));
                        break;
                    case 3:
                        updateState(OpenGarageBindingConstants.CHANNEL_OG_VEHICLE,
                                new StringType("Vehicle status not available"));
                        break;

                    default:
                        logger.debug("Received unknown vehicle value: {}", controllerVariables.vehicle);
                }
                updateState(OpenGarageBindingConstants.CHANNEL_OG_VEHICLE_STATUS,
                        new DecimalType(controllerVariables.vehicle));
            }
        } catch (IOException e) {
            logger.debug("Could not connect to OpenGarage controller", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Could not connect to OpenGarage controller");
        } catch (RuntimeException e) {
            logger.debug("Unexpected error connecting to OpenGarage controller", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void changeStatus(OpenGarageCommand status) throws OpenGarageCommunicationException {
        webTargets.setControllerVariables(status);
    }

    private Function<Boolean, Boolean> getInverter(String channelUID) {
        Channel channel = getThing().getChannel(channelUID);
        boolean invert = channel != null && channel.getConfiguration().as(OpenGarageChannelConfiguration.class).invert;
        if (invert) {
            return onOff -> !onOff;
        } else {
            return Function.identity();
        }
    }
}
