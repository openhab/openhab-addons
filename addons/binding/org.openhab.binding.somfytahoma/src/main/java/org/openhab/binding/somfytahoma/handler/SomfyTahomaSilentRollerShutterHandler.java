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
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;

import static org.openhab.binding.somfytahoma.SomfyTahomaBindingConstants.*;

/**
 * The {@link SomfyTahomaSilentRollerShutterHandler} is responsible for handling commands,
 * which are sent to one of the channels of the silent roller shutter thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaSilentRollerShutterHandler extends SomfyTahomaBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SomfyTahomaSilentRollerShutterHandler.class);

    public SomfyTahomaSilentRollerShutterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public Hashtable<String, String> getStateNames() {
        return new Hashtable<String, String>() {{
            put(POSITION, "core:ClosureState");
            put(POSITION_SILENT, "core:ClosureState");
            put(CONTROL, "core:ClosureState");
        }};
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command {} for channel {}", command, channelUID);
        if (!channelUID.getId().equals(POSITION) && !channelUID.getId().equals(CONTROL) && !channelUID.getId().equals(POSITION_SILENT)) {
            return;
        }

        String url = getThing().getConfiguration().get("url").toString();
        if (command.equals(RefreshType.REFRESH)) {
            //sometimes refresh is sent sooner than bridge initialized...
            if (getBridgeHandler() != null) {
                getBridgeHandler().updateChannelState(this, channelUID, url);
            }
        } else {
            if (channelUID.getId().equals(POSITION) || channelUID.getId().equals(CONTROL)) {
                String cmd = getTahomaCommand(command.toString());
                //Check if the rollershutter is not moving
                String executionId = getBridgeHandler().getCurrentExecutions(url);
                if (executionId != null) {
                    //STOP command should be interpreted if rollershutter is moving
                    //otherwise do nothing
                    if (cmd.equals(COMMAND_MY)) {
                        getBridgeHandler().cancelExecution(executionId);
                    }
                } else {
                    String param = cmd.equals(COMMAND_SET_CLOSURE) ? "[" + command.toString() + "]" : "[]";
                    getBridgeHandler().sendCommand(url, cmd, param);
                }
            } else {
                // POSITION_SILENT
                String param = "[" + command.toString() + ", \"lowspeed\"]";
                getBridgeHandler().sendCommand(url, COMMAND_SET_CLOSURESPEED, param);

            }
        }
    }

    private String getTahomaCommand(String command) {
        switch (command) {
            case "OFF":
            case "DOWN":
                return COMMAND_DOWN;
            case "ON":
            case "UP":
                return COMMAND_UP;
            case "STOP":
                return COMMAND_MY;
            default:
                return COMMAND_SET_CLOSURE;
        }
    }
}
