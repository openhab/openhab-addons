/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazonechocontrol.handler;

import static org.openhab.binding.amazonechocontrol.AmazonEchoControlBindingConstants.CHANNEL_SWITCH;

import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.amazonechocontrol.internal.Connection;

/**
 * The {@link SmartHomeSwitchHandler} is responsible for the handling a smarthome switch device
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class SmartHomeSwitchHandler extends SmartHomeBaseHandler {

    public SmartHomeSwitchHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(Connection connection, String entityId, String channelId, Command command)
            throws IOException, URISyntaxException {
        if (channelId.equals(CHANNEL_SWITCH)) {
            if (command == OnOffType.ON) {
                connection.sendSmartHomeDeviceCommand(entityId, "turnOn", null, null);
                updateState(CHANNEL_SWITCH, OnOffType.ON);
            }
            if (command == OnOffType.OFF) {
                connection.sendSmartHomeDeviceCommand(entityId, "turnOff", null, null);
                updateState(CHANNEL_SWITCH, OnOffType.OFF);
            }
        }
    }
}
