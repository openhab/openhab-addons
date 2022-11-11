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
 * The {@link SomfyTahomaHitachiDHWHandler} is responsible for handling commands,
 * which are sent to one of the channels of the Hitachi DHW thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaHitachiDHWHandler extends SomfyTahomaBaseThingHandler {

    public SomfyTahomaHitachiDHWHandler(Thing thing) {
        super(thing);
        stateNames.put(DHW_MODE, "modbus:DHWModeState");
        stateNames.put(DHW, "modbus:StatusDHWState");
        stateNames.put(ANTI_LEGIONELLA, "modbus:StatusAntiLegionellaState");
        stateNames.put(ANTI_LEGIONELLA_TEMP, "modbus:StatusAntiLegionellaSettingTemperatureState");
        stateNames.put(DHW_SETTING_TEMP, "modbus:StatusDHWSettingTemperatureState");
        stateNames.put(DHW_TEMP, "core:DHWTemperatureState");
        stateNames.put(TARGET_BOOST_MODE, "modbus:YutakiTargetBoostModeState");
        stateNames.put(BOOST_MODE, "modbus:YutakiBoostModeState");
        stateNames.put(ALARM_NUMBER, "modbus:AlarmNumberState");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        if (command instanceof RefreshType) {
            return;
        } else {
            switch (channelUID.getId()) {
                case DHW:
                    if (command instanceof StringType) {
                        sendCommand("setControlDHW", "[\"" + command + "\"]");
                    }
                    break;
                case DHW_MODE:
                    if (command instanceof StringType) {
                        sendCommand("setDHWMode", "[\"" + command + "\"]");
                    }
                    break;
                case ANTI_LEGIONELLA:
                    if (command instanceof StringType) {
                        sendCommand("setControlAntiLegionella", "[\"" + command + "\"]");
                    }
                    break;
                case TARGET_BOOST_MODE:
                    if (command instanceof StringType) {
                        sendCommand("setTargetBoostMode", "[\"" + command + "\"]");
                    }
                    break;
                case DHW_SETTING_TEMP:
                    sendTempCommand("setControlDHWSettingTemperature", command);
                    break;
                case ANTI_LEGIONELLA_TEMP:
                    sendTempCommand("setControlAntiLegionellaSettingTemperature", command);
                    break;
                default:
                    getLogger().debug("This channel doesn't accept any commands");
            }
        }
    }
}
