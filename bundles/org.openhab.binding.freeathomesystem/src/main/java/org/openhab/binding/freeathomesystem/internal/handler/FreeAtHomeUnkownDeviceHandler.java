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

package org.openhab.binding.freeathomesystem.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeAtHomeActuatorHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andras Uhrin - Initial contribution
 *
 */
@NonNullByDefault
public class FreeAtHomeUnkownDeviceHandler extends FreeAtHomeSystemBaseHandler {

    private final Logger logger = LoggerFactory.getLogger(FreeAtHomeUnkownDeviceHandler.class);

    public FreeAtHomeUnkownDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        // update only via Websocket
        logger.debug("Handle command unknown device - at channel {} - full command {}", channelUID.getAsString(),
                command.toFullString());
    }
}
