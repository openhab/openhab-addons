/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazonechocontrol.handler;

import static org.openhab.binding.amazonechocontrol.AmazonEchoControlBindingConstants.CHANNEL_DIMMER;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.amazonechocontrol.internal.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SmartHomeDimmerHandler} is responsible for the handling a smarthome dimmer device
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class SmartHomeDimmerHandler extends SmartHomeBaseHandler {

    private final Logger logger = LoggerFactory.getLogger(SmartHomeDimmerHandler.class);

    public SmartHomeDimmerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.info("SmartHomeDimmerHandler initialized");
        super.initialize();
    }

    @Override
    public void handleCommand(Connection connection, String entityId, String channelId, Command command)
            throws IOException, URISyntaxException {

        if (channelId.equals(CHANNEL_DIMMER)) {
            if (command == OnOffType.ON) {
                connection.sendSmartHomeDeviceCommand(entityId, "turnOn", null, null);
            }
            if (command == OnOffType.OFF) {
                connection.sendSmartHomeDeviceCommand(entityId, "turnOff", null, null);
            }
        }
        if (channelId.equals(CHANNEL_DIMMER)) {
            if (command instanceof PercentType) {
                PercentType value = (PercentType) command;
                double percent = value.doubleValue();
                if (percent >= 0 && percent <= 100) {
                    String percentValue = String.format(Locale.ROOT, "%.2f", (percent / 100));
                    connection.sendSmartHomeDeviceCommand(entityId, "setPercentage", "percentage", percentValue);
                    updateState(CHANNEL_DIMMER, value);
                }
            }
        }
    }
}
