/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.handler;

import static org.openhab.binding.mihome.XiaomiGatewayBindingConstants.CHANNEL_CURTAIN_CONTROL;

import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * Manage the Xiaomi Smart Curtain over the API
 *
 * @author Kuba Wolanin - initial contribution
 */
public class XiaomiActorCurtainHandler extends XiaomiActorBaseHandler {

    private final Logger logger = LoggerFactory.getLogger(XiaomiActorCurtainHandler.class);
    private String lastDirection;
    private String itemId;

    public XiaomiActorCurtainHandler(Thing thing) {
        super(thing);
        lastDirection = "open";
    }

    @Override
    void execute(ChannelUID channelUID, Command command) {
        String status = command.toString().toLowerCase();
        switch (channelUID.getId()) {
            case CHANNEL_CURTAIN_CONTROL:
                if (command instanceof UpDownType) {
                    if (command.equals(UpDownType.UP)) {
                        status = "open";
                    } else {
                        status = "close";
                    }
                } else if (command instanceof StopMoveType) {
                    if (command.equals(StopMoveType.STOP)) {
                        status = "stop";
                    } else {
                        status = lastDirection;
                    }
                } else if (command instanceof PercentType) {
                    getXiaomiBridgeHandler().writeToDevice(itemId, new String[] { "status" }, new Object[] { "auto" });
                    getXiaomiBridgeHandler().writeToDevice(itemId, new String[] { "curtain_level" },
                            new Object[] { status });
                } else {
                    logger.warn("Only UpDown or StopMove commands supported - not the command {}", command);
                    return;
                }
                if ("open".equals(status) | "close".equals(status)) {
                    if (!status.equals(lastDirection)) {
                        lastDirection = status;
                    }
                }
                getXiaomiBridgeHandler().writeToDevice(itemId, new String[] { "status" }, new Object[] { status });
                break;
            default:
                logger.warn("Can't handle command {} on channel {}", command, channelUID);
        }
    }

    @Override
    void parseHeartbeat(JsonObject data) {
        parseDefault(data);
    }

    @Override
    void parseReadAck(JsonObject data) {
        parseDefault(data);
    }

    @Override
    void parseReport(JsonObject data) {
        parseDefault(data);
    }

    @Override
    void parseWriteAck(JsonObject data) {
        parseDefault(data);
    }

    @Override
    void parseDefault(JsonObject data) {
        if (data.has("curtain_level")) {
            int level = data.get("curtain_level").getAsInt();
            if (level >= 0 | level <= 100) {
                updateState(CHANNEL_CURTAIN_CONTROL, new PercentType(level));
            }
        }
    }

}
