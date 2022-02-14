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
package org.openhab.binding.daikin.internal.handler;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.daikin.internal.DaikinBindingConstants;
import org.openhab.binding.daikin.internal.DaikinCommunicationException;
import org.openhab.binding.daikin.internal.DaikinDynamicStateDescriptionProvider;
import org.openhab.binding.daikin.internal.DaikinWebTargets;
import org.openhab.binding.daikin.internal.api.ControlInfo;
import org.openhab.binding.daikin.internal.api.EnergyInfoDayAndWeek;
import org.openhab.binding.daikin.internal.api.EnergyInfoYear;
import org.openhab.binding.daikin.internal.api.Enums.FanMovement;
import org.openhab.binding.daikin.internal.api.Enums.FanSpeed;
import org.openhab.binding.daikin.internal.api.Enums.HomekitMode;
import org.openhab.binding.daikin.internal.api.Enums.Mode;
import org.openhab.binding.daikin.internal.api.Enums.SpecialModeKind;
import org.openhab.binding.daikin.internal.api.SensorInfo;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles communicating with a Daikin air conditioning unit.
 *
 * @author Tim Waterhouse - Initial Contribution
 * @author Paul Smedley - Modifications to support Airbase Controllers
 * @author Lukas Agethen - Added support for Energy Year reading, compressor frequency and powerful mode
 * @author Wouter Denayer - Added to support for weekly & daily energy reading
 *
 */
@NonNullByDefault
public class DaikinAcUnitHandler extends DaikinBaseHandler {
    private final Logger logger = LoggerFactory.getLogger(DaikinAcUnitHandler.class);

    public DaikinAcUnitHandler(Thing thing, DaikinDynamicStateDescriptionProvider stateDescriptionProvider,
            @Nullable HttpClient httpClient) {
        super(thing, stateDescriptionProvider, httpClient);
    }

    @Override
    protected void pollStatus() throws IOException {
        DaikinWebTargets webTargets = this.webTargets;
        if (webTargets == null) {
            return;
        }
        ControlInfo controlInfo = webTargets.getControlInfo();
        updateState(DaikinBindingConstants.CHANNEL_AC_POWER, controlInfo.power ? OnOffType.ON : OnOffType.OFF);
        updateTemperatureChannel(DaikinBindingConstants.CHANNEL_AC_TEMP, controlInfo.temp);

        updateState(DaikinBindingConstants.CHANNEL_AC_MODE, new StringType(controlInfo.mode.name()));
        updateState(DaikinBindingConstants.CHANNEL_AC_FAN_SPEED, new StringType(controlInfo.fanSpeed.name()));
        updateState(DaikinBindingConstants.CHANNEL_AC_FAN_DIR, new StringType(controlInfo.fanMovement.name()));

        if (!controlInfo.power) {
            updateState(DaikinBindingConstants.CHANNEL_AC_HOMEKITMODE, new StringType(HomekitMode.OFF.getValue()));
        } else if (controlInfo.mode == Mode.COLD) {
            updateState(DaikinBindingConstants.CHANNEL_AC_HOMEKITMODE, new StringType(HomekitMode.COOL.getValue()));
        } else if (controlInfo.mode == Mode.HEAT) {
            updateState(DaikinBindingConstants.CHANNEL_AC_HOMEKITMODE, new StringType(HomekitMode.HEAT.getValue()));
        } else if (controlInfo.mode == Mode.AUTO) {
            updateState(DaikinBindingConstants.CHANNEL_AC_HOMEKITMODE, new StringType(HomekitMode.AUTO.getValue()));
        }

        updateState(DaikinBindingConstants.CHANNEL_AC_SPECIALMODE, new StringType(controlInfo.specialMode.name()));

        if (controlInfo.specialMode.isUndefined()) {
            updateState(DaikinBindingConstants.CHANNEL_AC_SPECIALMODE_POWERFUL, UnDefType.UNDEF);
        } else {
            updateState(DaikinBindingConstants.CHANNEL_AC_SPECIALMODE_POWERFUL,
                    controlInfo.specialMode.isPowerfulActive() ? OnOffType.ON : OnOffType.OFF);
        }

        SensorInfo sensorInfo = webTargets.getSensorInfo();
        updateTemperatureChannel(DaikinBindingConstants.CHANNEL_INDOOR_TEMP, sensorInfo.indoortemp);

        updateTemperatureChannel(DaikinBindingConstants.CHANNEL_OUTDOOR_TEMP, sensorInfo.outdoortemp);

        if (sensorInfo.indoorhumidity.isPresent()) {
            updateState(DaikinBindingConstants.CHANNEL_HUMIDITY,
                    new QuantityType<>(sensorInfo.indoorhumidity.get(), Units.PERCENT));
        } else {
            updateState(DaikinBindingConstants.CHANNEL_HUMIDITY, UnDefType.UNDEF);
        }

        if (sensorInfo.compressorfrequency.isPresent()) {
            updateState(DaikinBindingConstants.CHANNEL_CMP_FREQ,
                    new QuantityType<>(sensorInfo.compressorfrequency.get(), Units.PERCENT));
        } else {
            updateState(DaikinBindingConstants.CHANNEL_CMP_FREQ, UnDefType.UNDEF);
        }

        try {
            EnergyInfoYear energyInfoYear = webTargets.getEnergyInfoYear();

            if (energyInfoYear.energyHeatingThisYear.isPresent()) {
                updateEnergyYearChannel(DaikinBindingConstants.CHANNEL_ENERGY_HEATING_CURRENTYEAR,
                        energyInfoYear.energyHeatingThisYear);
            }
            if (energyInfoYear.energyCoolingThisYear.isPresent()) {
                updateEnergyYearChannel(DaikinBindingConstants.CHANNEL_ENERGY_COOLING_CURRENTYEAR,
                        energyInfoYear.energyCoolingThisYear);
            }
        } catch (DaikinCommunicationException e) {
            // Suppress any error if energy info is not supported.
            logger.debug("getEnergyInfoYear() error: {}", e.getMessage());
        }

        try {
            EnergyInfoDayAndWeek energyInfoDayAndWeek = webTargets.getEnergyInfoDayAndWeek();

            updateEnergyDayAndWeekChannel(DaikinBindingConstants.CHANNEL_ENERGY_HEATING_TODAY,
                    energyInfoDayAndWeek.energyHeatingToday);
            updateEnergyDayAndWeekChannel(DaikinBindingConstants.CHANNEL_ENERGY_HEATING_THISWEEK,
                    energyInfoDayAndWeek.energyHeatingThisWeek);
            updateEnergyDayAndWeekChannel(DaikinBindingConstants.CHANNEL_ENERGY_HEATING_LASTWEEK,
                    energyInfoDayAndWeek.energyHeatingLastWeek);
            updateEnergyDayAndWeekChannel(DaikinBindingConstants.CHANNEL_ENERGY_COOLING_TODAY,
                    energyInfoDayAndWeek.energyCoolingToday);
            updateEnergyDayAndWeekChannel(DaikinBindingConstants.CHANNEL_ENERGY_COOLING_THISWEEK,
                    energyInfoDayAndWeek.energyCoolingThisWeek);
            updateEnergyDayAndWeekChannel(DaikinBindingConstants.CHANNEL_ENERGY_COOLING_LASTWEEK,
                    energyInfoDayAndWeek.energyCoolingLastWeek);
        } catch (DaikinCommunicationException e) {
            // Suppress any error if energy info is not supported.
            logger.debug("getEnergyInfoDayAndWeek() error: {}", e.getMessage());
        }
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    protected boolean handleCommandInternal(ChannelUID channelUID, Command command)
            throws DaikinCommunicationException {
        switch (channelUID.getId()) {
            case DaikinBindingConstants.CHANNEL_AC_FAN_DIR:
                if (command instanceof StringType) {
                    changeFanDir(((StringType) command).toString());
                    return true;
                }
                break;
            case DaikinBindingConstants.CHANNEL_AC_SPECIALMODE_POWERFUL:
                if (command instanceof OnOffType) {
                    changeSpecialModePowerful(((OnOffType) command).equals(OnOffType.ON));
                    return true;
                }
                break;
        }
        return false;
    }

    @Override
    protected void changePower(boolean power) throws DaikinCommunicationException {
        DaikinWebTargets webTargets = this.webTargets;
        if (webTargets == null) {
            return;
        }
        ControlInfo info = webTargets.getControlInfo();
        info.power = power;
        webTargets.setControlInfo(info);
    }

    @Override
    protected void changeSetPoint(double newTemperature) throws DaikinCommunicationException {
        DaikinWebTargets webTargets = this.webTargets;
        if (webTargets == null) {
            return;
        }
        ControlInfo info = webTargets.getControlInfo();
        info.temp = Optional.of(newTemperature);
        webTargets.setControlInfo(info);
    }

    @Override
    protected void changeMode(String mode) throws DaikinCommunicationException {
        DaikinWebTargets webTargets = this.webTargets;
        if (webTargets == null) {
            return;
        }
        Mode newMode;
        try {
            newMode = Mode.valueOf(mode);
        } catch (IllegalArgumentException ex) {
            logger.warn("Invalid mode: {}. Valid values: {}", mode, Mode.values());
            return;
        }
        ControlInfo info = webTargets.getControlInfo();
        info.mode = newMode;
        webTargets.setControlInfo(info);
    }

    @Override
    protected void changeFanSpeed(String fanSpeed) throws DaikinCommunicationException {
        DaikinWebTargets webTargets = this.webTargets;
        if (webTargets == null) {
            return;
        }
        FanSpeed newSpeed;
        try {
            newSpeed = FanSpeed.valueOf(fanSpeed);
        } catch (IllegalArgumentException ex) {
            logger.warn("Invalid fan speed: {}. Valid values: {}", fanSpeed, FanSpeed.values());
            return;
        }
        ControlInfo info = webTargets.getControlInfo();
        info.fanSpeed = newSpeed;
        webTargets.setControlInfo(info);
    }

    protected void changeFanDir(String fanDir) throws DaikinCommunicationException {
        DaikinWebTargets webTargets = this.webTargets;
        if (webTargets == null) {
            return;
        }
        FanMovement newMovement;
        try {
            newMovement = FanMovement.valueOf(fanDir);
        } catch (IllegalArgumentException ex) {
            logger.warn("Invalid fan direction: {}. Valid values: {}", fanDir, FanMovement.values());
            return;
        }
        ControlInfo info = webTargets.getControlInfo();
        info.fanMovement = newMovement;
        webTargets.setControlInfo(info);
    }

    /**
     *
     * @param powerfulMode
     * @return Is change successful
     * @throws DaikinCommunicationException
     */
    protected boolean changeSpecialModePowerful(boolean powerfulMode) throws DaikinCommunicationException {
        DaikinWebTargets webTargets = this.webTargets;
        if (webTargets == null) {
            return false;
        }
        return webTargets.setSpecialMode(SpecialModeKind.POWERFUL, powerfulMode);
    }

    /**
     * Updates energy year channels. Values are provided in hundreds of Watt
     *
     * @param channel
     * @param maybePower
     */
    protected void updateEnergyYearChannel(String channel, Optional<Integer[]> maybePower) {
        IntStream.range(1, 13).forEach(i -> updateState(
                String.format(DaikinBindingConstants.CHANNEL_ENERGY_STRING_FORMAT, channel, i),
                maybePower.<State> map(
                        t -> new QuantityType<>(BigDecimal.valueOf(t[i - 1].longValue(), 1), Units.KILOWATT_HOUR))
                        .orElse(UnDefType.UNDEF))

        );
    }

    /**
     *
     * @param channel
     * @param maybePower
     */
    protected void updateEnergyDayAndWeekChannel(String channel, Optional<Double> maybePower) {
        if (maybePower.isPresent()) {
            updateState(channel,
                    maybePower.<State> map(t -> new QuantityType<>(new DecimalType(t), Units.KILOWATT_HOUR))
                            .orElse(UnDefType.UNDEF));
        }
    }

    @Override
    protected void registerUuid(@Nullable String key) {
        if (key == null) {
            return;
        }
        try {
            DaikinWebTargets webTargets = this.webTargets;
            if (webTargets == null) {
                return;
            }
            webTargets.registerUuid(key);
        } catch (DaikinCommunicationException e) {
            // suppress exceptions
            logger.debug("registerUuid({}) error: {}", key, e.getMessage());
        }
    }
}
