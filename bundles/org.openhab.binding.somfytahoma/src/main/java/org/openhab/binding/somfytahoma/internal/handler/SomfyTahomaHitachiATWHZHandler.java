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
 * The {@link SomfyTahomaHitachiATWHZHandler} is responsible for handling commands,
 * which are sent to one of the channels of the Hitachi Air To Water Heating Zone thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaHitachiATWHZHandler extends SomfyTahomaBaseThingHandler {

    public SomfyTahomaHitachiATWHZHandler(Thing thing) {
        super(thing);
        stateNames.put(ZONE_MODE, "modbus:AutoManuModeZone1State");
        stateNames.put(CIRCUIT_CONTROL, "modbus:ControlCircuit1State");
        stateNames.put(CIRCUIT_STATUS, "modbus:StatusCircuit1State");
        stateNames.put(YUTAKI_TARGET_MODE, "modbus:YutakiTargetModeState");
        stateNames.put(YUTAKI_MODE, "modbus:YutakiModeState");
        stateNames.put(HOLIDAY_MODE, "modbus:HolidayModeZone1State");
        stateNames.put(ALARM_NUMBER, "modbus:AlarmNumberState");
        stateNames.put(THERMOSTAT_SETTING_ZONE1, "modbus:ThermostatSettingStatusZone1State");
        stateNames.put(WH_SETTING_TEMP_ZONE1, "modbus:WaterHeatingSettingTemperatureStatusZone1State");
        stateNames.put(ROOM_AMBIENT_TEMP_ZONE1, "modbus:RoomAmbientTemperatureStatusZone1State");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        if (command instanceof RefreshType) {
            return;
        } else {
            switch (channelUID.getId()) {
                case ZONE_MODE:
                    if (command instanceof StringType) {
                        sendCommand("setAutoManuMode", "[\"" + command + "\"]");
                    }
                    break;
                case CIRCUIT_CONTROL:
                    if (command instanceof StringType) {
                        sendCommand("setControlCircuit1", "[\"" + command + "\"]");
                    }
                    break;
                case YUTAKI_TARGET_MODE:
                    if (command instanceof StringType) {
                        sendCommand("setTargetMode", "[\"" + command + "\"]");
                    }
                    break;
                case HOLIDAY_MODE:
                    if (command instanceof StringType) {
                        sendCommand("setHolidayMode", "[\"" + command + "\"]");
                    }
                    break;
                case THERMOSTAT_SETTING_ZONE1:
                    sendTempCommand("setThermostatSettingControlZone1", command);
                    break;
                case WH_SETTING_TEMP_ZONE1:
                    sendTempCommand("setWaterHeatingSettingTemperatureControlZone1", command);
                    break;
                case ROOM_AMBIENT_TEMP_ZONE1:
                    sendTempCommand("setRoomAmbientTemperatureControlZone1", command);
                    break;
                default:
                    getLogger().debug("This channel doesn't accept any commands");
            }
        }
    }
}
