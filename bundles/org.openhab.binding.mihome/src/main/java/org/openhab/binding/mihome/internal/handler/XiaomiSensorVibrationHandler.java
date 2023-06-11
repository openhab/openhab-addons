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

import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * Handles the Xiaomi Aqara Smart Motion Vibration Sensor
 *
 * @author Dieter Schmidt - Initial contribution
 */
public class XiaomiSensorVibrationHandler extends XiaomiSensorBaseHandler {

    private final Logger logger = LoggerFactory.getLogger(XiaomiSensorVibrationHandler.class);
    private static final String STATUS = "status";
    private static final String TILT_ANGLE = "final_tilt_angle";
    private static final String ORIENTATIONS = "coordination";
    private static final String BED_ACTIVITY = "bed_activity";

    public XiaomiSensorVibrationHandler(Thing thing) {
        super(thing);
    }

    @Override
    void parseReport(JsonObject data) {
        if (data.has(STATUS)) {
            triggerChannel(CHANNEL_ACTION, data.get(STATUS).getAsString().toUpperCase());
            updateState(CHANNEL_LAST_ACTION, new DateTimeType());
        } else if (data.has(TILT_ANGLE)) {
            updateState(CHANNEL_TILT_ANGLE, new DecimalType(Integer.parseInt((data.get(TILT_ANGLE).getAsString()))));
        } else if (data.has(ORIENTATIONS)) {
            Integer x = 0;
            Integer y = 0;
            Integer z = 0;
            try {
                x = Integer.parseInt((data.get(ORIENTATIONS).getAsString().split(",")[0]));
                y = Integer.parseInt((data.get(ORIENTATIONS).getAsString().split(",")[1]));
                z = Integer.parseInt((data.get(ORIENTATIONS).getAsString().split(",")[2]));
            } catch (NumberFormatException e) {
                logger.error("Could not parse coordinates", e);
            }
            updateState(CHANNEL_ORIENTATION_X, new DecimalType(x));
            updateState(CHANNEL_ORIENTATION_Y, new DecimalType(y));
            updateState(CHANNEL_ORIENTATION_Z, new DecimalType(z));
        } else if (data.has(BED_ACTIVITY)) {
            updateState(CHANNEL_BED_ACTIVITY,
                    new DecimalType(Integer.parseInt((data.get(BED_ACTIVITY).getAsString()))));
        }
    }
}
