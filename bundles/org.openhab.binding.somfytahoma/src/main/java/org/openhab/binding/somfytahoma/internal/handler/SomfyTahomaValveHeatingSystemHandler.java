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
package org.openhab.binding.somfytahoma.internal.handler;

import static org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants.*;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link SomfyTahomaValveHeatingSystemHandler} is responsible for handling commands,
 * which are sent to one of the channels of the valve heating system thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaValveHeatingSystemHandler extends SomfyTahomaBaseThingHandler {

    public SomfyTahomaValveHeatingSystemHandler(Thing thing) {
        super(thing);
        stateNames.put(TARGET_TEMPERATURE, TARGET_ROOM_TEMPERATURE_STATE);
        stateNames.put(BATTERY_LEVEL, BATTERY_LEVEL_STATE);
        stateNames.put(DEROGATION_HEATING_MODE, "io:DerogationHeatingModeState");
        stateNames.put(DEROGATED_TARGET_TEMPERATURE, "core:DerogatedTargetTemperatureState");
        stateNames.put(CURRENT_HEATING_MODE, "io:CurrentHeatingModeState");
        stateNames.put(OPEN_CLOSED_VALVE, "core:OpenClosedValveState");
        stateNames.put(OPERATING_MODE, "core:OperatingModeState");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        if (command instanceof RefreshType) {
            return;
        } else {
            if (DEROGATED_TARGET_TEMPERATURE.equals(channelUID.getId())) {
                BigDecimal temperature = toTemperature(command);
                if (temperature != null) {
                    String param = "[" + temperature.toPlainString() + ", \"next_mode\"]";
                    sendCommand(COMMAND_SET_DEROGATION, param);
                }
            } else if (DEROGATION_HEATING_MODE.equals(channelUID.getId())) {
                switch (command.toString()) {
                    case "auto":
                        sendCommand("exitDerogation");
                        break;
                    case "away":
                    case "comfort":
                    case "eco":
                    case "frostprotection":
                        String param = "[\"" + command.toString() + "\", \"next_mode\"]";
                        sendCommand(COMMAND_SET_DEROGATION, param);
                        break;
                    default:

                }
            }
        }
    }
}
