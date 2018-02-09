/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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

    private static final String STATUS = "status";
    private static final String OPEN = "open";
    private static final String CLOSED = "close";
    private static final String STOP = "stop";
    private static final String CURTAIN_LEVEL = "curtain_level";
    private static final String AUTO = "auto";

    public XiaomiActorCurtainHandler(Thing thing) {
        super(thing);
        lastDirection = OPEN;
    }

    @Override
    void execute(ChannelUID channelUID, Command command) {
        String status = command.toString().toLowerCase();
        switch (channelUID.getId()) {
            case CHANNEL_CURTAIN_CONTROL:
                if (command instanceof UpDownType) {
                    if (command.equals(UpDownType.UP)) {
                        status = OPEN;
                    } else {
                        status = CLOSED;
                    }
                } else if (command instanceof StopMoveType) {
                    if (command.equals(StopMoveType.STOP)) {
                        status = STOP;
                    } else {
                        status = lastDirection;
                    }
                } else if (command instanceof PercentType) {
                    getXiaomiBridgeHandler().writeToDevice(getItemId(), new String[] { STATUS },
                            new Object[] { AUTO });
                    getXiaomiBridgeHandler().writeToDevice(getItemId(), new String[] { CURTAIN_LEVEL },
                            new Object[] { status });
                } else {
                    logger.warn("Only UpDown or StopMove commands supported - not the command {}", command);
                    return;
                }
                if (OPEN.equals(status) | CLOSED.equals(status)) {
                    if (!status.equals(lastDirection)) {
                        lastDirection = status;
                    }
                }
                getXiaomiBridgeHandler().writeToDevice(getItemId(), new String[] { STATUS }, new Object[] { status });
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
        if (data.has(CURTAIN_LEVEL)) {
            int level = data.get(CURTAIN_LEVEL).getAsInt();
            if (level >= 0 | level <= 100) {
                updateState(CHANNEL_CURTAIN_CONTROL, new PercentType(level));
            }
        }
    }

}
