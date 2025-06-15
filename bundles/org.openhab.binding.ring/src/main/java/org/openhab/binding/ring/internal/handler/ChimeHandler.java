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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ring.internal.device.Chime;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * The handler for a Ring Chime.
 *
 * @author Ben Rosenblum - Initial contribution
 *
 */

@NonNullByDefault
public class ChimeHandler extends RingDeviceHandler {
    public ChimeHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Chime handler");
        super.initialize(Chime.class);
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
        logger.debug("ChimeHandler - minuteTick - device {}", getThing().getUID().getId());
        if (device == null) {
            initialize();
        }
    }
}
