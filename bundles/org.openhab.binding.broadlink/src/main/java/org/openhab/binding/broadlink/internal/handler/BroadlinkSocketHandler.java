/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.broadlink.internal.handler;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * Abstract superclass for power socket devices
 *
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
public abstract class BroadlinkSocketHandler extends BroadlinkBaseThingHandler {

    public BroadlinkSocketHandler(Thing thing) {
        super(thing);
    }

    protected abstract void setStatusOnDevice(int state) throws IOException;

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (channelUID.getId().equals("power-on")) {
                if (command == OnOffType.ON) {
                    setStatusOnDevice(1);
                } else if (command == OnOffType.OFF) {
                    setStatusOnDevice(0);
                }
            }
        } catch (IOException e) {
            logger.warn("Could not send command to socket device: {}", e.getMessage());
        }
    }
}
