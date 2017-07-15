/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.somfytahoma.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openhab.binding.somfytahoma.SomfyTahomaBindingConstants.CONTROL;
import static org.openhab.binding.somfytahoma.SomfyTahomaBindingConstants.POSITION;

/**
 * The {@link SomfyTahomaAwningHandler} is responsible for handling commands,
 * which are sent to one of the channels of the awning thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaAwningHandler extends BaseThingHandler implements SomfyTahomaThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SomfyTahomaAwningHandler.class);

    public SomfyTahomaAwningHandler(Thing thing) {
        super(thing);
    }

    SomfyTahomaBridgeHandler bridge = null;

    @Override
    public String getStateName() {
        return "core:ClosureState";
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command {} for channel {}", command, channelUID);
        if (!channelUID.getId().equals(POSITION) && !channelUID.getId().equals(CONTROL)) {
            return;
        }

        String url = getThing().getConfiguration().get("url").toString();
        if (command.equals(RefreshType.REFRESH)) {
            //sometimes refresh is sent sooner than bridge initialized...
            if(bridge != null) {
                bridge.updateChannelState(this, channelUID, url);
            }
        } else {
            String cmd = getTahomaCommand(command.toString());
            //Check if the awning is not moving
            String executionId = bridge.getCurrentExecutions(url);
            if (executionId != null) {
                //STOP command should be interpreted if awning moving
                //otherwise do nothing
                if (cmd.equals("my")) {
                    bridge.cancelExecution(executionId);
                }
            } else {
                String param = cmd.equals("setClosure") ? "[" + command.toString() + "]" : "[]";
                bridge.sendCommand(url, cmd, param);
            }
        }

    }

    @Override
    public void initialize() {
        bridge = (SomfyTahomaBridgeHandler) this.getBridge().getHandler();
        updateStatus(ThingStatus.ONLINE);
    }

    private String getTahomaCommand(String command) {

        switch (command) {
            case "DOWN":
                return "down";
            case "UP":
                return "up";
            case "STOP":
                return "my";
            default:
                return "setClosure";
        }
    }

}
