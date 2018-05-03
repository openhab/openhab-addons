/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.somfytahoma.handler;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;

import static org.openhab.binding.somfytahoma.SomfyTahomaBindingConstants.STATUS;
import static org.openhab.binding.somfytahoma.SomfyTahomaBindingConstants.VERSION;

/**
 * The {@link SomfyTahomaGatewayHandler} is responsible for handling commands,
 * which are sent to one of the channels of the gateway thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaGatewayHandler extends SomfyTahomaBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SomfyTahomaGatewayHandler.class);

    public SomfyTahomaGatewayHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        updateStatus(getBridge().getStatus());
    }

    @Override
    public Hashtable<String, String> getStateNames() {
        return null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command.equals(RefreshType.REFRESH)) {
            String id = getThing().getConfiguration().get("id").toString();
            if (channelUID.getId().equals(VERSION)) {
                updateState(channelUID, new StringType(getTahomaVersion(id)));
            } else if (channelUID.getId().equals(STATUS)) {
                updateState(channelUID, new StringType(getTahomaStatus(id)));
            }
        }
    }

}
