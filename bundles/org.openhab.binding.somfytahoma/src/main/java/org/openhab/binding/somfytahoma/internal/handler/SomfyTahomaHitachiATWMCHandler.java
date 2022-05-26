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
package org.openhab.binding.somfytahoma.internal.handler;

import static org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link SomfyTahomaHitachiATWMCHandler} is responsible for handling commands,
 * which are sent to one of the channels of the Hitachi Air To Water Main Component thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaHitachiATWMCHandler extends SomfyTahomaBaseThingHandler {

    public SomfyTahomaHitachiATWMCHandler(Thing thing) {
        super(thing);
        stateNames.put(AUTO_MANU_MODE, "core:AutoManuModeState");
        stateNames.put(UNIT_CONTROL, "modbus:ControlUnitState");
        stateNames.put(UNIT_MODE_STATUS, "modbus:StatusUnitModeState");
        stateNames.put(UNIT_MODE_CONTROL, "modbus:ControlUnitModeState");
        stateNames.put(BLOCK_MENU_CONTROL, "modbus:ControlBlockMenuState");
        stateNames.put(SPACE_MODE, "modbus:SpaceModeState");
        stateNames.put(ECO_MODE_TARGET_OFFSET, "modbus:EcoModeOffsetTargetState");
        stateNames.put(COMM_ALARM_BIT_STATUS, "modbus:StatusCommunicationAlarmBitState");
        stateNames.put(OPERATION, "modbus:OperationState");
        stateNames.put(OUTDOOR_TEMP, "modbus:OutdoorAmbientTemperatureState");
        stateNames.put(WATER_INLET_TEMP, "modbus:WaterInletUnitTemperatureState");
        stateNames.put(WATER_OUTLET_TEMP, "modbus:WaterOutletUnitTemperatureState");
        stateNames.put(ECO_MODE_OFFSET, "modbus:EcoModeOffsetState");
        stateNames.put(WATER_OUTLET_HP_TEMP, "modbus:WaterOutletHpTemperatureState");
        stateNames.put(LIQUID_TEMP_THMI, "modbus:LiquidTemperatureTHMIState");
        stateNames.put(LIQUID_TEMP, "modbus:LiquidTemperatureState");
        stateNames.put(COMPRESSOR_RUNNING_CURRENT, "modbus:CompressorRunningCurrentState");
        // override state type because the cloud sends consumption in percent
        cacheStateType(COMPRESSOR_RUNNING_CURRENT, TYPE_DECIMAL);
        stateNames.put(WATER_TEMP_SETTING, "modbus:WaterTemperatureSettingState");
        stateNames.put(YUTAKI_OPERATING_MODE, "modbus:YutakiVirtualOperatingModeState");
        stateNames.put(ALARM_NUMBER, "modbus:AlarmNumberState");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        if (command instanceof RefreshType) {
            return;
        } else {
            switch (channelUID.getId()) {
                case AUTO_MANU_MODE:
                    if (command instanceof StringType) {
                        sendCommand("setGlobalAutoManuMode", "[\"" + command + "\"]");
                    }
                    break;
                case UNIT_CONTROL:
                    if (command instanceof StringType) {
                        sendCommand("setControlUnit", "[\"" + command + "\"]");
                    }
                    break;
                case UNIT_MODE_CONTROL:
                    if (command instanceof StringType) {
                        sendCommand("setControlUnitMode", "[\"" + command + "\"]");
                    }
                    break;
                case BLOCK_MENU_CONTROL:
                    if (command instanceof StringType) {
                        sendCommand("setControlBlockMenu", "[\"" + command + "\"]");
                    }
                    break;
                case SPACE_MODE:
                    if (command instanceof StringType) {
                        sendCommand("setSpaceMode", "[\"" + command + "\"]");
                    }
                    break;
                case ECO_MODE_TARGET_OFFSET:
                    sendTempCommand("setEcoModeOffset", command);
                    break;
                default:
                    getLogger().debug("This channel doesn't accept any commands");
            }
        }
    }
}
