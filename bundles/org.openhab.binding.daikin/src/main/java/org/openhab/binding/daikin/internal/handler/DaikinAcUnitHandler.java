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
package org.openhab.binding.daikin.internal.handler;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import javax.measure.quantity.Temperature;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.daikin.internal.DaikinBindingConstants;
import org.openhab.binding.daikin.internal.DaikinCommunicationException;
import org.openhab.binding.daikin.internal.DaikinWebTargets;
import org.openhab.binding.daikin.internal.api.ControlInfo;
import org.openhab.binding.daikin.internal.api.Enums.FanMovement;
import org.openhab.binding.daikin.internal.api.Enums.FanSpeed;
import org.openhab.binding.daikin.internal.api.Enums.Mode;
import org.openhab.binding.daikin.internal.api.SensorInfo;
import org.openhab.binding.daikin.internal.api.airbase.AirbaseEnums.AirbaseFanSpeed;
import org.openhab.binding.daikin.internal.api.airbase.AirbaseEnums.AirbaseMode;
import org.openhab.binding.daikin.internal.api.airbase.AirbaseControlInfo;
import org.openhab.binding.daikin.internal.api.airbase.AirbaseModelInfo;
import org.openhab.binding.daikin.internal.api.airbase.AirbaseZoneInfo;

import org.openhab.binding.daikin.internal.config.DaikinConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles communicating with a Daikin air conditioning unit.
 *
 * @author Tim Waterhouse - Initial Contribution
 * @author Paul Smedley <paul@smedley.id.au> - Modifications to support Airbase Controllers
 *
 */
public class DaikinAcUnitHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(DaikinAcUnitHandler.class);

    private long refreshInterval;

    private DaikinWebTargets webTargets;
    private ScheduledFuture<?> pollFuture;

    public DaikinAcUnitHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            handleCommandInternal(channelUID, command);

            poll();
        } catch (DaikinCommunicationException ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
        }
    }

    private void handleCommandInternal(ChannelUID channelUID, Command command) throws DaikinCommunicationException {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        String channelId = channelUID.getId();

        switch (channelUID.getId()) {
            case DaikinBindingConstants.CHANNEL_AC_POWER:
                if (command instanceof OnOffType) {
                    changePower(((OnOffType) command).equals(OnOffType.ON));
                    return;
                }
                break;
            case DaikinBindingConstants.CHANNEL_AC_TEMP:
                if (changeSetPoint(command)) {
                    return;
                }
                break;
            case DaikinBindingConstants.CHANNEL_AIRBASE_AC_FAN_SPEED:
                if (command instanceof StringType) {
                    changeAirbaseFanSpeed(AirbaseFanSpeed.valueOf(((StringType) command).toString()));
                    return;
                }
                break;
            case DaikinBindingConstants.CHANNEL_AC_FAN_SPEED:
                if (command instanceof StringType) {
                    changeFanSpeed(FanSpeed.valueOf(((StringType) command).toString()));
                    return;
                }
                break;
            case DaikinBindingConstants.CHANNEL_AC_FAN_DIR:
                if (command instanceof StringType) {
                    changeFanDir(FanMovement.valueOf(((StringType) command).toString()));
                    return;
                }
                break;
            case DaikinBindingConstants.CHANNEL_AC_MODE:
                if (command instanceof StringType) {

                    if (thingTypeUID.equals(DaikinBindingConstants.THING_TYPE_AC_UNIT))
                        changeMode(Mode.valueOf(((StringType) command).toString()));
                    else
                        changeAirbaseMode(AirbaseMode.valueOf(((StringType) command).toString()));
                    return;
                }
                break;
        }
        /* additional controls for Daikin Airbase */
        if (channelId.startsWith(DaikinBindingConstants.CHANNEL_AIRBASE_AC_ZONE)) {
            int zoneNumber = Integer.parseInt(channelUID.getId().substring(4));
            if (command instanceof OnOffType) {
                 changeZone(zoneNumber, command == OnOffType.ON);
                 return;
            }
        }
        logger.warn("Received command of wrong type for thing '{}' on channel {}", thing.getUID().getAsString(),
                channelUID.getId());
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Daikin AC Unit");
        DaikinConfiguration config = getConfigAs(DaikinConfiguration.class);
        if (config.host == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Host address must be set");
        } else {
            webTargets = new DaikinWebTargets(config.host);
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
            ThingTypeUID thingTypeUID = thing.getThingTypeUID();

            if (thingTypeUID.equals(DaikinBindingConstants.THING_TYPE_AC_UNIT)) {
                pollStatus();
            } else {
                pollAirbaseStatus();
            }

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
            updateState(DaikinBindingConstants.CHANNEL_AC_POWER, controlInfo.power ? OnOffType.ON : OnOffType.OFF);
            updateTemperatureChannel(DaikinBindingConstants.CHANNEL_AC_TEMP, controlInfo.temp);

            updateState(DaikinBindingConstants.CHANNEL_AC_MODE, new StringType(controlInfo.mode.name()));
            updateState(DaikinBindingConstants.CHANNEL_AC_FAN_SPEED, new StringType(controlInfo.fanSpeed.name()));
            updateState(DaikinBindingConstants.CHANNEL_AC_FAN_DIR, new StringType(controlInfo.fanMovement.name()));
        }

        SensorInfo sensorInfo = webTargets.getSensorInfo();
        if (sensorInfo != null) {
            updateTemperatureChannel(DaikinBindingConstants.CHANNEL_INDOOR_TEMP, sensorInfo.indoortemp);

            updateTemperatureChannel(DaikinBindingConstants.CHANNEL_OUTDOOR_TEMP, sensorInfo.outdoortemp);

            if (sensorInfo.indoorhumidity.isPresent()) {
                updateState(DaikinBindingConstants.CHANNEL_HUMIDITY, new DecimalType(sensorInfo.indoorhumidity.get()));
            } else {
                updateState(DaikinBindingConstants.CHANNEL_HUMIDITY, UnDefType.UNDEF);
            }
        }
    }

    private void pollAirbaseStatus() throws IOException {
        AirbaseControlInfo controlInfo = webTargets.getAirbaseControlInfo();
        updateStatus(ThingStatus.ONLINE);
        if (controlInfo != null) {
            updateState(DaikinBindingConstants.CHANNEL_AC_POWER, controlInfo.power ? OnOffType.ON : OnOffType.OFF);
            updateTemperatureChannel(DaikinBindingConstants.CHANNEL_AC_TEMP, controlInfo.temp);
            updateState(DaikinBindingConstants.CHANNEL_AC_MODE, new StringType(controlInfo.mode.name()));
            updateState(DaikinBindingConstants.CHANNEL_AIRBASE_AC_FAN_SPEED, new StringType(controlInfo.fanSpeed.name()));
        }

        SensorInfo sensorInfo = webTargets.getAirbaseSensorInfo();
        if (sensorInfo != null) {
            updateTemperatureChannel(DaikinBindingConstants.CHANNEL_INDOOR_TEMP, sensorInfo.indoortemp);

            updateTemperatureChannel(DaikinBindingConstants.CHANNEL_OUTDOOR_TEMP, sensorInfo.outdoortemp);
        }
        AirbaseZoneInfo zoneInfo = webTargets.getAirbaseZoneInfo();
        if (zoneInfo != null) {
            IntStream.range(0, zoneInfo.zone.length).forEach(
            idx -> updateState(DaikinBindingConstants.CHANNEL_AIRBASE_AC_ZONE + idx, OnOffType.from(zoneInfo.zone[idx])));
        }
    }

    private void updateTemperatureChannel(String channel, Optional<Double> maybeTemperature) {
        updateState(channel,
                maybeTemperature.<State> map(t -> new QuantityType<Temperature>(new DecimalType(t), SIUnits.CELSIUS))
                        .orElse(UnDefType.UNDEF));
    }

    private void changePower(boolean power) throws DaikinCommunicationException {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(DaikinBindingConstants.THING_TYPE_AC_UNIT)) {
           // handle Daikin
           ControlInfo info = webTargets.getControlInfo();
           info.power = power;
           webTargets.setControlInfo(info);
        } else {
            // handle Airbase
           AirbaseControlInfo info = webTargets.getAirbaseControlInfo();
           info.power = power;
           webTargets.setAirbaseControlInfo(info);

        }
    }

    /**
     * @return true if the command was of an expected type, false otherwise
     */
    private boolean changeSetPoint(Command command) throws DaikinCommunicationException {
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

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(DaikinBindingConstants.THING_TYPE_AC_UNIT)) {
            // handle Daikin
            ControlInfo info = webTargets.getControlInfo();
            info.temp = Optional.of(newTemperature);
            webTargets.setControlInfo(info);
        } else {
            // handle Airbase
            AirbaseControlInfo info = webTargets.getAirbaseControlInfo();
            info.temp = Optional.of(newTemperature);
            webTargets.setAirbaseControlInfo(info);
        }

        return true;
    }

    private void changeMode(Mode mode) throws DaikinCommunicationException {
        ControlInfo info = webTargets.getControlInfo();
        info.mode = mode;
        webTargets.setControlInfo(info);
    }

    private void changeAirbaseMode(AirbaseMode mode) throws DaikinCommunicationException {
        AirbaseControlInfo info = webTargets.getAirbaseControlInfo();
        info.mode = mode;
        webTargets.setAirbaseControlInfo(info);
    }

    private void changeFanSpeed(FanSpeed fanSpeed) throws DaikinCommunicationException {
        ControlInfo info = webTargets.getControlInfo();
        info.fanSpeed = fanSpeed;
        webTargets.setControlInfo(info);
    }

    private void changeAirbaseFanSpeed(AirbaseFanSpeed fanSpeed) throws DaikinCommunicationException {
        AirbaseControlInfo info = webTargets.getAirbaseControlInfo();
        info.fanSpeed = fanSpeed;
        webTargets.setAirbaseControlInfo(info);
    }

    // FanDir only supported on standard Daikin controllers, so don't need to detect what kind of thing we have
    private void changeFanDir(FanMovement fanDir) throws DaikinCommunicationException {
        ControlInfo info = webTargets.getControlInfo();
        info.fanMovement = fanDir;
        webTargets.setControlInfo(info);
    }

    // Zones only supported on Airbase, so don't need to detect what kind of thing we have
    private void changeZone(int zone, boolean command) throws DaikinCommunicationException {
        AirbaseZoneInfo info = webTargets.getAirbaseZoneInfo();
        AirbaseModelInfo modelInfo = webTargets.getAirbaseModelInfo();
        info.zone[zone] = command;
        if (modelInfo.zonespresent >=zone) {
            webTargets.setAirbaseZoneInfo(info, modelInfo);
        }
    }

}
