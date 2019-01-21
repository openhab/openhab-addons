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
package org.openhab.binding.lgwebos.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.lgwebos.internal.handler.LGWebOSHandler;
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
@NonNullByDefault
public class PowerControlPower extends BaseChannelHandler<Void, Object> {
    private final Logger logger = LoggerFactory.getLogger(PowerControlPower.class);

    private PowerControl getControl(ConnectableDevice device) {
        return device.getCapability(PowerControl.class);
    }

    @Override
    public void onReceiveCommand(@Nullable ConnectableDevice device, String channelId, LGWebOSHandler handler,
            Command command) {
        if (device == null) {
            /*
             * Unable to send anything to a null device. Unless the user configured autoupdate="false" neither
             * onDeviceReady nor onDeviceRemoved will be called and item state would be permanently inconsistent.
             * Therefore setting state to OFF
             */
            handler.postUpdate(channelId, OnOffType.OFF);
            return;
        }

        if (OnOffType.ON == command) {
            if (hasCapability(device, PowerControl.On)) {
                getControl(device).powerOn(getDefaultResponseListener());
            }
        } else if (OnOffType.OFF == command) {
            if (hasCapability(device, PowerControl.Off)) {
                getControl(device).powerOff(getDefaultResponseListener());
            }
        } else {
            logger.warn("Only accept OnOffType. Type was {}.", command.getClass());
        }

    }

    @Override
    public void onDeviceReady(ConnectableDevice device, String channelId, LGWebOSHandler handler) {
        handler.postUpdate(channelId, OnOffType.ON);
    }

    @Override
    public void onDeviceRemoved(ConnectableDevice device, String channelId, LGWebOSHandler handler) {
        handler.postUpdate(channelId, OnOffType.OFF);
    }
}
