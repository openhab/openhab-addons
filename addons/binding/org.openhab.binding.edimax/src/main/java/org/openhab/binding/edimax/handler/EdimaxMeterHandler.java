/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.edimax.handler;

import static org.openhab.binding.edimax.EdimaxBindingConstants.*;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EdimaxMeterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Falk Harnisch - Initial contribution
 */
public class EdimaxMeterHandler extends EdimaxHandler {

    private Logger logger = LoggerFactory.getLogger(EdimaxMeterHandler.class);

    public EdimaxMeterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(ENERGY_CHANNEL)) {
            // TODO: handle command

        } else if (channelUID.getId().equals(POWER_CHANNEL)) {
            // TODO: handle command

        }
    }
}
