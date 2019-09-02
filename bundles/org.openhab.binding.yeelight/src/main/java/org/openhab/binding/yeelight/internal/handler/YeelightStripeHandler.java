/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.yeelight.internal.lib.device.DeviceStatus;

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
        handleCommandHelper(channelUID, command, "Handle Stripe Command");
    }

    @Override
    protected void updateUI(DeviceStatus status) {
        super.updateUI(status);
        updateBrightnessAndColorUI(status);
    }
}
