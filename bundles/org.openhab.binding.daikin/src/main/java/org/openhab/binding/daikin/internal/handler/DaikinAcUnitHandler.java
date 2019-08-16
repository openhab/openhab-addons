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
import org.openhab.binding.daikin.internal.api.airbase.AirbaseBasicInfo;
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
            /* additional controls for Daikin Airbase */
            case DaikinBindingConstants.CHANNEL_AIRBASE_AC_ZONE1:
                if (command instanceof OnOffType) {
                    changeZone1(((OnOffType) command).equals(OnOffType.ON));
                    return;
                }
                break;
            case DaikinBindingConstants.CHANNEL_AIRBASE_AC_ZONE2:
                if (command instanceof OnOffType) {
                    changeZone2(((OnOffType) command).equals(OnOffType.ON));
                    return;
                }
                break;
            case DaikinBindingConstants.CHANNEL_AIRBASE_AC_ZONE3:
                if (command instanceof OnOffType) {
                    changeZone3(((OnOffType) command).equals(OnOffType.ON));
                    return;
                }
                break;
            case DaikinBindingConstants.CHANNEL_AIRBASE_AC_ZONE4:
                if (command instanceof OnOffType) {
                    changeZone4(((OnOffType) command).equals(OnOffType.ON));
                    return;
                }
                break;
            case DaikinBindingConstants.CHANNEL_AIRBASE_AC_ZONE5:
                if (command instanceof OnOffType) {
                    changeZone5(((OnOffType) command).equals(OnOffType.ON));
                    return;
                }
                break;
            case DaikinBindingConstants.CHANNEL_AIRBASE_AC_ZONE6:
                if (command instanceof OnOffType) {
                    changeZone6(((OnOffType) command).equals(OnOffType.ON));
                    return;
                }
                break;
            case DaikinBindingConstants.CHANNEL_AIRBASE_AC_ZONE7:
                if (command instanceof OnOffType) {
                    changeZone7(((OnOffType) command).equals(OnOffType.ON));
                    return;
                }
                break;
            case DaikinBindingConstants.CHANNEL_AIRBASE_AC_ZONE8:
                if (command instanceof OnOffType) {
                    changeZone8(((OnOffType) command).equals(OnOffType.ON));
                    return;
                }
                break;
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
        AirbaseModelInfo modelInfo = webTargets.getAirbaseModelInfo();
        AirbaseZoneInfo zoneInfo = webTargets.getAirbaseZoneInfo();
        if (zoneInfo != null) {
            updateState(DaikinBindingConstants.CHANNEL_AIRBASE_AC_ZONE1, zoneInfo.zone[0] ? OnOffType.ON : OnOffType.OFF);
            updateState(DaikinBindingConstants.CHANNEL_AIRBASE_AC_ZONE2, zoneInfo.zone[1] ? OnOffType.ON : OnOffType.OFF);
            updateState(DaikinBindingConstants.CHANNEL_AIRBASE_AC_ZONE3, zoneInfo.zone[2] ? OnOffType.ON : OnOffType.OFF);
            updateState(DaikinBindingConstants.CHANNEL_AIRBASE_AC_ZONE4, zoneInfo.zone[3] ? OnOffType.ON : OnOffType.OFF);
            updateState(DaikinBindingConstants.CHANNEL_AIRBASE_AC_ZONE5, zoneInfo.zone[4] ? OnOffType.ON : OnOffType.OFF);
            updateState(DaikinBindingConstants.CHANNEL_AIRBASE_AC_ZONE6, zoneInfo.zone[5] ? OnOffType.ON : OnOffType.OFF);
            updateState(DaikinBindingConstants.CHANNEL_AIRBASE_AC_ZONE7, zoneInfo.zone[6] ? OnOffType.ON : OnOffType.OFF);
            updateState(DaikinBindingConstants.CHANNEL_AIRBASE_AC_ZONE8, zoneInfo.zone[7] ? OnOffType.ON : OnOffType.OFF);
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
    private void changeZone1(boolean zone1) throws DaikinCommunicationException {
        AirbaseZoneInfo info = webTargets.getAirbaseZoneInfo();
        AirbaseModelInfo modelInfo = webTargets.getAirbaseModelInfo();
        info.zone[0] = zone1;
        if (modelInfo.zonespresent >=1) webTargets.setAirbaseZoneInfo(info, modelInfo);
    }

    private void changeZone2(boolean zone2) throws DaikinCommunicationException {
        AirbaseZoneInfo info = webTargets.getAirbaseZoneInfo();
        AirbaseModelInfo modelInfo = webTargets.getAirbaseModelInfo();
        info.zone[1] = zone2;
        if (modelInfo.zonespresent >=2) webTargets.setAirbaseZoneInfo(info, modelInfo);
    }

    private void changeZone3(boolean zone3) throws DaikinCommunicationException {
        AirbaseZoneInfo info = webTargets.getAirbaseZoneInfo();
        AirbaseModelInfo modelInfo = webTargets.getAirbaseModelInfo();
        info.zone[2] = zone3;
        if (modelInfo.zonespresent >=3) webTargets.setAirbaseZoneInfo(info, modelInfo);
    }

    private void changeZone4(boolean zone4) throws DaikinCommunicationException {
        AirbaseZoneInfo info = webTargets.getAirbaseZoneInfo();
        AirbaseModelInfo modelInfo = webTargets.getAirbaseModelInfo();
        info.zone[3] = zone4;
        if (modelInfo.zonespresent >=4) webTargets.setAirbaseZoneInfo(info, modelInfo);
    }

    private void changeZone5(boolean zone5) throws DaikinCommunicationException {
        AirbaseZoneInfo info = webTargets.getAirbaseZoneInfo();
        AirbaseModelInfo modelInfo = webTargets.getAirbaseModelInfo();
        info.zone[4] = zone5;
        if (modelInfo.zonespresent >=5) webTargets.setAirbaseZoneInfo(info, modelInfo);
    }

    private void changeZone6(boolean zone6) throws DaikinCommunicationException {
        AirbaseZoneInfo info = webTargets.getAirbaseZoneInfo();
        AirbaseModelInfo modelInfo = webTargets.getAirbaseModelInfo();
        info.zone[5] = zone6;
        if (modelInfo.zonespresent >=6) webTargets.setAirbaseZoneInfo(info, modelInfo);
    }

    private void changeZone7(boolean zone7) throws DaikinCommunicationException {
        AirbaseZoneInfo info = webTargets.getAirbaseZoneInfo();
        AirbaseModelInfo modelInfo = webTargets.getAirbaseModelInfo();
        info.zone[6] = zone7;
        if (modelInfo.zonespresent >=7) webTargets.setAirbaseZoneInfo(info, modelInfo);
    }

    private void changeZone8(boolean zone8) throws DaikinCommunicationException {
        AirbaseZoneInfo info = webTargets.getAirbaseZoneInfo();
        AirbaseModelInfo modelInfo = webTargets.getAirbaseModelInfo();
        info.zone[7] = zone8;
        if (modelInfo.zonespresent ==8) webTargets.setAirbaseZoneInfo(info, modelInfo);
    }

}
