/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
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
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.somfytahoma.SomfyTahomaBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SomfyTahomaOnOffHandler} is responsible for handling commands,
 * which are sent to one of the channels of the on/off thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaOnOffHandler extends BaseThingHandler implements SomfyTahomaThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SomfyTahomaOnOffHandler.class);

    public SomfyTahomaOnOffHandler(Thing thing) {
        super(thing);
    }

    SomfyTahomaBridgeHandler bridge = null;

    Gson gson = new Gson();

    @Override
    public String getStateName() {
        return "core:OnOffState";
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String url = getThing().getConfiguration().get("url").toString();

        if (channelUID.getId().equals(SomfyTahomaBindingConstants.SWITCH) && command instanceof OnOffType) {
            bridge.sendCommand(url, "setOnOff", "[\"" + command.toString().toLowerCase() + "\"]");
        }
        if (command.equals(RefreshType.REFRESH)) {
            //sometimes refresh is sent sooner than bridge initialized...
            if (bridge != null) {
                bridge.updateChannelState(this, channelUID, url);
            }
        }
    }

    @Override
    public void initialize() {
        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        bridge = (SomfyTahomaBridgeHandler) this.getBridge().getHandler();
        updateStatus(ThingStatus.ONLINE);
    }
}
