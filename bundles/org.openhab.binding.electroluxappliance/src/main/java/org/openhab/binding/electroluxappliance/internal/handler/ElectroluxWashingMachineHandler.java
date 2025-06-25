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
import org.openhab.binding.electroluxappliance.internal.dto.ApplianceDTO;
import org.openhab.binding.electroluxappliance.internal.dto.WashingMachineStateDTO;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
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
 * The {@link ElectroluxWashingMachineHandler} is responsible for handling commands and status updates for
 * Electrolux washing machines.
 *
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public class ElectroluxWashingMachineHandler extends ElectroluxApplianceHandler {

    private final Logger logger = LoggerFactory.getLogger(ElectroluxWashingMachineHandler.class);

    public ElectroluxWashingMachineHandler(Thing thing, @Reference TranslationProvider translationProvider,
            @Reference LocaleProvider localeProvider) {
        super(thing, translationProvider, localeProvider);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Command received: {} on channelID: {}", command, channelUID);
        if (CHANNEL_STATUS.equals(channelUID.getId()) || command instanceof RefreshType) {
            super.handleCommand(channelUID, command);
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
                        getLocalizedText("error.electroluxappliance.wm.not-connected"));
            }
        }
    }

    private State getValue(String channelId, ApplianceDTO dto) {
        var reported = ((WashingMachineStateDTO) dto.getApplianceState()).getProperties().getReported();
        switch (channelId) {
            case CHANNEL_DOOR_STATE:
                return "OPEN".equals(reported.getDoorState()) ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
            case CHANNEL_DOOR_LOCK:
                return "ON".equals(reported.getDoorLock()) ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
            case CHANNEL_TIME_TO_START:
                return new QuantityType<>(reported.getStartTime(), Units.SECOND);
            case CHANNEL_TIME_TO_END:
                return new QuantityType<>(reported.getTimeToEnd(), Units.SECOND);
            case CHANNEL_APPLIANCE_UI_SW_VERSION:
                return new StringType(reported.getApplianceUiSwVersion());
            case CHANNEL_OPTISENSE_RESULT:
                return new StringType(Integer.toString(reported.getFCMiscellaneousState().getOptisenseResult()));
            case CHANNEL_DETERGENT_EXTRA_DOSAGE:
                return new StringType(Integer.toString(reported.getFCMiscellaneousState().getDetergentExtradosage()));
            case CHANNEL_SOFTENER_EXTRA_DOSAGE:
                return new StringType(Integer.toString(reported.getFCMiscellaneousState().getSoftenerExtradosage()));
            case CHANNEL_WATER_USAGE:
                return new QuantityType<>(reported.getFCMiscellaneousState().getWaterUsage(), Units.LITRE);
            case CHANNEL_TOTAL_WASH_CYCLES_COUNT:
                return new StringType(Integer.toString(reported.getTotalWashCyclesCount()));
            case CHANNEL_CYCLE_PHASE:
                return new StringType(reported.getCyclePhase());
            case CHANNEL_APPLIANCE_TOTAL_WORKING_TIME:
                return new StringType(Integer.toString(reported.getApplianceTotalWorkingTime()));
            case CHANNEL_APPLIANCE_STATE:
                return new StringType(reported.getApplianceState());
            case CHANNEL_APPLIANCE_MODE:
                return new StringType(reported.getApplianceMode());
            case CHANNEL_ANALOG_TEMPERATURE:
                return new StringType(reported.getUserSelections().getAnalogTemperature());
            case CHANNEL_ANALOG_SPIN_SPEED:
                return new StringType(reported.getUserSelections().getAnalogSpinSpeed());
            case CHANNEL_STEAM_VALUE:
                return new StringType(reported.getUserSelections().getSteamValue());
            case CHANNEL_PROGRAMS_ORDER:
                return new StringType(reported.getUserSelections().getProgramUID());

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
                properties.put(Thing.PROPERTY_FIRMWARE_VERSION, ((WashingMachineStateDTO) dto.getApplianceState())
                        .getProperties().getReported().getNetworkInterface().getSwVersion());
            }
        }
        return properties;
    }
}
