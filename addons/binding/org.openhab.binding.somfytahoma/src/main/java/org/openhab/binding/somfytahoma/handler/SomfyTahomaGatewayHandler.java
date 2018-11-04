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

import static org.eclipse.smarthome.core.thing.Thing.PROPERTY_FIRMWARE_VERSION;
import static org.openhab.binding.somfytahoma.SomfyTahomaBindingConstants.STATUS;

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
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command {} for channel {}", command, channelUID);
        if (RefreshType.REFRESH.equals(command)) {
            updateChannelState(channelUID);
        }
    }

    @Override
    public void updateChannelState(ChannelUID channelUID) {
        if (STATUS.equals(channelUID.getId())) {
            String id = getThing().getConfiguration().get("id").toString();
            updateState(channelUID, new StringType(getTahomaStatus(id)));
            //update the firmware property
            updateProperty(PROPERTY_FIRMWARE_VERSION, getTahomaVersion(id));
        }
    }

}
