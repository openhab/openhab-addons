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

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Thing;
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
        logger.debug("Cube data: {}", data);
        if (data.has(STATUS)) {
            triggerChannel(CHANNEL_CUBE_ACTION, data.get(STATUS).getAsString().toUpperCase());
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
            triggerChannel(CHANNEL_CUBE_ACTION, rot < 0 ? "ROTATE_LEFT" : "ROTATE_RIGHT");
            updateState(CHANNEL_CUBE_ROTATION_ANGLE, new DecimalType(rot));
            updateState(CHANNEL_CUBE_ROTATION_TIME, new DecimalType(time));
        }
    }
}
