/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
 * The {@link SomfyTahomaZwaveHeatingSystemHandler} is responsible for handling commands,
 * which are sent to one of the channels of the z-wave heating system thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaZwaveHeatingSystemHandler extends SomfyTahomaBaseThingHandler {

    public SomfyTahomaZwaveHeatingSystemHandler(Thing thing) {
        super(thing);
        stateNames.put(TARGET_TEMPERATURE, TARGET_TEMPERATURE_STATE);
        stateNames.put(CURRENT_TEMPERATURE, "zwave:SetPointHeatingValueState");
        stateNames.put(BATTERY_LEVEL, BATTERY_LEVEL_STATE);
        stateNames.put(CURRENT_STATE, ZWAVE_SET_POINT_TYPE_STATE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        if (command instanceof RefreshType) {
            return;
        } else {
            if (TARGET_TEMPERATURE.equals(channelUID.getId())) {
                BigDecimal temperature = toTemperature(command);
                if (temperature != null) {
                    String param = "[" + temperature.toPlainString() + "]";
                    sendCommand("setTargetTemperature", param);
                }
            }
        }
    }
}
