/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * Handles the Xiaomi human body motion sensor
 *
 * @author Patrick Boos - Initial contribution
 * @author Dieter Schmidt - Refactor
 */
public class XiaomiSensorMotionHandler extends XiaomiSensorBaseHandlerWithTimer {

    private static final int DEFAULT_TIMER = 120;
    private static final int MIN_TIMER = 5;
    private static final String STATUS = "status";
    private static final String MOTION = "motion";
    private static final String LUX = "lux";

    private final Logger logger = LoggerFactory.getLogger(XiaomiSensorMotionHandler.class);

    public XiaomiSensorMotionHandler(Thing thing) {
        super(thing, DEFAULT_TIMER, MIN_TIMER, CHANNEL_MOTION_OFF_TIMER);
    }

    @Override
    void parseReport(JsonObject data) {
        boolean hasMotion = data.has(STATUS) && MOTION.equals(data.get(STATUS).getAsString());

        if (data.has(LUX)) {
            int illu = data.get(LUX).getAsInt();
            updateState(CHANNEL_ILLUMINATION, new DecimalType(illu));
        }

        synchronized (this) {
            if (hasMotion) {
                updateState(CHANNEL_MOTION, OnOffType.ON);
                updateState(CHANNEL_LAST_MOTION, new DateTimeType());
                startTimer();
            }
        }
    }

    @Override
    void execute(ChannelUID channelUID, Command command) {
        if (CHANNEL_MOTION_OFF_TIMER.equals(channelUID.getId())) {
            if (command != null && command instanceof DecimalType) {
                setTimerFromDecimalType((DecimalType) command);
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
        updateState(CHANNEL_MOTION, OnOffType.OFF);
    }
}
