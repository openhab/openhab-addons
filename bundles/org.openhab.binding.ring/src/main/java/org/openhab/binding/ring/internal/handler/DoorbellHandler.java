/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.ring.internal.handler;

import static org.openhab.binding.ring.RingBindingConstants.CHANNEL_STATUS_BATTERY;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ring.internal.api.RingDeviceTO;
import org.openhab.binding.ring.internal.device.Doorbell;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * The handler for a Ring Video Doorbell.
 *
 * @author Wim Vissers - Initial contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 *
 */

@NonNullByDefault
public class DoorbellHandler extends RingDeviceHandler {
    private int lastBattery = -1;

    public DoorbellHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Doorbell handler");
        super.initialize(Doorbell.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Do Nothing
    }

    @Override
    protected void refreshState() {
        // Do Nothing
    }

    @Override
    protected void minuteTick() {
        logger.debug("DoorbellHandler - minuteTick - device {}", getThing().getUID().getId());
        if (device == null) {
            initialize();
        }
        RingDeviceTO deviceTO = device.getDeviceStatus();
        if (deviceTO.health.batteryPercentage != lastBattery) {
            logger.debug("Battery Level: {}", deviceTO.health.batteryPercentage);
            ChannelUID channelUID = new ChannelUID(thing.getUID(), CHANNEL_STATUS_BATTERY);
            updateState(channelUID, new DecimalType(deviceTO.health.batteryPercentage));
            lastBattery = deviceTO.health.batteryPercentage;
        } else {
            logger.debug("Battery Level Unchanged for {} - {} vs {}", getThing().getUID().getId(),
                    deviceTO.health.batteryPercentage, lastBattery);
        }
    }
}
