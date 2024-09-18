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
package org.openhab.binding.electroluxappliances.internal.handler;

import static org.openhab.binding.electroluxappliances.internal.ElectroluxAppliancesBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.electroluxappliances.internal.ElectroluxAppliancesConfiguration;
import org.openhab.binding.electroluxappliances.internal.dto.ApplianceDTO;
import org.openhab.binding.electroluxappliances.internal.dto.WashingMachineStateDTO;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
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
public class ElectroluxWashingMachineHandler extends ElectroluxAppliancesHandler {

    private final Logger logger = LoggerFactory.getLogger(ElectroluxWashingMachineHandler.class);

    private ElectroluxAppliancesConfiguration config = new ElectroluxAppliancesConfiguration();

    public ElectroluxWashingMachineHandler(Thing thing) {
        super(thing);
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
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private State getValue(String channelId, ApplianceDTO dto) {
        switch (channelId) {
            case DOOR_STATE:
                return "OPEN".equals(
                        ((WashingMachineStateDTO) dto.getApplianceState()).getProperties().getReported().getDoorState())
                                ? OpenClosedType.OPEN
                                : OpenClosedType.CLOSED;
            case DOOR_LOCK:
                return OnOffType.from(
                        ((WashingMachineStateDTO) dto.getApplianceState()).getProperties().getReported().getDoorLock());
            case START_TIME:
                return new QuantityType<>(
                        ((WashingMachineStateDTO) dto.getApplianceState()).getProperties().getReported().getStartTime(),
                        Units.SECOND);
            case TIME_TO_END:
                return new QuantityType<>(
                        ((WashingMachineStateDTO) dto.getApplianceState()).getProperties().getReported().getTimeToEnd(),
                        Units.SECOND);
            case APPLIANCE_UI_SW_VERSION:
                return new StringType(((WashingMachineStateDTO) dto.getApplianceState()).getProperties().getReported()
                        .getApplianceUiSwVersion());
            case CONNECTION_STATE:
                return "Connected".equals(((WashingMachineStateDTO) dto.getApplianceState()).getConnectionState())
                        ? OnOffType.from(true)
                        : OnOffType.from(false);
            case OPTISENSE_RESULT:
                return new StringType(Integer.toString(((WashingMachineStateDTO) dto.getApplianceState())
                        .getProperties().getReported().getFCMiscellaneousState().getOptisenseResult()));
            case DETERGENT_EXTRA_DOSAGE:
                return new StringType(Integer.toString(((WashingMachineStateDTO) dto.getApplianceState())
                        .getProperties().getReported().getFCMiscellaneousState().getDetergentExtradosage()));
            case SOFTENER_EXTRA_DOSAGE:
                return new StringType(Integer.toString(((WashingMachineStateDTO) dto.getApplianceState())
                        .getProperties().getReported().getFCMiscellaneousState().getSoftenerExtradosage()));
            case WATER_USAGE:
                return new StringType(Integer.toString(((WashingMachineStateDTO) dto.getApplianceState())
                        .getProperties().getReported().getFCMiscellaneousState().getWaterUsage()));
            case TOTAL_WASH_CYCLES_COUNT:
                return new StringType(Integer.toString(((WashingMachineStateDTO) dto.getApplianceState())
                        .getProperties().getReported().getTotalWashCyclesCount()));
            case CYCLE_PHASE:
                return new StringType(((WashingMachineStateDTO) dto.getApplianceState()).getProperties().getReported()
                        .getCyclePhase());
            case APPLIANCE_TOTAL_WORKING_TIME:
                return new StringType(Integer.toString(((WashingMachineStateDTO) dto.getApplianceState())
                        .getProperties().getReported().getApplianceTotalWorkingTime()));
            case APPLIANCE_STATE:
                return new StringType(((WashingMachineStateDTO) dto.getApplianceState()).getProperties().getReported()
                        .getApplianceState());
            case APPLIANCE_MODE:
                return new StringType(((WashingMachineStateDTO) dto.getApplianceState()).getProperties().getReported()
                        .getApplianceMode());
            case ANALOG_TEMPERATURE:
                return new StringType(((WashingMachineStateDTO) dto.getApplianceState()).getProperties().getReported()
                        .getUserSelections().getAnalogTemperature());
            case ANALOG_SPIN_SPEED:
                return new StringType(((WashingMachineStateDTO) dto.getApplianceState()).getProperties().getReported()
                        .getUserSelections().getAnalogSpinSpeed());
            case STEAM_VALUE:
                return new StringType(((WashingMachineStateDTO) dto.getApplianceState()).getProperties().getReported()
                        .getUserSelections().getSteamValue());
            case PROGRAMS_ORDER:
                return new StringType(((WashingMachineStateDTO) dto.getApplianceState()).getProperties().getReported()
                        .getUserSelections().getProgramUID());

        }
        return UnDefType.UNDEF;
    }

    @Override
    public Map<String, String> refreshProperties() {
        Map<String, String> properties = new HashMap<>();
        Bridge bridge = getBridge();
        if (bridge != null) {
            ElectroluxAppliancesBridgeHandler bridgeHandler = (ElectroluxAppliancesBridgeHandler) bridge.getHandler();
            if (bridgeHandler != null) {
                ApplianceDTO dto = bridgeHandler.getElectroluxAppliancesThings().get(config.getSerialNumber());
                if (dto != null) {
                    properties.put(Thing.PROPERTY_VENDOR, dto.getApplianceInfo().getApplianceInfo().getBrand());
                    properties.put(PROPERTY_COLOUR, dto.getApplianceInfo().getApplianceInfo().getColour());
                    properties.put(PROPERTY_DEVICE, dto.getApplianceInfo().getApplianceInfo().getDeviceType());
                    properties.put(Thing.PROPERTY_MODEL_ID, dto.getApplianceInfo().getApplianceInfo().getModel());
                    properties.put(Thing.PROPERTY_SERIAL_NUMBER,
                            dto.getApplianceInfo().getApplianceInfo().getSerialNumber());
                    properties.put(Thing.PROPERTY_FIRMWARE_VERSION, ((WashingMachineStateDTO) dto.getApplianceState())
                            .getProperties().getReported().getNetworkInterface().getSwVersion());
                }
            }
        }
        return properties;
    }
}
