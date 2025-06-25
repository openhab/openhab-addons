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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.electroluxappliance.internal.ElectroluxApplianceBindingConstants;
import org.openhab.binding.electroluxappliance.internal.api.ElectroluxGroupAPI;
import org.openhab.binding.electroluxappliance.internal.dto.AirPurifierStateDTO;
import org.openhab.binding.electroluxappliance.internal.dto.ApplianceDTO;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
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
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ElectroluxAirPurifierHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public class ElectroluxAirPurifierHandler extends ElectroluxApplianceHandler {

    private final Logger logger = LoggerFactory.getLogger(ElectroluxAirPurifierHandler.class);

    public ElectroluxAirPurifierHandler(Thing thing, @Reference TranslationProvider translationProvider,
            @Reference LocaleProvider localeProvider) {
        super(thing, translationProvider, localeProvider);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Command received: {} on channelID: {}", command, channelUID);
        if (CHANNEL_STATUS.equals(channelUID.getId()) || command instanceof RefreshType) {
            super.handleCommand(channelUID, command);
        } else {
            ApplianceDTO dto = getApplianceDTO();
            ElectroluxGroupAPI api = getElectroluxGroupAPI();
            if (api != null && dto != null) {
                if (CHANNEL_WORK_MODE.equals(channelUID.getId())) {
                    if (command.toString().equals(COMMAND_WORKMODE_POWEROFF)) {
                        api.workModePowerOff(dto.getApplianceId());
                    } else if (command.toString().equals(COMMAND_WORKMODE_AUTO)) {
                        api.workModeAuto(dto.getApplianceId());
                    } else if (command.toString().equals(COMMAND_WORKMODE_MANUAL)) {
                        api.workModeManual(dto.getApplianceId());
                    }
                } else if (CHANNEL_FAN_SPEED.equals(channelUID.getId())) {
                    api.setFanSpeedLevel(dto.getApplianceId(), Integer.parseInt(command.toString()));
                } else if (CHANNEL_IONIZER.equals(channelUID.getId())) {
                    if (command == OnOffType.OFF) {
                        api.setIonizer(dto.getApplianceId(), "false");
                    } else if (command == OnOffType.ON) {
                        api.setIonizer(dto.getApplianceId(), "true");
                    } else {
                        logger.debug("Unknown command! {}", command);
                    }
                } else if (CHANNEL_UI_LIGHT.equals(channelUID.getId())) {
                    if (command == OnOffType.OFF) {
                        api.setUILight(dto.getApplianceId(), "false");
                    } else if (command == OnOffType.ON) {
                        api.setUILight(dto.getApplianceId(), "true");
                    } else {
                        logger.debug("Unknown command! {}", command);
                    }
                } else if (CHANNEL_SAFETY_LOCK.equals(channelUID.getId())) {
                    if (command == OnOffType.OFF) {
                        api.setSafetyLock(dto.getApplianceId(), "false");
                    } else if (command == OnOffType.ON) {
                        api.setSafetyLock(dto.getApplianceId(), "true");
                    } else {
                        logger.debug("Unknown command! {}", command);
                    }
                }

                final Bridge bridge = getBridge();
                if (bridge != null && bridge.getHandler() instanceof ElectroluxApplianceBridgeHandler bridgeHandler) {
                    bridgeHandler.handleCommand(
                            new ChannelUID(this.thing.getUID(), ElectroluxApplianceBindingConstants.CHANNEL_STATUS),
                            RefreshType.REFRESH);
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
                        getLocalizedText("error.electroluxappliance.ap.not-connected"));
            }
        }
    }

    private State getValue(String channelId, ApplianceDTO dto) {
        var reported = ((AirPurifierStateDTO) dto.getApplianceState()).getProperties().getReported();
        switch (channelId) {
            case CHANNEL_TEMPERATURE:
                return new QuantityType<>(reported.getTemp(), SIUnits.CELSIUS);
            case CHANNEL_HUMIDITY:
                return new QuantityType<>(reported.getHumidity(), Units.PERCENT);
            case CHANNEL_TVOC:
                return new QuantityType<>(reported.getTvoc(), Units.PARTS_PER_BILLION);
            case CHANNEL_PM1:
                return new QuantityType<>(reported.getPm1(), Units.MICROGRAM_PER_CUBICMETRE);
            case CHANNEL_PM25:
                return new QuantityType<>(reported.getPm25(), Units.MICROGRAM_PER_CUBICMETRE);
            case CHANNEL_PM10:
                return new QuantityType<>(reported.getPm10(), Units.MICROGRAM_PER_CUBICMETRE);
            case CHANNEL_CO2:
                return new QuantityType<>(reported.getCo2(), Units.PARTS_PER_MILLION);
            case CHANNEL_FAN_SPEED:
                return new StringType(Integer.toString(reported.getFanspeed()));
            case CHANNEL_FILTER_LIFE:
                return new QuantityType<>(reported.getFilterLife(), Units.PERCENT);
            case CHANNEL_IONIZER:
                return OnOffType.from(reported.isIonizer());
            case CHANNEL_UI_LIGHT:
                return OnOffType.from(reported.isUiLight());
            case CHANNEL_SAFETY_LOCK:
                return OnOffType.from(reported.isSafetyLock());
            case CHANNEL_WORK_MODE:
                return new StringType(reported.getWorkmode());
            case CHANNEL_DOOR_STATE:
                return reported.isDoorOpen() ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
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
                properties.put(Thing.PROPERTY_FIRMWARE_VERSION,
                        ((AirPurifierStateDTO) dto.getApplianceState()).getProperties().getReported().getFrmVerNIU());

            }
        }
        return properties;
    }
}
