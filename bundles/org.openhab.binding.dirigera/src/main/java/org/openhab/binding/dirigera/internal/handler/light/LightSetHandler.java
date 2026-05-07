/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.dirigera.internal.handler.light;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.DirigeraStateDescriptionProvider;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link LightSetHandler} controls a DIRIGERA light set (multiple lights as one logical unit).
 * Commands are sent to the hub via the /devices/set/{id} endpoint instead of /devices/{id}.
 * All four light capabilities are supported: on/off, brightness, color temperature, and color.
 *
 * @author Bernd Weymann - Initial contribution
 * @author Bernd Weymann - add device set handling
 */
@NonNullByDefault
public class LightSetHandler extends ColorLightHandler {
    private final Logger logger = LoggerFactory.getLogger(LightSetHandler.class);

    public LightSetHandler(Thing thing, Map<String, String> mapping, DirigeraStateDescriptionProvider stateProvider) {
        super(thing, mapping, stateProvider);
        super.setChildHandler(this);
    }

    /**
     * Override sendAttributes to route all light set commands to /devices/set/{id}.
     * This is the only behavioral difference from ColorLightHandler.
     */
    @Override
    protected int sendAttributes(JSONObject attributes) {
        if (customDebug) {
            logger.info("DIRIGERA LIGHT_SET {} sending set attributes {}", thing.getLabel(), attributes);
        }
        return super.sendSetAttributes(attributes);
    }
}
