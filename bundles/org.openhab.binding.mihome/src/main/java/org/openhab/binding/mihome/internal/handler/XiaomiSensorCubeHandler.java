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
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * Handles the Xiaomi magic controller cube
 *
 * @author Patrick Boos - Initial contribution
 * @author Dieter Schmidt - Refactor
 */
public class XiaomiSensorCubeHandler extends XiaomiSensorBaseHandler {

    private final Logger logger = LoggerFactory.getLogger(XiaomiSensorCubeHandler.class);
    private static final String STATUS = "status";
    private static final String ROTATE = "rotate";

    public XiaomiSensorCubeHandler(Thing thing) {
        super(thing);
    }

    @Override
    void parseReport(JsonObject data) {
        if (data.has(STATUS)) {
            triggerChannel(CHANNEL_ACTION, data.get(STATUS).getAsString().toUpperCase());
            updateState(CHANNEL_LAST_ACTION, new DateTimeType());
        } else if (data.has(ROTATE)) {
            Integer rot = 0;
            Integer time = 0;
            try {
                rot = Integer.parseInt((data.get(ROTATE).getAsString().split(",")[0]));
                // convert from percent to angle degrees
                rot = (int) (rot * 3.6);
            } catch (NumberFormatException e) {
                logger.error("Could not parse rotation angle", e);
            }
            try {
                time = Integer.parseInt((data.get(ROTATE).getAsString().split(",")[1]));
            } catch (NumberFormatException e) {
                logger.error("Could not parse rotation time", e);
            }
            updateState(CHANNEL_CUBE_ROTATION_ANGLE, new QuantityType<>(rot, ANGLE_UNIT));
            updateState(CHANNEL_CUBE_ROTATION_TIME, new QuantityType<>(time, TIME_UNIT));
            triggerChannel(CHANNEL_ACTION, rot < 0 ? "ROTATE_LEFT" : "ROTATE_RIGHT");
        }
    }
}
