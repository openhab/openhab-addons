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
package org.openhab.binding.daikinairbase.internal.handler;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Temperature;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.daikinairbase.internal.DaikinAirbaseBindingConstants;
import org.openhab.binding.daikinairbase.internal.DaikinAirbaseCommunicationException;
import org.openhab.binding.daikinairbase.internal.DaikinAirbaseWebTargets;
import org.openhab.binding.daikinairbase.internal.api.ControlInfo;
import org.openhab.binding.daikinairbase.internal.api.Enums.FanMovement;
import org.openhab.binding.daikinairbase.internal.api.Enums.FanSpeed;
import org.openhab.binding.daikinairbase.internal.api.Enums.Mode;
import org.openhab.binding.daikinairbase.internal.api.SensorInfo;
import org.openhab.binding.daikinairbase.internal.config.DaikinAirbaseConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles communicating with a Daikin Airbase air conditioning unit.
 *
 * @author Tim Waterhouse - Initial Contribution
 *
 */
public class DaikinAirbaseAcUnitHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(DaikinAirbaseAcUnitHandler.class);

    private long refreshInterval;

    private DaikinAirbaseWebTargets webTargets;
    private ScheduledFuture<?> pollFuture;

    public DaikinAirbaseAcUnitHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            handleCommandInternal(channelUID, command);

            poll();
        } catch (DaikinAirbaseCommunicationException ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
        }
    }

    private void handleCommandInternal(ChannelUID channelUID, Command command) throws DaikinAirbaseCommunicationException {
        switch (channelUID.getId()) {
            case DaikinAirbaseBindingConstants.CHANNEL_AC_POWER:
                if (command instanceof OnOffType) {
                    changePower(((OnOffType) command).equals(OnOffType.ON));
                    return;
                }
                break;
            case DaikinAirbaseBindingConstants.CHANNEL_AC_TEMP:
                if (changeSetPoint(command)) {
                    return;
                }
                break;
            case DaikinAirbaseBindingConstants.CHANNEL_AC_FAN_SPEED:
                if (command instanceof StringType) {
                    changeFanSpeed(FanSpeed.valueOf(((StringType) command).toString()));
                    return;
                }
                break;
            case DaikinAirbaseBindingConstants.CHANNEL_AC_MODE:
                if (command instanceof StringType) {
                    changeMode(Mode.valueOf(((StringType) command).toString()));
                    return;
                }
                break;
        }

        logger.warn("Received command of wrong type for thing '{}' on channel {}", thing.getUID().getAsString(),
                channelUID.getId());
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Daikin Airbase AC Unit");
        DaikinAirbaseConfiguration config = getConfigAs(DaikinAirbaseConfiguration.class);
        if (config.host == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Host address must be set");
        } else {
            webTargets = new DaikinAirbaseWebTargets(config.host);
            refreshInterval = config.refresh;

            schedulePoll();
        }
    }

    @Override
    public void handleRemoval() {
        super.handleRemoval();
        stopPoll();
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
        logger.debug("Scheduling poll for 500ms out, then every {} s", refreshInterval);
        pollFuture = scheduler.scheduleWithFixedDelay(this::poll, 1, refreshInterval, TimeUnit.SECONDS);
    }

    private synchronized void stopPoll() {
        if (pollFuture != null && !pollFuture.isCancelled()) {
            pollFuture.cancel(true);
            pollFuture = null;
        }
    }

    private synchronized void poll() {
        try {
            logger.debug("Polling for state");
            pollStatus();
        } catch (IOException e) {
            logger.debug("Could not connect to Daikin Airbase controller", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (RuntimeException e) {
            logger.warn("Unexpected error connecting to Daikin Airbase controller", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void pollStatus() throws IOException {
        ControlInfo controlInfo = webTargets.getControlInfo();
        updateStatus(ThingStatus.ONLINE);
        if (controlInfo != null) {
            updateState(DaikinAirbaseBindingConstants.CHANNEL_AC_POWER, controlInfo.power ? OnOffType.ON : OnOffType.OFF);
            updateTemperatureChannel(DaikinAirbaseBindingConstants.CHANNEL_AC_TEMP, controlInfo.temp);

            updateState(DaikinAirbaseBindingConstants.CHANNEL_AC_MODE, new StringType(controlInfo.mode.name()));
            updateState(DaikinAirbaseBindingConstants.CHANNEL_AC_FAN_SPEED, new StringType(controlInfo.fanSpeed.name()));
        }

        SensorInfo sensorInfo = webTargets.getSensorInfo();
        if (sensorInfo != null) {
            updateTemperatureChannel(DaikinAirbaseBindingConstants.CHANNEL_INDOOR_TEMP, sensorInfo.indoortemp);

            updateTemperatureChannel(DaikinAirbaseBindingConstants.CHANNEL_OUTDOOR_TEMP, sensorInfo.outdoortemp);
        }
    }

    private void updateTemperatureChannel(String channel, Optional<Double> maybeTemperature) {
        updateState(channel,
                maybeTemperature.<State> map(t -> new QuantityType<Temperature>(new DecimalType(t), SIUnits.CELSIUS))
                        .orElse(UnDefType.UNDEF));
    }

    private void changePower(boolean power) throws DaikinAirbaseCommunicationException {
        ControlInfo info = webTargets.getControlInfo();
        info.power = power;
        webTargets.setControlInfo(info);
    }

    /**
     * @return true if the command was of an expected type, false otherwise
     */
    private boolean changeSetPoint(Command command) throws DaikinAirbaseCommunicationException {
        double newTemperature;
        if (command instanceof DecimalType) {
            newTemperature = ((DecimalType) command).doubleValue();
        } else if (command instanceof QuantityType) {
            newTemperature = ((QuantityType<Temperature>) command).toUnit(SIUnits.CELSIUS).doubleValue();
        } else {
            return false;
        }

        // Only half degree increments are allowed, all others are silently rejected by the A/C units
        newTemperature = Math.round(newTemperature * 2) / 2.0;

        ControlInfo info = webTargets.getControlInfo();
        info.temp = Optional.of(newTemperature);
        webTargets.setControlInfo(info);

        return true;
    }

    private void changeMode(Mode mode) throws DaikinAirbaseCommunicationException {
        ControlInfo info = webTargets.getControlInfo();
        info.mode = mode;
        webTargets.setControlInfo(info);
    }

    private void changeFanSpeed(FanSpeed fanSpeed) throws DaikinAirbaseCommunicationException {
        ControlInfo info = webTargets.getControlInfo();
        info.fanSpeed = fanSpeed;
        webTargets.setControlInfo(info);
    }
}
