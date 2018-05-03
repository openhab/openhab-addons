/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.somfytahoma.handler;

import com.google.gson.Gson;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;

import static org.openhab.binding.somfytahoma.SomfyTahomaBindingConstants.EXECUTE_ACTION;

/**
 * The {@link SomfyTahomaActionGroupHandler} is responsible for handling commands,
 * which are sent to one of the channels of the action group thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaActionGroupHandler extends SomfyTahomaBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SomfyTahomaActionGroupHandler.class);

    public SomfyTahomaActionGroupHandler(Thing thing) {
        super(thing);
    }

    private Gson gson = new Gson();

    @Override
    public Hashtable<String, String> getStateNames() {
        return null;
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    protected boolean isAlwaysOnline() {
        return true;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Action group: {} received command: {}", channelUID.getId(),command.toString());
        if (channelUID.getId().equals(EXECUTE_ACTION) && command instanceof OnOffType) {
            if (command.equals(OnOffType.ON)) {
                executeActionGroup();
            }
        }
    }
}
