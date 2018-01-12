/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lgwebos.internal;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.lgwebos.handler.LGWebOSHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.service.capability.PowerControl;

/**
 * Handles Power Control Command.
 * Note: Connect SDK only supports powering OFF for most devices.
 *
 * @author Sebastian Prehn - initial contribution
 */
public class PowerControlPower extends BaseChannelHandler<Void> {
    private final Logger logger = LoggerFactory.getLogger(PowerControlPower.class);

    private PowerControl getControl(ConnectableDevice device) {
        return device.getCapability(PowerControl.class);
    }

    @Override
    public void onReceiveCommand(ConnectableDevice device, String channelId, LGWebOSHandler handler, Command command) {
        if (device == null) {
            /*
             * Unable to send anything to a null device. Unless the user configured autoupdate="false" neither
             * onDeviceReady nor onDeviceRemoved will be called and item state would be permanently inconsistent.
             * Therefore setting state to OFF
             */
            handler.postUpdate(channelId, OnOffType.OFF);
            return;
        }

        if (OnOffType.ON == command || OnOffType.OFF == command) {
            if (OnOffType.ON == command && device.hasCapabilities(PowerControl.On)) {
                getControl(device).powerOn(createDefaultResponseListener());
            } else if (OnOffType.OFF == command && device.hasCapabilities(PowerControl.Off)) {
                getControl(device).powerOff(createDefaultResponseListener());
            }
        } else {
            logger.warn("only accept OnOffType");
            return;
        }
    }

    @Override
    public void onDeviceReady(ConnectableDevice device, String channelId, LGWebOSHandler handler) {
        super.onDeviceReady(device, channelId, handler);
        handler.postUpdate(channelId, OnOffType.ON);
    }

    @Override
    public void onDeviceRemoved(ConnectableDevice device, String channelId, LGWebOSHandler handler) {
        super.onDeviceRemoved(device, channelId, handler);
        handler.postUpdate(channelId, OnOffType.OFF);
    }
}
