/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.openhab.binding.mihome.internal.XiaomiGatewayBindingConstants.*;

import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * Handles the Xiaomi smart plug device
 *
 * @author Patrick Boos - Initial contribution
 * @author Dieter Schmidt - Refactor
 */
public class XiaomiActorPlugHandler extends XiaomiActorBaseHandler {

    private final Logger logger = LoggerFactory.getLogger(XiaomiActorPlugHandler.class);

    private static final String STATUS = "status";
    private static final String IN_USE = "inuse";
    private static final String LOAD_POWER = "load_power";
    private static final String ON = "on";
    private static final String POWER_CONSUMED = "power_consumed";

    public XiaomiActorPlugHandler(Thing thing) {
        super(thing);
    }

    @Override
    void execute(ChannelUID channelUID, Command command) {
        if (CHANNEL_POWER_ON.equals(channelUID.getId())) {
            String status = command.toString().toLowerCase();
            getXiaomiBridgeHandler().writeToDevice(getItemId(), new String[] { STATUS }, new Object[] { status });
            return;
        }
        // Only gets here, if no condition was met
        logger.warn("Can't handle command {} on channel {}", command, channelUID);
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
        getStatusFromData(data);
    }

    @Override
    void parseWriteAck(JsonObject data) {
        parseDefault(data);
    }

    @Override
    void parseDefault(JsonObject data) {
        getStatusFromData(data);
        if (data.has(IN_USE)) {
            updateState(CHANNEL_IN_USE, (data.get(IN_USE).getAsInt() == 1) ? OnOffType.ON : OnOffType.OFF);
        }
        if (data.has(LOAD_POWER)) {
            updateState(CHANNEL_LOAD_POWER, new DecimalType(data.get(LOAD_POWER).getAsBigDecimal()));
        }
        if (data.has(POWER_CONSUMED)) {
            updateState(CHANNEL_POWER_CONSUMED,
                    new DecimalType(data.get(POWER_CONSUMED).getAsBigDecimal().scaleByPowerOfTen(-3)));
        }
    }

    private void getStatusFromData(JsonObject data) {
        if (data.has(STATUS)) {
            boolean isOn = ON.equals(data.get(STATUS).getAsString());
            updateState(CHANNEL_POWER_ON, isOn ? OnOffType.ON : OnOffType.OFF);
            if (!isOn) {
                updateState(CHANNEL_IN_USE, OnOffType.OFF);
                updateState(CHANNEL_LOAD_POWER, new DecimalType(0));
            }
        }
    }
}
