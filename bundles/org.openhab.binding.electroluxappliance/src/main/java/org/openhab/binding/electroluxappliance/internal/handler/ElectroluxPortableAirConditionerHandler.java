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

import java.util.HashMap;
import java.util.Map;

import javax.measure.UnconvertibleException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.electroluxappliance.internal.ElectroluxApplianceConfiguration;
import org.openhab.binding.electroluxappliance.internal.api.ElectroluxGroupAPI;
import org.openhab.binding.electroluxappliance.internal.dto.AirPurifierStateDTO;
import org.openhab.binding.electroluxappliance.internal.dto.ApplianceDTO;
import org.openhab.binding.electroluxappliance.internal.dto.PortableAirConditionerStateDTO;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
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

    private ElectroluxApplianceConfiguration config = new ElectroluxApplianceConfiguration();

    public ElectroluxPortableAirConditionerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Command received: {} on channelID: {}", command, channelUID);
        if (CHANNEL_STATUS.equals(channelUID.getId()) || command instanceof RefreshType) {
            super.handleCommand(channelUID, command);
        }
        ApplianceDTO dto = getApplianceDTO();
        ElectroluxGroupAPI api = getElectroluxGroupAPI();
        if (CHANNEL_FAN_MODE.equals(channelUID.getId())) {
            if (api != null && dto != null) {
                api.setPACFanMode(dto.getApplianceId(), command.toString().toUpperCase());
            }
        } else if (CHANNEL_SLEEP_MODE.equals(channelUID.getId())) {
            if (api != null && dto != null) {
                api.setPACSleepMode(dto.getApplianceId(), command.equals(OnOffType.ON) ? "ON" : "OFF");
            }
        } else if (CHANNEL_FAN_SWING.equals(channelUID.getId())) {
            if (api != null && dto != null) {
                api.setPACSwingState(dto.getApplianceId(), command.equals(OnOffType.ON) ? "ON" : "OFF");
            }
        } else if (CHANNEL_CHILD_LOCK.equals(channelUID.getId())) {
            if (api != null && dto != null) {
                api.setPACChildLockState(dto.getApplianceId(), command.equals(OnOffType.ON));
            }
        } else if (CHANNEL_DEVICE_RUNNING.equals(channelUID.getId())) {
            if (api != null && dto != null) {
                api.setPACRunning(dto.getApplianceId(), command.equals(OnOffType.ON) ? "ON" : "OFF");
            }
        } else if (CHANNEL_MODE.equals(channelUID.getId())) {
            if (api != null && dto != null) {
                api.setPACMode(dto.getApplianceId(), command.toString().toUpperCase());
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
                        api.setPACTargetTemperature(dto.getApplianceId(), (int) Math.round(value));
                    } catch (UnconvertibleException e) {
                        logger.warn("Failed to get correct units for target temperature {}", e.getMessage());
                    }
                }
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
                        "Portable Air Conditioner not connected");
            }
        }
    }

    private State getValue(final String channelId, final ApplianceDTO dto) {
        var reported = ((PortableAirConditionerStateDTO) dto.getApplianceState()).getProperties().getReported();
        switch (channelId) {
            case CHANNEL_AMBIENT_TEMPERATURE: // Measured temperature
                if (reported.getIsReadAmbientTemperatureC()) {
                    return new QuantityType<>(reported.getAmbientTemperatureC(), SIUnits.CELSIUS);
                } else if (reported.getIsReadAmbientTemperatureF()) {
                    return new QuantityType<>(reported.getAmbientTemperatureF(), ImperialUnits.FAHRENHEIT);
                }
            case CHANNEL_TARGET_TEMPERATURE: // Target set-point temperature
                if (reported.getIsReadTargetTemperature()) {
                    return new QuantityType<>(reported.getTargetTemperature(), SIUnits.CELSIUS);
                }
            case CHANNEL_SLEEP_MODE: // Whether sleep mode is disabled from the dto
                if (reported.getIsReadSleepMode()) {
                    return reported.getSleepModeOn() ? OnOffType.ON : OnOffType.OFF;
                }
            case CHANNEL_FAN_SWING: // Whether the fan swing as its called is enabled
                if (reported.getIsReadVerticalSwing()) {
                    return reported.getVerticalSwingOn() ? OnOffType.ON : OnOffType.OFF;
                }
            case CHANNEL_CHILD_LOCK:
                if (reported.getIsReadUiLockMode()) {
                    return reported.getUiLockModeOn() ? OnOffType.ON : OnOffType.OFF;
                }
            case CHANNEL_FAN_MODE:
                if (reported.isReadFanSpeedSetting()) {
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
        }
        logger.warn("Read {}", reported);
        return UnDefType.UNDEF;
    }

    @Override
    public Map<String, String> refreshProperties() {
        Map<String, String> properties = new HashMap<>();

        final Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof ElectroluxApplianceBridgeHandler bridgeHandler) {
            ApplianceDTO dto = bridgeHandler.getElectroluxApplianceThings().get(config.getSerialNumber());
            if (dto != null) {
                var applianceInfo = dto.getApplianceInfo().getApplianceInfo();
                properties.put(Thing.PROPERTY_VENDOR, applianceInfo.getBrand());
                properties.put(PROPERTY_COLOUR, applianceInfo.getColour());
                properties.put(PROPERTY_DEVICE, applianceInfo.getDeviceType());
                properties.put(Thing.PROPERTY_MODEL_ID, applianceInfo.getModel());
                properties.put(Thing.PROPERTY_SERIAL_NUMBER, applianceInfo.getSerialNumber());
                properties.put(Thing.PROPERTY_FIRMWARE_VERSION,
                        ((AirPurifierStateDTO) dto.getApplianceState()).getProperties().getReported().getFrmVerNIU());

            }
        }
        return properties;
    }
}
