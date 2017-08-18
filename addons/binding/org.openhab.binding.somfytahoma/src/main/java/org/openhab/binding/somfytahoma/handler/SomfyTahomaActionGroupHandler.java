/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 * <p>
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
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.somfytahoma.model.SomfyTahomaAction;
import org.openhab.binding.somfytahoma.model.SomfyTahomaCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Hashtable;

import static org.openhab.binding.somfytahoma.SomfyTahomaBindingConstants.TRIGGER;
import static org.openhab.binding.somfytahoma.SomfyTahomaBindingConstants.TYPE_PERCENT;

/**
 * The {@link SomfyTahomaActionGroupHandler} is responsible for handling commands,
 * which are sent to one of the channels of the action group thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaActionGroupHandler extends BaseThingHandler implements SomfyTahomaThingHandler {

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
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(TRIGGER) && command instanceof OnOffType) {
            if ("ON".equals(command.toString())) {
                String url = getThing().getConfiguration().get("url").toString();
                ArrayList<SomfyTahomaAction> actions = getBridgeHandler().getTahomaActions(url);
                for (SomfyTahomaAction action : actions) {
                    sendCommand(action);
                }
            }
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
    }

    private void sendCommand(SomfyTahomaAction action) {

        for (SomfyTahomaCommand command : action.getCommands()) {
            String parameters = command.getType() == TYPE_PERCENT ? gson.toJson(command.getPercentParameters()) : gson.toJson(command.getParameters());
            logger.debug("Sending to device {} command {} params {}", action.getDeviceURL(), command.getName(), parameters);
            getBridgeHandler().sendCommand(action.getDeviceURL(), command.getName(), parameters);
        }
    }

    private SomfyTahomaBridgeHandler getBridgeHandler() {
        return (SomfyTahomaBridgeHandler) this.getBridge().getHandler();
    }

}
