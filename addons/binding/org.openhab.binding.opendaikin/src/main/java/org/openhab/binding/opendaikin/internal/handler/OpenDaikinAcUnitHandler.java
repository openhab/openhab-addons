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
package org.openhab.binding.opendaikin.internal.handler;

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
import org.openhab.binding.opendaikin.internal.OpenDaikinBindingConstants;
import org.openhab.binding.opendaikin.internal.OpenDaikinCommunicationException;
import org.openhab.binding.opendaikin.internal.OpenDaikinWebTargets;
import org.openhab.binding.opendaikin.internal.api.ControlInfo;
import org.openhab.binding.opendaikin.internal.api.Enums.FanMovement;
import org.openhab.binding.opendaikin.internal.api.Enums.FanSpeed;
import org.openhab.binding.opendaikin.internal.api.Enums.Mode;
import org.openhab.binding.opendaikin.internal.api.SensorInfo;
import org.openhab.binding.opendaikin.internal.config.OpenDaikinConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles communicating with a Daikin air conditioning unit.
 *
 * @author Tim Waterhouse - Initial Contribution
 *
 */
public class OpenDaikinAcUnitHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(OpenDaikinAcUnitHandler.class);

    private long refreshInterval;

    private OpenDaikinWebTargets webTargets;
    private ScheduledFuture<?> pollFuture;

    public OpenDaikinAcUnitHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            handleCommandInternal(channelUID, command);

            poll();
        } catch (OpenDaikinCommunicationException ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
        }
    }

    private void handleCommandInternal(ChannelUID channelUID, Command command) throws OpenDaikinCommunicationException {
        switch (channelUID.getId()) {
            case OpenDaikinBindingConstants.CHANNEL_AC_POWER:
                changePower(((OnOffType) command).equals(OnOffType.ON));
                return;
            case OpenDaikinBindingConstants.CHANNEL_AC_TEMP:
                if (changeSetPoint(command)) {
                    return;
                }
            case OpenDaikinBindingConstants.CHANNEL_AC_FAN_SPEED:
                changeFanSpeed(FanSpeed.valueOf(((StringType) command).toString()));
                return;
            case OpenDaikinBindingConstants.CHANNEL_AC_FAN_DIR:
                changeFanDir(FanMovement.valueOf(((StringType) command).toString()));
                return;
            case OpenDaikinBindingConstants.CHANNEL_AC_MODE:
                changeMode(Mode.valueOf(((StringType) command).toString()));
                return;
        }

        logger.warn("Received command of wrong type for thing '{}' on channel {}", thing.getUID().getAsString(),
                channelUID.getId());
    }

    @Override
    public void initialize() {
        logger.debug("Initializing OpenDaikin AC Unit");
        OpenDaikinConfiguration config = getConfigAs(OpenDaikinConfiguration.class);
        if (config.host == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Host address must be set");
        } else {
            webTargets = new OpenDaikinWebTargets(config.host);
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
            logger.debug("Could not connect to Daikin controller", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (RuntimeException e) {
            logger.warn("Unexpected error connecting to Daikin controller", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void pollStatus() throws IOException {
        ControlInfo controlInfo = webTargets.getControlInfo();
        updateStatus(ThingStatus.ONLINE);
        if (controlInfo != null) {
            updateState(OpenDaikinBindingConstants.CHANNEL_AC_POWER, controlInfo.power ? OnOffType.ON : OnOffType.OFF);
            updateTemperatureChannel(OpenDaikinBindingConstants.CHANNEL_AC_TEMP, controlInfo.temp);

            updateState(OpenDaikinBindingConstants.CHANNEL_AC_MODE, new StringType(controlInfo.mode.name()));
            updateState(OpenDaikinBindingConstants.CHANNEL_AC_FAN_SPEED, new StringType(controlInfo.fanSpeed.name()));
            updateState(OpenDaikinBindingConstants.CHANNEL_AC_FAN_DIR, new StringType(controlInfo.fanMovement.name()));
        }

        SensorInfo sensorInfo = webTargets.getSensorInfo();
        if (sensorInfo != null) {
            updateTemperatureChannel(OpenDaikinBindingConstants.CHANNEL_INDOOR_TEMP, sensorInfo.indoortemp);

            updateTemperatureChannel(OpenDaikinBindingConstants.CHANNEL_OUTDOOR_TEMP, sensorInfo.outdoortemp);

            if (sensorInfo.indoorhumidity.isPresent()) {
                updateState(OpenDaikinBindingConstants.CHANNEL_HUMIDITY,
                        new DecimalType(sensorInfo.indoorhumidity.get()));
            } else {
                updateState(OpenDaikinBindingConstants.CHANNEL_HUMIDITY, UnDefType.UNDEF);
            }
        }
    }

    private void updateTemperatureChannel(String channel, Optional<Double> maybeTemperature) {
        updateState(channel,
                maybeTemperature.<State> map(t -> new QuantityType<Temperature>(new DecimalType(t), SIUnits.CELSIUS))
                        .orElse(UnDefType.UNDEF));
    }

    private void changePower(boolean power) throws OpenDaikinCommunicationException {
        ControlInfo info = webTargets.getControlInfo();
        info.power = power;
        webTargets.setControlInfo(info);
    }

    /**
     * @return true if the command was of an expected type, false otherwise
     */
    private boolean changeSetPoint(Command command) throws OpenDaikinCommunicationException {
        double newTemperature;
        if (command instanceof DecimalType) {
            newTemperature = ((DecimalType) command).doubleValue();
        } else if (command instanceof QuantityType) {
            newTemperature = ((QuantityType<Temperature>) command).toUnit(SIUnits.CELSIUS).doubleValue();
        } else {
            return false;
        }

        ControlInfo info = webTargets.getControlInfo();
        info.temp = Optional.of(newTemperature);
        webTargets.setControlInfo(info);

        return true;
    }

    private void changeMode(Mode mode) throws OpenDaikinCommunicationException {
        ControlInfo info = webTargets.getControlInfo();
        info.mode = mode;
        webTargets.setControlInfo(info);
    }

    private void changeFanSpeed(FanSpeed fanSpeed) throws OpenDaikinCommunicationException {
        ControlInfo info = webTargets.getControlInfo();
        info.fanSpeed = fanSpeed;
        webTargets.setControlInfo(info);
    }

    private void changeFanDir(FanMovement fanDir) throws OpenDaikinCommunicationException {
        ControlInfo info = webTargets.getControlInfo();
        info.fanMovement = fanDir;
        webTargets.setControlInfo(info);
    }
}
