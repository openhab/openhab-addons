/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.yeelight.internal.handler;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.yeelight.internal.YeelightBindingConstants;
import org.openhab.binding.yeelight.internal.lib.device.DeviceStatus;
import org.openhab.binding.yeelight.internal.lib.enums.ActiveMode;

import static org.openhab.binding.yeelight.internal.YeelightBindingConstants.COLOR_TEMPERATURE_MINIMUM;
import static org.openhab.binding.yeelight.internal.YeelightBindingConstants.COLOR_TEMPERATURE_STEP;

/**
 * The {@link YeelightCeilingWithNightHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Pogudalov Nikita - Initial contribution
 */
public class YeelightCeilingWithNightHandler extends YeelightCeilingHandler {

    public YeelightCeilingWithNightHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        handleCommandHelper(channelUID, command, "Handle Ceiling Command");
    }

    @Override
    protected void updateUI(DeviceStatus status) {
        super.updateUI(status);
        if (status.isPowerOff()) {
            updateState(YeelightBindingConstants.CHANNEL_BRIGHTNESS, PercentType.ZERO);
        } else {
            updateState(YeelightBindingConstants.CHANNEL_BRIGHTNESS, new PercentType(status.getBrightness()));
            updateState(YeelightBindingConstants.CHANNEL_COLOR_TEMPERATURE,
                    new PercentType((status.getCt() - COLOR_TEMPERATURE_MINIMUM) / COLOR_TEMPERATURE_STEP));
        }

        updateState(YeelightBindingConstants.CHANNEL_NIGHTLIGHT,
                (status.getActiveMode() == ActiveMode.MOONLIGHT_MODE) ? OnOffType.ON : OnOffType.OFF);
    }
}
