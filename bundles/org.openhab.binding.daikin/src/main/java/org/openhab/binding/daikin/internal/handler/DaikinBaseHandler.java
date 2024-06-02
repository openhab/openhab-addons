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
package org.openhab.binding.daikin.internal.handler;

import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.daikin.internal.DaikinBindingConstants;
import org.openhab.binding.daikin.internal.DaikinCommunicationException;
import org.openhab.binding.daikin.internal.DaikinCommunicationForbiddenException;
import org.openhab.binding.daikin.internal.DaikinDynamicStateDescriptionProvider;
import org.openhab.binding.daikin.internal.DaikinWebTargets;
import org.openhab.binding.daikin.internal.api.Enums.HomekitMode;
import org.openhab.binding.daikin.internal.config.DaikinConfiguration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class that handles common tasks with a Daikin air conditioning unit.
 *
 * @author Tim Waterhouse - Initial Contribution
 * @author Paul Smedley - Modifications to support Airbase Controllers
 * @author Jimmy Tanagra - Split handler classes, support Airside and DynamicStateDescription
 *
 */
@NonNullByDefault
public abstract class DaikinBaseHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(DaikinBaseHandler.class);

    private final @Nullable HttpClient httpClient;

    private long refreshInterval;

    protected @Nullable DaikinWebTargets webTargets;
    private @Nullable ScheduledFuture<?> pollFuture;
    protected final DaikinDynamicStateDescriptionProvider stateDescriptionProvider;
    protected @Nullable DaikinConfiguration config;
    private boolean uuidRegistrationAttempted = false;

    // Abstract methods to be overridden by specific Daikin implementation class
    protected abstract void pollStatus() throws DaikinCommunicationException;

    protected abstract void changePower(boolean power) throws DaikinCommunicationException;

    protected abstract void changeSetPoint(double newTemperature) throws DaikinCommunicationException;

    protected abstract void changeMode(String mode) throws DaikinCommunicationException;

    protected abstract void changeFanSpeed(String fanSpeed) throws DaikinCommunicationException;

    // Power, Temp, Fan and Mode are handled in this base class. Override this to handle additional channels.
    protected abstract boolean handleCommandInternal(ChannelUID channelUID, Command command)
            throws DaikinCommunicationException;

    protected abstract void registerUuid(@Nullable String key);

    public DaikinBaseHandler(Thing thing, DaikinDynamicStateDescriptionProvider stateDescriptionProvider,
            @Nullable HttpClient httpClient) {
        super(thing);
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.httpClient = httpClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (webTargets == null) {
            logger.warn("webTargets is null. This is possibly a bug.");
            return;
        }
        try {
            if (handleCommandInternal(channelUID, command)) {
                return;
            }
            switch (channelUID.getId()) {
                case DaikinBindingConstants.CHANNEL_AC_POWER:
                    if (command instanceof OnOffType onOffCommand) {
                        changePower(onOffCommand.equals(OnOffType.ON));
                        return;
                    }
                    break;
                case DaikinBindingConstants.CHANNEL_AC_TEMP:
                    if (changeSetPoint(command)) {
                        return;
                    }
                    break;
                case DaikinBindingConstants.CHANNEL_AIRBASE_AC_FAN_SPEED:
                case DaikinBindingConstants.CHANNEL_AC_FAN_SPEED:
                    if (command instanceof StringType stringCommand) {
                        changeFanSpeed(stringCommand.toString());
                        return;
                    }
                    break;
                case DaikinBindingConstants.CHANNEL_AC_HOMEKITMODE:
                    if (command instanceof StringType) {
                        changeHomekitMode(command.toString());
                        return;
                    }
                    break;
                case DaikinBindingConstants.CHANNEL_AC_MODE:
                    if (command instanceof StringType stringCommand) {
                        changeMode(stringCommand.toString());
                        return;
                    }
                    break;
            }
            logger.debug("Received command ({}) of wrong type for thing '{}' on channel {}", command,
                    thing.getUID().getAsString(), channelUID.getId());
        } catch (DaikinCommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Daikin AC Unit");
        config = getConfigAs(DaikinConfiguration.class);
        if (config.host == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Host address must be set");
        } else {
            if (config.uuid != null) {
                config.uuid = config.uuid.replaceAll("\\s|-", "");
            }
            webTargets = new DaikinWebTargets(httpClient, config.host, config.secure, config.uuid);
            refreshInterval = config.refresh;
            schedulePoll();
        }
    }

    @Override
    public void handleRemoval() {
        stopPoll();
        super.handleRemoval();
    }

    @Override
    public void dispose() {
        stopPoll();
        super.dispose();
    }

    protected void schedulePoll() {
        if (pollFuture != null) {
            pollFuture.cancel(false);
        }
        logger.debug("Scheduling poll for 1s out, then every {} s", refreshInterval);
        pollFuture = scheduler.scheduleWithFixedDelay(this::poll, 1, refreshInterval, TimeUnit.SECONDS);
    }

    protected synchronized void stopPoll() {
        if (pollFuture != null && !pollFuture.isCancelled()) {
            pollFuture.cancel(true);
            pollFuture = null;
        }
    }

    private synchronized void poll() {
        try {
            logger.trace("Polling for state");
            pollStatus();
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (DaikinCommunicationForbiddenException e) {
            if (!uuidRegistrationAttempted && config.key != null && config.uuid != null) {
                logger.debug("poll: Attempting to register uuid {} with key {}", config.uuid, config.key);
                registerUuid(config.key);
                uuidRegistrationAttempted = true;
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Access denied. Check uuid/key.");
                logger.warn("{} access denied by adapter. Check uuid/key.", thing.getUID());
            }
        } catch (DaikinCommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    protected void updateTemperatureChannel(String channel, Optional<Double> maybeTemperature) {
        updateState(channel,
                maybeTemperature.<State> map(t -> new QuantityType<>(t, SIUnits.CELSIUS)).orElse(UnDefType.UNDEF));
    }

    /**
     * @return true if the command was of an expected type, false otherwise
     */
    private boolean changeSetPoint(Command command) throws DaikinCommunicationException {
        double newTemperature;
        if (command instanceof DecimalType decimalCommand) {
            newTemperature = decimalCommand.doubleValue();
        } else if (command instanceof QuantityType) {
            newTemperature = ((QuantityType<Temperature>) command).toUnit(SIUnits.CELSIUS).doubleValue();
        } else {
            return false;
        }

        // Only half degree increments are allowed, all others are silently rejected by the A/C units
        newTemperature = Math.round(newTemperature * 2) / 2.0;
        changeSetPoint(newTemperature);
        return true;
    }

    private void changeHomekitMode(String homekitmode) throws DaikinCommunicationException {
        if (HomekitMode.OFF.getValue().equals(homekitmode)) {
            changePower(false);
        } else {
            changePower(true);
            if (HomekitMode.AUTO.getValue().equals(homekitmode)) {
                changeMode("AUTO");
            } else if (HomekitMode.HEAT.getValue().equals(homekitmode)) {
                changeMode("HEAT");
            } else if (HomekitMode.COOL.getValue().equals(homekitmode)) {
                changeMode("COLD");
            }
        }
    }
}
