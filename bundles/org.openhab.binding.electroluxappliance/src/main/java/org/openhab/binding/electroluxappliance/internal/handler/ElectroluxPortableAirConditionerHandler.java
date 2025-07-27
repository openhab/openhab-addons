/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.electroluxappliance.internal.handler;

import static org.openhab.binding.electroluxappliance.internal.ElectroluxApplianceBindingConstants.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import javax.measure.UnconvertibleException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.electroluxappliance.internal.api.ElectroluxGroupAPI;
import org.openhab.binding.electroluxappliance.internal.dto.ApplianceDTO;
import org.openhab.binding.electroluxappliance.internal.dto.PortableAirConditionerStateDTO;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ElectroluxPortableAirConditionerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class ElectroluxPortableAirConditionerHandler extends ElectroluxApplianceHandler {

    private final Logger logger = LoggerFactory.getLogger(ElectroluxPortableAirConditionerHandler.class);

    private final Storage<String> strStore;

    public ElectroluxPortableAirConditionerHandler(Thing thing, @Reference TranslationProvider translationProvider,
            @Reference LocaleProvider localeProvider, @Reference Storage<String> strStore) {
        super(thing, translationProvider, localeProvider);
        this.strStore = strStore;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Command received: {} on channelID: {}", command, channelUID);
        if (CHANNEL_STATUS.equals(channelUID.getId()) || command instanceof RefreshType) {
            super.handleCommand(channelUID, command);
            return;
        }
        ApplianceDTO dto = getApplianceDTO();
        ElectroluxGroupAPI api = getElectroluxGroupAPI();
        if (CHANNEL_FAN_MODE.equals(channelUID.getId())) {
            if (api != null && dto != null) {
                api.sendCapabilityRequest(dto.getApplianceId(), "fanSpeedSetting", dto,
                        command.toString().toUpperCase());
            }
        } else if (CHANNEL_SLEEP_MODE.equals(channelUID.getId())) {
            if (api != null && dto != null) {
                api.sendCapabilityRequest(dto.getApplianceId(), "sleepMode", dto,
                        command.equals(OnOffType.ON) ? "ON" : "OFF");
            }
        } else if (CHANNEL_FAN_SWING.equals(channelUID.getId())) {
            if (api != null && dto != null) {
                api.sendCapabilityRequest(dto.getApplianceId(), "verticalSwing", dto,
                        command.equals(OnOffType.ON) ? "ON" : "OFF");
            }
        } else if (CHANNEL_CHILD_LOCK.equals(channelUID.getId())) {
            if (api != null && dto != null) {
                api.sendCapabilityRequest(dto.getApplianceId(), "uiLockMode", dto,
                        String.valueOf(command.equals(OnOffType.ON)).toLowerCase());
            }
        } else if (CHANNEL_DEVICE_RUNNING.equals(channelUID.getId())) {
            if (api != null && dto != null) {
                api.sendCapabilityRequest(dto.getApplianceId(), "executeCommand", dto,
                        command.equals(OnOffType.ON) ? "ON" : "OFF");
            }
        } else if (CHANNEL_MODE.equals(channelUID.getId())) {
            if (api != null && dto != null) {
                api.sendCapabilityRequest(dto.getApplianceId(), "mode", dto, command.toString().toUpperCase());
            }
        } else if (CHANNEL_TARGET_TEMPERATURE.equals(channelUID.getId())) {
            if (api != null && dto != null) {
                if (command instanceof QuantityType quantityCommand) {
                    try {
                        double value = quantityCommand.doubleValue();
                        if (quantityCommand.getUnit() == ImperialUnits.FAHRENHEIT) {
                            value = ImperialUnits.FAHRENHEIT.getConverterTo(SIUnits.CELSIUS)
                                    .convert(quantityCommand.doubleValue());
                        }

                        api.sendCapabilityRequest(dto.getApplianceId(), "targetTemperatureC", dto,
                                String.valueOf(Math.round(value)));

                    } catch (UnconvertibleException e) {
                        logger.warn("{}", getLocalizedText("error.electroluxappliance.pac.failed-target-temp-units",
                                e.getMessage()));
                    }
                }
            }
        } else if (CHANNEL_OFF_TIMER_ACTIVE.equals(channelUID.getId())) {
            if (api != null && dto != null) {
                if (command instanceof OnOffType) {
                    if (OnOffType.OFF.equals(command)) {
                        api.sendCapabilityRequest(dto.getApplianceId(), "stopTime", dto, "0");
                    } else {
                        final String savedVal = strStore.get(CHANNEL_OFF_TIMER_DURATION);
                        if (savedVal != null) {
                            boolean result = api.sendCapabilityRequest(dto.getApplianceId(), "stopTime", dto, savedVal);
                            if (result) {
                                try {
                                    int durationSeconds = Integer.parseInt(savedVal);
                                    updateState(CHANNEL_OFF_TIMER_TIME,
                                            new DateTimeType(Instant.now().plus(durationSeconds, ChronoUnit.SECONDS)));
                                } catch (NumberFormatException nfe) {
                                }
                            }
                        }
                    }
                }
            }
        } else if (CHANNEL_ON_TIMER_ACTIVE.equals(channelUID.getId())) {
            if (api != null && dto != null) {
                if (command instanceof OnOffType) {
                    if (OnOffType.OFF.equals(command)) {
                        api.sendCapabilityRequest(dto.getApplianceId(), "startTime", dto, "0");
                    } else {
                        final String savedVal = strStore.get(CHANNEL_ON_TIMER_DURATION);
                        if (savedVal != null) {
                            boolean result = api.sendCapabilityRequest(dto.getApplianceId(), "startTime", dto,
                                    savedVal);
                            if (result) {
                                try {
                                    int durationSeconds = Integer.parseInt(savedVal);
                                    updateState(CHANNEL_ON_TIMER_TIME,
                                            new DateTimeType(Instant.now().plus(durationSeconds, ChronoUnit.SECONDS)));
                                } catch (NumberFormatException nfe) {
                                }
                            }
                        }
                    }
                }
            }
        } else if (CHANNEL_OFF_TIMER_DURATION.equals(channelUID.getId())) {
            if (command instanceof QuantityType quantityCommand) {
                int targetValue = quantityCommand.intValue();
                strStore.put(CHANNEL_OFF_TIMER_DURATION, String.valueOf(targetValue));
            }
        } else if (CHANNEL_ON_TIMER_DURATION.equals(channelUID.getId())) {
            if (command instanceof QuantityType quantityCommand) {
                int targetValue = quantityCommand.intValue();
                strStore.put(CHANNEL_ON_TIMER_DURATION, String.valueOf(targetValue));
            }
        }
    }

    @Override
    public void update(@Nullable ApplianceDTO dto) {
        if (dto != null) {
            // Update all channels from the updated data
            getThing().getChannels().stream().map(Channel::getUID).filter(channelUID -> isLinked(channelUID))
                    .forEach(channelUID -> {
                        State state = getValue(channelUID.getId(), dto);
                        logger.trace("Channel: {}, State: {}", channelUID, state);
                        updateState(channelUID, state);
                    });
            if ("Connected".equalsIgnoreCase(dto.getApplianceState().getConnectionState())) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        getLocalizedText("error.electroluxappliance.pac.not-connected"));
            }
        }
    }

    private State getValue(final String channelId, final ApplianceDTO dto) {
        var reported = ((PortableAirConditionerStateDTO) dto.getApplianceState()).getProperties().getReported();
        switch (channelId) {
            case CHANNEL_AMBIENT_TEMPERATURE:
                if (reported.getIsReadAmbientTemperatureC()) {
                    return new QuantityType<>(reported.getAmbientTemperatureC(), SIUnits.CELSIUS);
                } else if (reported.getIsReadAmbientTemperatureF()) {
                    return new QuantityType<>(reported.getAmbientTemperatureF(), ImperialUnits.FAHRENHEIT);
                }
            case CHANNEL_TARGET_TEMPERATURE:
                if (reported.getIsReadTargetTemperature()) {
                    return new QuantityType<>(reported.getTargetTemperature(), SIUnits.CELSIUS);
                }
            case CHANNEL_SLEEP_MODE:
                if (reported.getIsReadSleepMode()) {
                    return reported.getSleepModeOn() ? OnOffType.ON : OnOffType.OFF;
                }
            case CHANNEL_FAN_SWING:
                if (reported.getIsReadVerticalSwing()) {
                    return reported.getVerticalSwingOn() ? OnOffType.ON : OnOffType.OFF;
                }
            case CHANNEL_CHILD_LOCK:
                if (reported.getIsReadUiLockMode()) {
                    return reported.getUiLockModeOn() ? OnOffType.ON : OnOffType.OFF;
                }
            case CHANNEL_FAN_MODE:
                if (reported.getIsReadFanSpeedSetting()) {
                    return new StringType(reported.getFanSpeedSetting().toUpperCase());
                }
            case CHANNEL_MODE:
                if (reported.getIsReadMode()) {
                    return new StringType(reported.getMode().toUpperCase());
                }
            case CHANNEL_NETWORK_QUALITY_INDICATOR:
                if (reported.getNetworkInterface().getIsReadLinkQualityIndicator()) {
                    return new StringType(reported.getNetworkInterface().getLinkQualityIndicator().toUpperCase());
                }
            case CHANNEL_DEVICE_RUNNING:
                if (reported.getIsReadApplianceState()) {
                    return reported.getApplicanceStateRunning() ? OnOffType.ON : OnOffType.OFF;
                }
            case CHANNEL_NETWORK_RSSI:
                if (reported.getNetworkInterface().getIsReadRSSI()) {
                    return new QuantityType<>(reported.getNetworkInterface().getRSSI(), Units.DECIBEL_MILLIWATTS);
                }
            case CHANNEL_COMPRESSOR_STATE:
                if (reported.getIsReadCompressorState()) {
                    return reported.getCompressorStateOn() ? OnOffType.ON : OnOffType.OFF;
                }
            case CHANNEL_FOURWAY_VALVE_STATE:
                if (reported.getIsReadFourWayValveState()) {
                    return reported.getFourWayValveStateOn() ? OnOffType.ON : OnOffType.OFF;
                }
            case CHANNEL_EVAP_DEFROST_STATE:
                if (reported.getIsReadEvapDefrostState()) {
                    return reported.getEvapDefrostStateOn() ? OnOffType.ON : OnOffType.OFF;
                }
            case CHANNEL_OFF_TIMER_ACTIVE:
                if (reported.getIsReadStopTime()) {
                    return reported.getStopTime() > 0 ? OnOffType.ON : OnOffType.OFF;
                }
            case CHANNEL_ON_TIMER_ACTIVE:
                if (reported.getIsReadStartTime()) {
                    return reported.getStartTime() > 0 ? OnOffType.ON : OnOffType.OFF;
                }
            case CHANNEL_OFF_TIMER_DURATION:
                final String storedOffValue = strStore.get(CHANNEL_OFF_TIMER_DURATION);
                if (storedOffValue != null) {
                    return new QuantityType<>(Integer.valueOf(storedOffValue), Units.SECOND);
                } else {
                    strStore.put(CHANNEL_OFF_TIMER_DURATION, "1800");
                    return new QuantityType<>(1800, Units.SECOND);
                }
            case CHANNEL_ON_TIMER_DURATION:
                final String storedOnValue = strStore.get(CHANNEL_ON_TIMER_DURATION);
                if (storedOnValue != null) {
                    return new QuantityType<>(Integer.valueOf(storedOnValue), Units.SECOND);
                } else {
                    strStore.put(CHANNEL_ON_TIMER_DURATION, "1800");
                    return new QuantityType<>(1800, Units.SECOND);
                }
            case CHANNEL_ON_TIMER_TIME:
                if (reported.getIsReadStartTime() && reported.getStartTime() > 0) {
                    return new DateTimeType(
                            dto.getApplianceStateTimestamp().plus(reported.getStartTime(), ChronoUnit.SECONDS));
                }
            case CHANNEL_OFF_TIMER_TIME:
                if (reported.getIsReadStopTime() && reported.getStopTime() > 0) {
                    return new DateTimeType(
                            dto.getApplianceStateTimestamp().plus(reported.getStopTime(), ChronoUnit.SECONDS));
                }
            case CHANNEL_FILTER_STATE:
                if (reported.getIsReadFilterState()) {
                    return new StringType(reported.getFilterState().toUpperCase());
                }

        }
        return UnDefType.UNDEF;
    }

    @Override
    public Map<String, String> refreshProperties() {
        Map<String, String> properties = new HashMap<>();

        final Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof ElectroluxApplianceBridgeHandler bridgeHandler) {
            ApplianceDTO dto = bridgeHandler.getElectroluxApplianceThings().get(getApplianceConfig().getSerialNumber());
            if (dto != null) {
                var applianceInfo = dto.getApplianceInfo().getApplianceInfo();
                properties.put(Thing.PROPERTY_VENDOR, applianceInfo.getBrand());
                properties.put(PROPERTY_COLOUR, applianceInfo.getColour());
                properties.put(PROPERTY_DEVICE, applianceInfo.getDeviceType());
                properties.put(Thing.PROPERTY_MODEL_ID, applianceInfo.getModel());
                properties.put(Thing.PROPERTY_SERIAL_NUMBER, applianceInfo.getSerialNumber());

                if (dto.getApplianceState() instanceof PortableAirConditionerStateDTO pacDto) {
                    if (pacDto.getProperties().getReported().getIsReadVmNoNIO()) {
                        properties.put(PROPERTY_NIU_FW_VERSION, pacDto.getProperties().getReported().getVmNoNIO());
                    }
                    if (pacDto.getProperties().getReported().getIsReadVmNoMCU()) {
                        properties.put(PROPERTY_MCU_FW_VERSION, pacDto.getProperties().getReported().getVmNoMCU());
                    }
                }
            }
        }
        return properties;
    }
}
