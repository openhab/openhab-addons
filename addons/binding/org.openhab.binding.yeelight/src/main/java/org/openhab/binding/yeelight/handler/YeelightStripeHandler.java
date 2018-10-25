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

import com.yeelight.sdk.device.DeviceStatus;

/**
 * The {@link YeelightStripeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Coaster Li - Initial contribution
 */
public class YeelightStripeHandler extends YeelightHandlerBase {

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
