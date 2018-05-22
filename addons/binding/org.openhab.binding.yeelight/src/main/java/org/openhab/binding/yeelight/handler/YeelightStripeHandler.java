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
 * The {@link YeelightStripeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Coaster Li - Initial contribution
 */
public class YeelightStripeHandler extends YeelightHandlerBase {

    private final Logger logger = LoggerFactory.getLogger(YeelightStripeHandler.class);

    public YeelightStripeHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        handleCommandHelper(channelUID, command, "Handle Stripe Command {}");
    }

    @Override
    protected void updateUI(DeviceStatus status) {
        super.updateUI(status);
        updateBrightnessAndColorUI(status);
    }
}
