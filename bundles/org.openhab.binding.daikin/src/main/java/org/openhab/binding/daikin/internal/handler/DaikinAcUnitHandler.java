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

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.daikin.internal.DaikinBindingConstants;
import org.openhab.binding.daikin.internal.DaikinCommunicationException;
import org.openhab.binding.daikin.internal.DaikinDynamicStateDescriptionProvider;
import org.openhab.binding.daikin.internal.api.ControlInfo;
import org.openhab.binding.daikin.internal.api.DemandControl;
import org.openhab.binding.daikin.internal.api.EnergyInfoDayAndWeek;
import org.openhab.binding.daikin.internal.api.EnergyInfoYear;
import org.openhab.binding.daikin.internal.api.Enums.DemandControlMode;
import org.openhab.binding.daikin.internal.api.Enums.FanMovement;
import org.openhab.binding.daikin.internal.api.Enums.FanSpeed;
import org.openhab.binding.daikin.internal.api.Enums.HomekitMode;
import org.openhab.binding.daikin.internal.api.Enums.Mode;
import org.openhab.binding.daikin.internal.api.Enums.SpecialMode;
import org.openhab.binding.daikin.internal.api.SensorInfo;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * Handles communicating with a Daikin air conditioning unit.
 *
 * @author Tim Waterhouse - Initial Contribution
 * @author Paul Smedley - Modifications to support Airbase Controllers
 * @author Lukas Agethen - Added support for Energy Year reading, compressor frequency and powerful mode
 * @author Wouter Denayer - Added to support for weekly and daily energy reading
 *
 */
@NonNullByDefault
public class DaikinAcUnitHandler extends DaikinBaseHandler {
    private final Logger logger = LoggerFactory.getLogger(DaikinAcUnitHandler.class);

    private Optional<Integer> autoModeValue = Optional.empty();
    private boolean pollDemandControl = true;
    private Optional<String> savedDemandControlSchedule = Optional.empty();
    private Optional<Integer> savedDemandControlMaxPower = Optional.empty();

    public DaikinAcUnitHandler(Thing thing, DaikinDynamicStateDescriptionProvider stateDescriptionProvider,
            @Nullable HttpClient httpClient) {
        super(thing, stateDescriptionProvider, httpClient);
    }

    @Override
    protected void pollStatus() throws DaikinCommunicationException {
        ControlInfo controlInfo = webTargets.getControlInfo();
        if (!"OK".equals(controlInfo.ret)) {
            throw new DaikinCommunicationException("Invalid response from host");
        }

        updateState(DaikinBindingConstants.CHANNEL_AC_POWER, OnOffType.from(controlInfo.power));
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

        if (controlInfo.advancedMode.isUndefined()) {
            updateState(DaikinBindingConstants.CHANNEL_AC_STREAMER, UnDefType.UNDEF);
            updateState(DaikinBindingConstants.CHANNEL_AC_SPECIALMODE, UnDefType.UNDEF);
        } else {
            updateState(DaikinBindingConstants.CHANNEL_AC_STREAMER,
                    OnOffType.from(controlInfo.advancedMode.isStreamerActive()));
            updateState(DaikinBindingConstants.CHANNEL_AC_SPECIALMODE,
                    new StringType(controlInfo.getSpecialMode().name()));
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

        if (pollDemandControl) {
            try {
                DemandControl demandInfo = webTargets.getDemandControl();
                String schedule = demandInfo.getSchedule();
                int maxPower = demandInfo.maxPower;

                if (demandInfo.mode == DemandControlMode.SCHEDULED) {
                    savedDemandControlSchedule = Optional.of(schedule);
                    maxPower = demandInfo.getScheduledMaxPower();
                } else if (demandInfo.mode == DemandControlMode.MANUAL) {
                    savedDemandControlMaxPower = Optional.of(demandInfo.maxPower);
                }

                updateState(DaikinBindingConstants.CHANNEL_AC_DEMAND_MODE, new StringType(demandInfo.mode.name()));
                updateState(DaikinBindingConstants.CHANNEL_AC_DEMAND_MAX_POWER, new PercentType(maxPower));
                updateState(DaikinBindingConstants.CHANNEL_AC_DEMAND_SCHEDULE, new StringType(schedule));
            } catch (DaikinCommunicationException e) {
                // Suppress any error if demand control is not supported.
                logger.debug("getDemandControl() error: {}", e.getMessage());
                pollDemandControl = false;
            }
        }
    }

    @Override
    protected boolean handleCommandInternal(ChannelUID channelUID, Command command)
            throws DaikinCommunicationException {
        switch (channelUID.getId()) {
            case DaikinBindingConstants.CHANNEL_AC_FAN_DIR:
                if (command instanceof StringType stringCommand) {
                    if (changeFanDir(stringCommand.toString())) {
                        updateState(channelUID, stringCommand);
                    }
                    return true;
                }
                break;
            case DaikinBindingConstants.CHANNEL_AC_SPECIALMODE:
                if (command instanceof StringType stringCommand) {
                    if (changeSpecialMode(stringCommand.toString())) {
                        updateState(channelUID, stringCommand);
                    }
                    return true;
                }
                break;
            case DaikinBindingConstants.CHANNEL_AC_STREAMER:
                if (command instanceof OnOffType onOffCommand) {
                    if (changeStreamer(onOffCommand.equals(OnOffType.ON))) {
                        updateState(channelUID, onOffCommand);
                    }
                    return true;
                }
                break;
            case DaikinBindingConstants.CHANNEL_AC_DEMAND_MODE:
                if (command instanceof StringType stringCommand) {
                    changeDemandMode(stringCommand.toString());
                    return true;
                }
                break;
            case DaikinBindingConstants.CHANNEL_AC_DEMAND_MAX_POWER:
                if (command instanceof PercentType percentCommand) {
                    if (changeDemandMaxPower(percentCommand.intValue())) {
                        updateState(DaikinBindingConstants.CHANNEL_AC_DEMAND_MODE,
                                new StringType(DemandControlMode.MANUAL.name()));
                        updateState(channelUID, percentCommand);
                    }
                    return true;
                }
                break;
            case DaikinBindingConstants.CHANNEL_AC_DEMAND_SCHEDULE:
                if (command instanceof StringType stringCommand) {
                    if (changeDemandSchedule(stringCommand.toString())) {
                        updateState(DaikinBindingConstants.CHANNEL_AC_DEMAND_MODE,
                                new StringType(DemandControlMode.SCHEDULED.name()));
                        updateState(channelUID, stringCommand);
                    }
                    return true;
                }
                break;
        }
        return false;
    }

    @Override
    protected boolean changePower(boolean power) throws DaikinCommunicationException {
        ControlInfo info = webTargets.getControlInfo();
        info.power = power;
        return webTargets.setControlInfo(info);
    }

    @Override
    protected boolean changeSetPoint(double newTemperature) throws DaikinCommunicationException {
        ControlInfo info = webTargets.getControlInfo();
        info.temp = Optional.of(newTemperature);
        return webTargets.setControlInfo(info);
    }

    @Override
    protected boolean changeMode(String mode) throws DaikinCommunicationException {
        Mode newMode;
        try {
            newMode = Mode.valueOf(mode);
        } catch (IllegalArgumentException ex) {
            logger.warn("Invalid mode: {}. Valid values: {}", mode, Mode.values());
            return false;
        }
        ControlInfo info = webTargets.getControlInfo();
        info.mode = newMode;
        if (autoModeValue.isPresent()) {
            info.autoModeValue = autoModeValue.get();
        }
        boolean accepted = webTargets.setControlInfo(info);

        // If mode=0 is not accepted try AUTO1 (mode=1)
        if (!accepted && newMode == Mode.AUTO && autoModeValue.isEmpty()) {
            info.autoModeValue = Mode.AUTO1.getValue();
            accepted = webTargets.setControlInfo(info);
            if (accepted) {
                autoModeValue = Optional.of(info.autoModeValue);
                logger.debug("AUTO uses mode={}", info.autoModeValue);
            } else {
                logger.warn("AUTO mode not accepted with mode=0 or mode=1");
            }
        }

        return accepted;
    }

    @Override
    protected boolean changeFanSpeed(String fanSpeed) throws DaikinCommunicationException {
        FanSpeed newSpeed;
        try {
            newSpeed = FanSpeed.valueOf(fanSpeed);
        } catch (IllegalArgumentException ex) {
            logger.warn("Invalid fan speed: {}. Valid values: {}", fanSpeed, FanSpeed.values());
            return false;
        }
        ControlInfo info = webTargets.getControlInfo();
        info.fanSpeed = newSpeed;
        return webTargets.setControlInfo(info);
    }

    protected boolean changeFanDir(String fanDir) throws DaikinCommunicationException {
        FanMovement newMovement;
        try {
            newMovement = FanMovement.valueOf(fanDir);
        } catch (IllegalArgumentException ex) {
            logger.warn("Invalid fan direction: {}. Valid values: {}", fanDir, FanMovement.values());
            return false;
        }
        ControlInfo info = webTargets.getControlInfo();
        info.fanMovement = newMovement;
        return webTargets.setControlInfo(info);
    }

    protected boolean changeSpecialMode(String specialMode) throws DaikinCommunicationException {
        SpecialMode newMode;
        try {
            newMode = SpecialMode.valueOf(specialMode);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid specialmode: {}. Valid values: {}", specialMode, SpecialMode.values());
            return false;
        }
        return webTargets.setSpecialMode(newMode);
    }

    protected boolean changeStreamer(boolean streamerMode) throws DaikinCommunicationException {
        return webTargets.setStreamerMode(streamerMode);
    }

    protected void changeDemandMode(String mode) throws DaikinCommunicationException {
        DemandControlMode newMode;
        try {
            newMode = DemandControlMode.valueOf(mode);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid demand mode: {}. Valid values: {}", mode, DemandControlMode.values());
            return;
        }
        DemandControl demandInfo = webTargets.getDemandControl();
        boolean scheduleChanged = false;
        boolean maxPowerChanged = false;
        if (demandInfo.mode != newMode) {
            if (newMode == DemandControlMode.SCHEDULED && savedDemandControlSchedule.isPresent()) {
                // restore previously saved schedule
                demandInfo.setSchedule(savedDemandControlSchedule.get());
                scheduleChanged = true;
            }

            if (newMode == DemandControlMode.MANUAL && savedDemandControlMaxPower.isPresent()) {
                // restore previously saved maxPower
                demandInfo.maxPower = savedDemandControlMaxPower.get();
                maxPowerChanged = true;
            }
        }
        demandInfo.mode = newMode;
        if (webTargets.setDemandControl(demandInfo)) {
            updateState(DaikinBindingConstants.CHANNEL_AC_DEMAND_MODE, new StringType(newMode.name()));
            if (scheduleChanged) {
                updateState(DaikinBindingConstants.CHANNEL_AC_DEMAND_SCHEDULE,
                        new StringType(savedDemandControlSchedule.get()));
            }
            if (maxPowerChanged) {
                updateState(DaikinBindingConstants.CHANNEL_AC_DEMAND_MAX_POWER,
                        new PercentType(savedDemandControlMaxPower.get()));
            }
        }
    }

    protected boolean changeDemandMaxPower(int maxPower) throws DaikinCommunicationException {
        DemandControl demandInfo = webTargets.getDemandControl();
        demandInfo.mode = DemandControlMode.MANUAL;
        demandInfo.maxPower = maxPower;
        savedDemandControlMaxPower = Optional.of(maxPower);
        return webTargets.setDemandControl(demandInfo);
    }

    protected boolean changeDemandSchedule(String schedule) throws DaikinCommunicationException {
        DemandControl demandInfo = webTargets.getDemandControl();
        try {
            demandInfo.setSchedule(schedule);
        } catch (JsonSyntaxException e) {
            logger.warn("Invalid schedule: {}. {}", schedule, e.getMessage());
            return false;
        }
        demandInfo.mode = DemandControlMode.SCHEDULED;
        savedDemandControlSchedule = Optional.of(demandInfo.getSchedule());
        return webTargets.setDemandControl(demandInfo);
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
                Objects.requireNonNull(maybePower.<State> map(
                        t -> new QuantityType<>(BigDecimal.valueOf(t[i - 1].longValue(), 1), Units.KILOWATT_HOUR))
                        .orElse(UnDefType.UNDEF)))

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
                    Objects.requireNonNull(
                            maybePower.<State> map(t -> new QuantityType<>(new DecimalType(t), Units.KILOWATT_HOUR))
                                    .orElse(UnDefType.UNDEF)));
        }
    }

    @Override
    protected void registerUuid(@Nullable String key) {
        if (key == null) {
            return;
        }
        try {
            webTargets.registerUuid(key);
        } catch (DaikinCommunicationException e) {
            // suppress exceptions
            logger.debug("registerUuid({}) error: {}", key, e.getMessage());
        }
    }
}
