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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.daikin.internal.DaikinBindingConstants;
import org.openhab.binding.daikin.internal.DaikinCommunicationException;
import org.openhab.binding.daikin.internal.DaikinDynamicStateDescriptionProvider;
import org.openhab.binding.daikin.internal.api.Enums.HomekitMode;
import org.openhab.binding.daikin.internal.api.SensorInfo;
import org.openhab.binding.daikin.internal.api.airbase.AirbaseControlInfo;
import org.openhab.binding.daikin.internal.api.airbase.AirbaseEnums.AirbaseFanSpeed;
import org.openhab.binding.daikin.internal.api.airbase.AirbaseEnums.AirbaseFeature;
import org.openhab.binding.daikin.internal.api.airbase.AirbaseEnums.AirbaseMode;
import org.openhab.binding.daikin.internal.api.airbase.AirbaseModelInfo;
import org.openhab.binding.daikin.internal.api.airbase.AirbaseZoneInfo;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles communicating with a Daikin Airbase wifi adapter.
 *
 * @author Tim Waterhouse - Initial Contribution
 * @author Paul Smedley - Modifications to support Airbase Controllers
 * @author Jimmy Tanagra - Support Airside and auto fan levels, DynamicStateDescription
 *
 */
@NonNullByDefault
public class DaikinAirbaseUnitHandler extends DaikinBaseHandler {
    private final Logger logger = LoggerFactory.getLogger(DaikinAirbaseUnitHandler.class);
    private @Nullable AirbaseModelInfo airbaseModelInfo;

    public DaikinAirbaseUnitHandler(Thing thing, DaikinDynamicStateDescriptionProvider stateDescriptionProvider,
            @Nullable HttpClient httpClient) {
        super(thing, stateDescriptionProvider, httpClient);
    }

    @Override
    protected boolean handleCommandInternal(ChannelUID channelUID, Command command)
            throws DaikinCommunicationException {
        if (channelUID.getId().startsWith(DaikinBindingConstants.CHANNEL_AIRBASE_AC_ZONE)) {
            int zoneNumber = Integer.parseInt(channelUID.getId().substring(4));
            if (command instanceof OnOffType onOffCommand) {
                if (changeZone(zoneNumber, onOffCommand == OnOffType.ON)) {
                    updateState(channelUID, onOffCommand);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    protected void pollStatus() throws DaikinCommunicationException {
        if (airbaseModelInfo == null || !"OK".equals(airbaseModelInfo.ret)) {
            airbaseModelInfo = webTargets.getAirbaseModelInfo();
            updateChannelStateDescriptions();
        }

        AirbaseControlInfo controlInfo = webTargets.getAirbaseControlInfo();
        if (!"OK".equals(controlInfo.ret)) {
            throw new DaikinCommunicationException("Invalid response from host");
        }

        updateState(DaikinBindingConstants.CHANNEL_AC_POWER, OnOffType.from(controlInfo.power));
        updateTemperatureChannel(DaikinBindingConstants.CHANNEL_AC_TEMP, controlInfo.temp);
        updateState(DaikinBindingConstants.CHANNEL_AC_MODE, new StringType(controlInfo.mode.name()));
        updateState(DaikinBindingConstants.CHANNEL_AIRBASE_AC_FAN_SPEED, new StringType(controlInfo.fanSpeed.name()));

        if (!controlInfo.power) {
            updateState(DaikinBindingConstants.CHANNEL_AC_HOMEKITMODE, new StringType(HomekitMode.OFF.getValue()));
        } else if (controlInfo.mode == AirbaseMode.COLD) {
            updateState(DaikinBindingConstants.CHANNEL_AC_HOMEKITMODE, new StringType(HomekitMode.COOL.getValue()));
        } else if (controlInfo.mode == AirbaseMode.HEAT) {
            updateState(DaikinBindingConstants.CHANNEL_AC_HOMEKITMODE, new StringType(HomekitMode.HEAT.getValue()));
        } else if (controlInfo.mode == AirbaseMode.AUTO) {
            updateState(DaikinBindingConstants.CHANNEL_AC_HOMEKITMODE, new StringType(HomekitMode.AUTO.getValue()));
        }

        SensorInfo sensorInfo = webTargets.getAirbaseSensorInfo();
        updateTemperatureChannel(DaikinBindingConstants.CHANNEL_INDOOR_TEMP, sensorInfo.indoortemp);
        updateTemperatureChannel(DaikinBindingConstants.CHANNEL_OUTDOOR_TEMP, sensorInfo.outdoortemp);

        AirbaseZoneInfo zoneInfo = webTargets.getAirbaseZoneInfo();
        IntStream.range(0, zoneInfo.zone.length)
                .forEach(idx -> updateState(DaikinBindingConstants.CHANNEL_AIRBASE_AC_ZONE + (idx + 1),
                        OnOffType.from(zoneInfo.zone[idx])));
    }

    @Override
    protected boolean changePower(boolean power) throws DaikinCommunicationException {
        AirbaseControlInfo info = webTargets.getAirbaseControlInfo();
        info.power = power;
        return webTargets.setAirbaseControlInfo(info);
    }

    @Override
    protected boolean changeSetPoint(double newTemperature) throws DaikinCommunicationException {
        AirbaseControlInfo info = webTargets.getAirbaseControlInfo();
        info.temp = Optional.of(newTemperature);
        return webTargets.setAirbaseControlInfo(info);
    }

    @Override
    protected boolean changeMode(String mode) throws DaikinCommunicationException {
        AirbaseMode newMode;
        try {
            newMode = AirbaseMode.valueOf(mode);
        } catch (IllegalArgumentException ex) {
            logger.warn("Invalid mode: {}. Valid values: {}", mode, AirbaseMode.values());
            return false;
        }
        if (airbaseModelInfo != null) {
            if ((newMode == AirbaseMode.AUTO && !airbaseModelInfo.features.contains(AirbaseFeature.AUTO))
                    || (newMode == AirbaseMode.DRY && !airbaseModelInfo.features.contains(AirbaseFeature.DRY))) {
                logger.warn("{} mode is not supported by your controller", mode);
                return false;
            }
        }
        AirbaseControlInfo info = webTargets.getAirbaseControlInfo();
        info.mode = newMode;
        return webTargets.setAirbaseControlInfo(info);
    }

    @Override
    protected boolean changeFanSpeed(String speed) throws DaikinCommunicationException {
        AirbaseFanSpeed newFanSpeed;
        try {
            newFanSpeed = AirbaseFanSpeed.valueOf(speed);
        } catch (IllegalArgumentException ex) {
            logger.warn("Invalid fan speed: {}. Valid values: {}", speed, AirbaseFanSpeed.values());
            return false;
        }
        if (airbaseModelInfo != null) {
            if (EnumSet.range(AirbaseFanSpeed.AUTO_LEVEL_1, AirbaseFanSpeed.AUTO_LEVEL_5).contains(newFanSpeed)
                    && !airbaseModelInfo.features.contains(AirbaseFeature.FRATE_AUTO)) {
                logger.warn("Fan AUTO_LEVEL_X is not supported by your controller");
                return false;
            }
            if (newFanSpeed == AirbaseFanSpeed.AIRSIDE && !airbaseModelInfo.features.contains(AirbaseFeature.AIRSIDE)) {
                logger.warn("Airside is not supported by your controller");
                return false;
            }
        }
        AirbaseControlInfo info = webTargets.getAirbaseControlInfo();
        info.fanSpeed = newFanSpeed;
        return webTargets.setAirbaseControlInfo(info);
    }

    /**
     * 
     * Turn the zone on/off
     * The Airbase controller allows turning off all zones, so we allow it here too
     * 
     * @param zone the zone number starting from 1
     * @param command true to turn on the zone, false to turn it off
     * 
     */
    protected boolean changeZone(int zone, boolean command) throws DaikinCommunicationException {
        AirbaseZoneInfo zoneInfo = webTargets.getAirbaseZoneInfo();
        long maxZones = zoneInfo.zone.length;

        if (airbaseModelInfo != null) {
            maxZones = Math.min(maxZones, airbaseModelInfo.zonespresent);
        }
        if (zone <= 0 || zone > maxZones) {
            logger.warn("The given zone number ({}) is outside the number of zones supported by the controller ({})",
                    zone, maxZones);
            return false;
        }

        zoneInfo.zone[zone - 1] = command;
        return webTargets.setAirbaseZoneInfo(zoneInfo);
    }

    @Override
    protected void registerUuid(@Nullable String key) {
        // not implemented. There is currently no known Airbase adapter that requires uuid authentication
    }

    protected void updateChannelStateDescriptions() {
        updateAirbaseFanSpeedChannelStateDescription();
        updateAirbaseModeChannelStateDescription();
    }

    protected void updateAirbaseFanSpeedChannelStateDescription() {
        List<StateOption> options = new ArrayList<>();
        options.add(new StateOption(AirbaseFanSpeed.AUTO.name(), AirbaseFanSpeed.AUTO.getLabel()));
        if (airbaseModelInfo.features.contains(AirbaseFeature.AIRSIDE)) {
            options.add(new StateOption(AirbaseFanSpeed.AIRSIDE.name(), AirbaseFanSpeed.AIRSIDE.getLabel()));
        }
        for (AirbaseFanSpeed f : EnumSet.range(AirbaseFanSpeed.LEVEL_1, AirbaseFanSpeed.LEVEL_5)) {
            options.add(new StateOption(f.name(), f.getLabel()));
        }
        if (airbaseModelInfo.features.contains(AirbaseFeature.FRATE_AUTO)) {
            for (AirbaseFanSpeed f : EnumSet.range(AirbaseFanSpeed.AUTO_LEVEL_1, AirbaseFanSpeed.AUTO_LEVEL_5)) {
                options.add(new StateOption(f.name(), f.getLabel()));
            }
        }
        stateDescriptionProvider.setStateOptions(
                new ChannelUID(thing.getUID(), DaikinBindingConstants.CHANNEL_AIRBASE_AC_FAN_SPEED), options);
    }

    protected void updateAirbaseModeChannelStateDescription() {
        List<StateOption> options = new ArrayList<>();
        if (airbaseModelInfo.features.contains(AirbaseFeature.AUTO)) {
            options.add(new StateOption(AirbaseMode.AUTO.name(), AirbaseMode.AUTO.getLabel()));
        }
        for (AirbaseMode f : EnumSet.complementOf(EnumSet.of(AirbaseMode.AUTO, AirbaseMode.DRY))) {
            options.add(new StateOption(f.name(), f.getLabel()));
        }
        if (airbaseModelInfo.features.contains(AirbaseFeature.DRY)) {
            options.add(new StateOption(AirbaseMode.DRY.name(), AirbaseMode.DRY.getLabel()));
        }
        stateDescriptionProvider.setStateOptions(new ChannelUID(thing.getUID(), DaikinBindingConstants.CHANNEL_AC_MODE),
                options);
    }
}
