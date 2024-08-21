/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.mihome.internal.handler;

import static org.openhab.binding.mihome.internal.XiaomiGatewayBindingConstants.CHANNEL_SWITCH_CH0;

import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * Handles the Xiaomi aqara wall switch with one button
 *
 * @author Dieter Schmidt - Initial contribution
 */
public class XiaomiAqaraActorSwitch1Handler extends XiaomiActorBaseHandler {

    private final Logger logger = LoggerFactory.getLogger(XiaomiAqaraActorSwitch1Handler.class);

    private static final String CHANNEL_0 = "channel_0";
    private static final String ON = "on";

    public XiaomiAqaraActorSwitch1Handler(Thing thing) {
        super(thing);
    }

    @Override
    void execute(ChannelUID channelUID, Command command) {
        if (CHANNEL_SWITCH_CH0.equals(channelUID.getId())) {
            String status = command.toString().toLowerCase();
            getXiaomiBridgeHandler().writeToDevice(getItemId(), new String[] { CHANNEL_0 }, new Object[] { status });
            return;
        }
        // Only gets here, if no condition was met
        logger.error("Can't handle command {} on channel {}", command, channelUID);
    }

    @Override
    void parseReport(JsonObject data) {
        parseDefault(data);
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
    void parseWriteAck(JsonObject data) {
        logger.debug("Got write ack message but ignoring it to prevent item state toggling");
    }

    @Override
    void parseDefault(JsonObject data) {
        if (data.has(CHANNEL_0)) {
            boolean isOn = ON.equals(data.get(CHANNEL_0).getAsString().toLowerCase());
            updateState(CHANNEL_SWITCH_CH0, OnOffType.from(isOn));
        }
    }
}
