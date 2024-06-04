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

import static org.openhab.binding.mihome.internal.XiaomiGatewayBindingConstants.*;

import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * Handles the Xiaomi smart door/window sensor
 *
 * @author Patrick Boos - Initial contribution
 * @author Dieter Schmidt - Refactor
 * @author Daniel Walters - Add voltage parsing
 */
public class XiaomiSensorMagnetHandler extends XiaomiSensorBaseHandlerWithTimer {

    private static final int DEFAULT_TIMER = 300;
    private static final int MIN_TIMER = 30;
    private static final String OPEN = "open";
    private static final String CLOSED = "close";
    private static final String STATUS = "status";

    private final Logger logger = LoggerFactory.getLogger(XiaomiSensorMagnetHandler.class);

    public XiaomiSensorMagnetHandler(Thing thing) {
        super(thing, DEFAULT_TIMER, MIN_TIMER, CHANNEL_OPEN_ALARM_TIMER);
    }

    @Override
    void parseReport(JsonObject data) {
        if (data.has(STATUS)) {
            String sensorStatus = data.get(STATUS).getAsString();
            synchronized (this) {
                if (OPEN.equals(sensorStatus)) {
                    updateState(CHANNEL_LAST_OPENED, new DateTimeType());
                    startTimer();
                } else {
                    cancelRunningTimer();
                }
            }
        }
        parseDefault(data);
    }

    @Override
    void parseDefault(JsonObject data) {
        if (data.has(STATUS)) {
            String sensorStatus = data.get(STATUS).getAsString();
            if (OPEN.equals(sensorStatus)) {
                updateState(CHANNEL_IS_OPEN, OpenClosedType.OPEN);
            } else if (CLOSED.equals(sensorStatus)) {
                updateState(CHANNEL_IS_OPEN, OpenClosedType.CLOSED);
            } else {
                updateState(CHANNEL_IS_OPEN, UnDefType.UNDEF);
            }
        }
        super.parseDefault(data);
    }

    @Override
    void execute(ChannelUID channelUID, Command command) {
        if (CHANNEL_OPEN_ALARM_TIMER.equals(channelUID.getId())) {
            if (command instanceof DecimalType decimalCommand) {
                setTimerFromDecimalType(decimalCommand);
                return;
            }
            // Only gets here, if no condition was met
            logger.error("Can't handle command {} on channel {}", command, channelUID);
        } else {
            logger.error("Channel {} does not exist", channelUID);
        }
    }

    @Override
    void onTimer() {
        triggerChannel(CHANNEL_OPEN_ALARM, "ALARM");
    }
}
