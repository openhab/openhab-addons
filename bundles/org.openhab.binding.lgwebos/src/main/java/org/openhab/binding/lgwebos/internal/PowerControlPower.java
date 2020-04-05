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
package org.openhab.binding.lgwebos.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.lgwebos.internal.handler.LGWebOSHandler;
import org.openhab.binding.lgwebos.internal.handler.core.CommandConfirmation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles Power Control Command.
 * Note: Connect SDK only supports powering OFF for most devices.
 *
 * @author Sebastian Prehn - Initial contribution
 */
@NonNullByDefault
public class PowerControlPower extends BaseChannelHandler<CommandConfirmation> {
    private final Logger logger = LoggerFactory.getLogger(PowerControlPower.class);

    private boolean powerOn;

    @Override
    public void onReceiveCommand(String channelId, LGWebOSHandler handler, Command command) {
        if (RefreshType.REFRESH == command) {
            handler.postUpdate(channelId, handler.getSocket().isConnected() ? OnOffType.ON : OnOffType.OFF);
            return;
        }
        if (OnOffType.ON == command) {
            logger.debug(
                    "Received ON - Turning TV on via API is not supported by LG WebOS TVs. You may succeed using wake on lan binding, please consult lgwebos binding documentation.");
        }
        if (!handler.getSocket().isConnected()) {
            /*
             * Unable to send anything to a not connected device.
             * onDeviceReady nor onDeviceRemoved will be called and item state would be permanently inconsistent.
             * Therefore setting state to OFF
             */
            handler.postUpdate(channelId, OnOffType.OFF);
        } else if (OnOffType.OFF == command) {
            if (powerOn) {
                handler.getSocket().powerOff(getDefaultResponseListener());
            } else {
                logger.debug("Received OFF - Ignored as the TV is already off.");
            }
        } else {
            logger.info("Only accept OnOffType, RefreshType. Type was {}.", command.getClass());
        }

    }

    @Override
    public void onDeviceReady(String channelId, LGWebOSHandler handler) {
        powerOn = true;
        handler.postUpdate(channelId, OnOffType.ON);
    }

    @Override
    public void onDeviceRemoved(String channelId, LGWebOSHandler handler) {
        powerOn = false;
        handler.postUpdate(channelId, OnOffType.OFF);
    }
}
