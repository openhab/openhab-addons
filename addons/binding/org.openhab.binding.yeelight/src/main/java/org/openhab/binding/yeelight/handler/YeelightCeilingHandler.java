/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yeelight.handler;

import static org.openhab.binding.yeelight.YeelightBindingConstants.*;

import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.yeelight.YeelightBindingConstants;

import com.yeelight.sdk.device.DeviceStatus;

/**
 * The {@link YeelightCeilingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Coaster Li - Initial contribution
 */
public class YeelightCeilingHandler extends YeelightHandlerBase {

    public YeelightCeilingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        handleCommandHelper(channelUID, command, "Handle Ceiling Command {}");
    }

    @Override
    protected void updateUI(DeviceStatus status) {
        super.updateUI(status);
        if (status.isPowerOff()) {
            updateState(YeelightBindingConstants.CHANNEL_BRIGHTNESS, new PercentType(0));
        } else {
            updateState(YeelightBindingConstants.CHANNEL_BRIGHTNESS, new PercentType(status.getBrightness()));
            updateState(YeelightBindingConstants.CHANNEL_COLOR_TEMPERATURE,
                    new PercentType((status.getCt() - COLOR_TEMPERATURE_MINIMUM) / COLOR_TEMPERATURE_STEP));
        }
    }
}
