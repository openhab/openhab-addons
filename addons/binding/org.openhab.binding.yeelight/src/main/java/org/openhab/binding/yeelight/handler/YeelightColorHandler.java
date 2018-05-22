/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yeelight.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.yeelight.YeelightBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yeelight.sdk.device.ConnectState;
import com.yeelight.sdk.device.DeviceStatus;
import com.yeelight.sdk.services.DeviceManager;

/**
 * The {@link YeelightColorHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Coaster Li - Initial contribution
 */
public class YeelightColorHandler extends YeelightHandlerBase {

    private final Logger logger = LoggerFactory.getLogger(YeelightColorHandler.class);

    public YeelightColorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handle Color Command {}", command);

        // if device is disconnect, start discover to reconnect.
        if (mDevice.isAutoConnect() && mDevice.getConnectionState() != ConnectState.CONNECTED) {
            DeviceManager.getInstance().startDiscovery(5 * 1000);
            return;
        }

        switch (channelUID.getId()) {
            case YeelightBindingConstants.CHANNEL_BRIGHTNESS:
                handleBrightnessChannelCommand(channelUID, command);
                break;

            case YeelightBindingConstants.CHANNEL_COLOR:
                handleColorChannelCommand(channelUID, command);
                break;
            case YeelightBindingConstants.CHANNEL_COLOR_TEMPERATURE:
                handleColorTemperatureChannelCommand(channelUID, command);
                break;

            default:
                break;
        }
    }

    @Override
    protected void updateUI(DeviceStatus status) {
        super.updateUI(status);
        updateBrightnessAndColorUI(status);
    }
}
