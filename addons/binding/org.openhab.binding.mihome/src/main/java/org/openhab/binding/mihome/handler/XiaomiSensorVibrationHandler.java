/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.handler;

import static org.openhab.binding.mihome.XiaomiGatewayBindingConstants.*;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Thing;
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
            Integer X = 0;
            Integer Y = 0;
            Integer Z = 0;
            try {
                X = Integer.parseInt((data.get(ORIENTATIONS).getAsString().split(",")[0]));
                Y = Integer.parseInt((data.get(ORIENTATIONS).getAsString().split(",")[1]));
                Z = Integer.parseInt((data.get(ORIENTATIONS).getAsString().split(",")[2]));
            } catch (NumberFormatException e) {
                logger.error("Could not parse coordinates", e);
            }
            updateState(CHANNEL_ORIENTATION_X, new DecimalType(X));
            updateState(CHANNEL_ORIENTATION_Y, new DecimalType(Y));
            updateState(CHANNEL_ORIENTATION_Z, new DecimalType(Z));
        } else if (data.has(BED_ACTIVITY)) {
            updateState(CHANNEL_BED_ACTIVITY,
                    new DecimalType(Integer.parseInt((data.get(BED_ACTIVITY).getAsString()))));
        }
    }
}
